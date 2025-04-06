package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import org.apache.logging.log4j.util.SystemPropertiesPropertySource;

import java.util.*;
import java.util.function.Predicate;

public class TwoVarsLinearInequality implements ValueDomain<TwoVarsLinearInequality> {
    static final TwoVarsLinearInequality TOP = new TwoVarsLinearInequality(true);
    static final TwoVarsLinearInequality BOTTOM = new TwoVarsLinearInequality(false);

    final Set<TwoVarsInequality> constraints;
    private final boolean isTop;

    private TwoVarsLinearInequality(boolean isTop) {
        this.isTop = isTop;
        this.constraints = isTop ? Collections.emptySet() : Collections.singleton(new TwoVarsInequality(0, null, 0, null, -1));
    }

    public TwoVarsLinearInequality(Set<TwoVarsInequality> constraints) {
        this.isTop = false;
        this.constraints = new HashSet<>(constraints);
        complete();
    }

    public TwoVarsLinearInequality() {
        this.isTop = false;
        this.constraints = new HashSet<>();
        complete(); // Apply closure to the empty set (which does nothing in this case)
    }

    @Override
    public TwoVarsLinearInequality top() {
        return TOP;
    }

    @Override
    public TwoVarsLinearInequality bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return isTop && constraints.isEmpty();
    }

    @Override
    public boolean isBottom() {
        return !isTop && constraints.size() == 1 && constraints.iterator().next().c < 0 && constraints.iterator().next().a == 0 && constraints.iterator().next().b == 0;
    }

    @Override
    public TwoVarsLinearInequality lub(TwoVarsLinearInequality other) throws SemanticException {
        if (isTop() || other.isTop()) return TOP;
        if (isBottom()) return other;
        if (other.isBottom()) return this;

        Set<TwoVarsInequality> result = new HashSet<>(this.constraints);
        result.addAll(other.constraints);
        return new TwoVarsLinearInequality(eliminateRedundancies(result));
    }

    @Override
    public TwoVarsLinearInequality glb(TwoVarsLinearInequality other) throws SemanticException {
        if (isBottom() || other.isBottom()) return BOTTOM;
        if (isTop()) return other;
        if (other.isTop()) return this;

        Set<TwoVarsInequality> result = new HashSet<>(this.constraints);
        result.addAll(other.constraints);
        return checkSatisfiability(result) ? new TwoVarsLinearInequality(result) : BOTTOM;
    }

    @Override
    public TwoVarsLinearInequality widening(TwoVarsLinearInequality other) throws SemanticException {
        if (isBottom()) return other;
        if (other.isBottom()) return this;
        if (isTop() || other.isTop()) return TOP;

        Set<TwoVarsInequality> result = new HashSet<>();
        for (TwoVarsInequality c : this.constraints) {
            if (other.satisfies(c)) result.add(c);
        }
        return new TwoVarsLinearInequality(eliminateRedundancies(result));
    }

    @Override
    public boolean lessOrEqual(TwoVarsLinearInequality other) throws SemanticException {
        if (isBottom()) return true;
        if (other.isTop()) return true;
        if (isTop() && !other.isTop()) return false;

        for (TwoVarsInequality c : this.constraints) {
            if (!other.satisfies(c)) return false;
        }
        return true;
    }

    @Override
    public TwoVarsLinearInequality assign(Identifier id, ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if (isBottom()) return this;
        if (isHeapIdentifier(id)) return this;

        Set<TwoVarsInequality> newConstraints = project(id).constraints;
        if (expression instanceof Identifier) {
            Identifier exprId = (Identifier) expression;
            if (!isHeapIdentifier(exprId)) {
                newConstraints.add(new TwoVarsInequality(1, id, -1, exprId, 0));
                newConstraints.add(new TwoVarsInequality(-1, id, 1, exprId, 0));
            }
        } else if (expression instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) expression;
            if (bin.getOperator() instanceof AdditionOperator && bin.getLeft() instanceof Identifier && bin.getRight() instanceof Constant) {
                Identifier left = (Identifier) bin.getLeft();
                Constant right = (Constant) bin.getRight();
                if (!isHeapIdentifier(left) && right.getValue() instanceof Integer) {
                    int value = (Integer) right.getValue();
                    newConstraints.add(new TwoVarsInequality(1, id, -1, left, value));
                    newConstraints.add(new TwoVarsInequality(-1, id, 1, left, -value));
                }
            }
        }
        return checkSatisfiability(newConstraints) ? new TwoVarsLinearInequality(newConstraints) : BOTTOM;
    }

    @Override
    public TwoVarsLinearInequality smallStepSemantics(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        // For a relational domain like TVPI, we typically don't modify the state
        // unless the expression provides new constraints, which is handled by assume or assign
        return this;
    }

    @Override
    public TwoVarsLinearInequality assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle)
            throws SemanticException {
        if (isBottom()) return this;

        Set<TwoVarsInequality> newConstraints = new HashSet<>(constraints);
        if (expression instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) expression;
            BinaryOperator op = bin.getOperator();
            if (bin.getLeft() instanceof Identifier && bin.getRight() instanceof BinaryExpression) {
                Identifier left = (Identifier) bin.getLeft();
                BinaryExpression rightExpr = (BinaryExpression) bin.getRight();
                if (rightExpr.getOperator() instanceof AdditionOperator &&
                        rightExpr.getLeft() instanceof Identifier &&
                        rightExpr.getRight() instanceof Constant) {
                    Identifier rightId = (Identifier) rightExpr.getLeft();
                    Constant rightConst = (Constant) rightExpr.getRight();
                    if (!isHeapIdentifier(left) && !isHeapIdentifier(rightId) && rightConst.getValue() instanceof Integer) {
                        int value = (Integer) rightConst.getValue();
                        if (op instanceof ComparisonLe) {
                            newConstraints.add(new TwoVarsInequality(1, left, -1, rightId, value));
                        }
                    }
                }
            } else if (bin.getLeft() instanceof Identifier && bin.getRight() instanceof Identifier) {
                Identifier left = (Identifier) bin.getLeft();
                Identifier right = (Identifier) bin.getRight();
                if (!isHeapIdentifier(left) && !isHeapIdentifier(right)) {
                    if (op instanceof ComparisonLe) {
                        newConstraints.add(new TwoVarsInequality(1, left, -1, right, 0));
                    }
                }
            }
        }
        return checkSatisfiability(newConstraints) ? new TwoVarsLinearInequality(newConstraints) : BOTTOM;
    }

    @Override
    public boolean knowsIdentifier(Identifier id) {
        if (isTop() || isBottom() || isHeapIdentifier(id)) {
            return false; // TOP and BOTTOM don't "know" any specific identifier, and we ignore heap identifiers
        }
        for (TwoVarsInequality c : constraints) {
            if ((c.x != null && c.x.equals(id)) || (c.y != null && c.y.equals(id))) {
                return true; // The identifier is present in at least one constraint
            }
        }
        return false; // The identifier is not present in any constraint
    }

    @Override
    public TwoVarsLinearInequality forgetIdentifier(Identifier id) throws SemanticException {
        System.out.println("Forgetting " + id);
        if (isTop() || isBottom() || isHeapIdentifier(id)) return this;
        return new TwoVarsLinearInequality(project(id).constraints);
    }

    @Override
    public TwoVarsLinearInequality forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        if (isTop() || isBottom()) {
            return this; // No change for TOP or BOTTOM
        }

        Set<TwoVarsInequality> newConstraints = new HashSet<>();
        for (TwoVarsInequality c : constraints) {
            // Keep the constraint if neither x nor y satisfies the predicate
            if ((c.x == null || !test.test(c.x)) && (c.y == null || !test.test(c.y))) {
                newConstraints.add(c);
            }
        }
        return new TwoVarsLinearInequality(newConstraints);
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        // To be completed later for full satisfiability checking
        return Satisfiability.UNKNOWN;
    }

    @Override
    public TwoVarsLinearInequality pushScope(ScopeToken token) throws SemanticException {
        // For a simple relational domain, we don't modify the constraints when entering a scope
        return this;
    }

    @Override
    public TwoVarsLinearInequality popScope(ScopeToken token) throws SemanticException {
        // For a simple relational domain, we don't modify the constraints when exiting a scope
        return this;
    }

    // Utility methods
    private void complete() {
        Set<TwoVarsInequality> closure = computeClosure(constraints);
        constraints.clear();
        constraints.addAll(closure);
    }

    private Set<TwoVarsInequality> computeClosure(Set<TwoVarsInequality> constraints) {
        Set<TwoVarsInequality> closure = new HashSet<>(constraints);

        // 1. Check for constant contradictions (x and y are null or a = 0 and b = 0)
        for (TwoVarsInequality c : closure) {
            if (c.a == 0 && c.b == 0 && c.x == null && c.y == null && c.c < 0) {
                //Bottom
                return Collections.singleton(new TwoVarsInequality(0, null, 0, null, -1));
            }
        }

        // 2. Combine identical terms, keeps tighter one
        // if there are x - y <= 5 and x - y <= 3 keeps x - y <= 3
        Map<String, TwoVarsInequality> combined = new HashMap<>();
        for (TwoVarsInequality c : closure) {
            String key = (c.x != null ? c.x.toString() : "null") + "," +
                    (c.y != null ? c.y.toString() : "null") + "," +
                    c.a + "," + c.b;
            if (combined.containsKey(key)) {
                TwoVarsInequality existing = combined.get(key);
                if (existing.c > c.c) {
                    combined.put(key, c);
                }
            } else {
                combined.put(key, c);
            }
        }
        System.out.println("Combined map: " + combined.values());
        closure.clear();
        closure.addAll(combined.values());

        // 3. Simple transitivity check with tighter-bound filter
        // if there are constraints of form x - y <= 5 and y - z <= 3 this adds x - z <= 8 constraint
        Set<TwoVarsInequality> toAdd = new HashSet<>();
        for (TwoVarsInequality c1 : closure) {
            for (TwoVarsInequality c2 : closure) {
                if (c1 != c2 && c1.y != null && c2.x != null && c1.y.equals(c2.x)) {
                    if (c1.b * c2.a < 0) {
                        int newA = c1.a;
                        int newB = c2.b;
                        int newC = c1.c * Math.abs(c2.a) + c2.c * Math.abs(c1.b);
                        Identifier newX = c1.x;
                        Identifier newY = c2.y;
                        TwoVarsInequality derived = new TwoVarsInequality(newA, newX, newB, newY, newC);
                        if (newA == 0 && newB == 0 && newX == null && newY == null && newC < 0) {
                            return Collections.singleton(new TwoVarsInequality(0, null, 0, null, -1));
                        }
                        String key = (newX != null ? newX.toString() : "null") + "," +
                                (newY != null ? newY.toString() : "null") + "," +
                                newA + "," + newB;
                        if (combined.containsKey(key)) {
                            TwoVarsInequality existing = combined.get(key);
                            if (existing.c > newC) {
                                toAdd.add(derived);
                            }
                        } else {
                            toAdd.add(derived);
                        }
                    }
                }
            }
        }
        System.out.println("To add from transitivity: " + toAdd);
        closure.addAll(toAdd);
        return closure;
    }

    private Set<TwoVarsInequality> eliminateRedundancies(Set<TwoVarsInequality> constraints) {
        // To be completed with redundancy elimination
        return new HashSet<>(constraints);
    }

    private boolean checkSatisfiability(Set<TwoVarsInequality> constraints) {
        // To be completed with full satisfiability checking
        return true;
    }

    private TwoVarsLinearInequality project(Identifier id) {
        Set<TwoVarsInequality> result = new HashSet<>();
        for (TwoVarsInequality c : constraints) {
            if ((c.x != null && c.x.equals(id)) || (c.y != null && c.y.equals(id))) continue;
            result.add(c);
        }
        return new TwoVarsLinearInequality(result);
    }

    private boolean satisfies(TwoVarsInequality c) {
        // To be completed with satisfiability checking
        return true;
    }

    public static boolean isHeapIdentifier(Identifier id) {
        if (id == null) return false;
        String idStr = id.toString();
        return idStr.contains("heap") || idStr.contains("this") || idStr.contains("&pp@");
    }

    @Override
    public StructuredRepresentation representation() {
        if (isTop()) return Lattice.topRepresentation();
        if (isBottom()) return Lattice.bottomRepresentation();
        return new StringRepresentation(constraints.toString());
    }

    public static class TwoVarsInequality {
        final int a, b, c;
        final Identifier x;
        final Identifier y;

        public TwoVarsInequality(int a, Identifier x, int b, Identifier y, int c) {
            this.a = a;
            this.x = x;
            this.b = b;
            this.y = y;
            this.c = c;
        }

        @Override
        public String toString() {
            String left = "";
            if (a != 0) left += a + "*" + x;
            if (b != 0) left += (b > 0 && !left.isEmpty() ? " + " : b < 0 ? " - " : "") + Math.abs(b) + "*" + y;
            return left + " <= " + c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TwoVarsInequality that = (TwoVarsInequality) o;
            return a == that.a && b == that.b && c == that.c && Objects.equals(x, that.x) && Objects.equals(y, that.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c, x, y);
        }
    }
}