package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.FunctionalLattice;
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

import java.util.*;
import java.util.function.Predicate;

public class TwoVarsLinearInequality extends FunctionalLattice<TwoVarsLinearInequality, Identifier, TwoVarsLinearInequality.ConstraintSet> implements ValueDomain<TwoVarsLinearInequality> {

    // Static constants for top and bottom
    public static final TwoVarsLinearInequality TOP = new TwoVarsLinearInequality(
            new ConstraintSet(Collections.emptySet())
    );
    private static final TwoVarsLinearInequality BOTTOM = new TwoVarsLinearInequality(
            new ConstraintSet(Collections.singleton(new TwoVarsInequality(0, null, 0, null, -1))) // 0 <= -1
    );

    private final ConstraintSet constraintSet;

    public TwoVarsLinearInequality() {
        super(new ConstraintSet(Collections.emptySet()));
        this.constraintSet = lattice; // Use the lattice passed to super()
    }

    private TwoVarsLinearInequality(ConstraintSet constraintSet) {
        super(constraintSet);
        this.constraintSet = constraintSet;
    }

    @Override
    public TwoVarsLinearInequality bottom() {
        return BOTTOM;
    }

    @Override
    public TwoVarsLinearInequality top() {
        return TOP;
    }

    @Override
    public ConstraintSet stateOfUnknown(Identifier key) {
        return lattice.top();
    }

    @Override
    public TwoVarsLinearInequality mk(ConstraintSet lattice, Map<Identifier, ConstraintSet> function) {
        return new TwoVarsLinearInequality(lattice);
    }

    @Override
    public TwoVarsLinearInequality lubAux(TwoVarsLinearInequality other) throws SemanticException {
        return new TwoVarsLinearInequality(this.constraintSet.lub(other.constraintSet));
    }

    @Override
    public boolean lessOrEqualAux(TwoVarsLinearInequality other) throws SemanticException {
        return this.constraintSet.lessOrEqual(other.constraintSet);
    }

    @Override
    public TwoVarsLinearInequality wideningAux(TwoVarsLinearInequality other) throws SemanticException {
        return new TwoVarsLinearInequality(this.constraintSet.widening(other.constraintSet));
    }

    @Override
    public TwoVarsLinearInequality glbAux(TwoVarsLinearInequality other) throws SemanticException {
        return new TwoVarsLinearInequality(this.constraintSet.glb(other.constraintSet));
    }


    @Override
    public TwoVarsLinearInequality assign(Identifier id, ValueExpression expression, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;
    }

    @Override
    public TwoVarsLinearInequality smallStepSemantics(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;
    }

    @Override
    public TwoVarsLinearInequality assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        return null;
    }

    @Override
    public boolean knowsIdentifier(Identifier id) {
        return false;
    }

    @Override
    public TwoVarsLinearInequality forgetIdentifier(Identifier id) throws SemanticException {
        return null;
    }

    @Override
    public TwoVarsLinearInequality forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        return null;
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;
    }

    @Override
    public TwoVarsLinearInequality pushScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public TwoVarsLinearInequality popScope(ScopeToken token) throws SemanticException {
        return null;
    }

    @Override
    public StructuredRepresentation representation() {
        return constraintSet.representation();
    }

    // Represents a single TVPI inequality: ax + by <= c
    public static class TwoVarsInequality {
        private final int a, b, c;
        private final Identifier x, y; // null if variable is absent

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
            if (b != 0)
                left += (b > 0 && !left.isEmpty() ? " + " : b < 0 ? " - " : "") + Math.abs(b) + "*" + y;
            return left + " <= " + c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TwoVarsInequality that = (TwoVarsInequality) o;
            return a == that.a && b == that.b && c == that.c &&
                    (Objects.equals(x, that.x)) &&
                    (Objects.equals(y, that.y));
        }
    }

    // Nested lattice for the constraint set
    public static class ConstraintSet implements Lattice<ConstraintSet> {
        private final Set<TwoVarsInequality> constraints;

        public ConstraintSet(Set<TwoVarsInequality> constraints) {
            this.constraints = constraints;
        }

        @Override
        public ConstraintSet top() {
            return new ConstraintSet(Collections.emptySet());
        }

        // 0 <= -1
        @Override
        public ConstraintSet bottom() {
            return new ConstraintSet(Collections.singleton(new TwoVarsInequality(0, null, 0, null, -1)));
        }

        @Override
        public ConstraintSet lub(ConstraintSet other) throws SemanticException {
            if (isBottom()) return other;
            if (other.isBottom()) return this;
            if (isTop() || other.isTop()) return top();

            Set<TwoVarsInequality> result = new HashSet<>(this.constraints);
            result.addAll(other.constraints);
            return new ConstraintSet(eliminateRedundancies(result));
        }

        @Override
        public ConstraintSet glb(ConstraintSet other) throws SemanticException {
            if (isTop()) return other;
            if (other.isTop()) return this;
            if (isBottom() || other.isBottom()) return bottom();

            Set<TwoVarsInequality> result = new HashSet<>(this.constraints);
            result.addAll(other.constraints);
            return checkSatisfiability(result) ? new ConstraintSet(result) : bottom();
        }

        @Override
        public ConstraintSet widening(ConstraintSet other) throws SemanticException {
            if (isBottom()) return other;
            if (other.isBottom()) return this;
            if (isTop() || other.isTop()) return top();

            Set<TwoVarsInequality> result = new HashSet<>();
            for (TwoVarsInequality c : this.constraints) {
                if (other.satisfies(c)) result.add(c);
            }
            return new ConstraintSet(eliminateRedundancies(result));
        }

        @Override
        public boolean lessOrEqual(ConstraintSet other) throws SemanticException {
            if (isBottom()) return true;
            if (other.isTop()) return true;
            if (isTop() && !other.isTop()) return false;
            if (other.isBottom() && !isBottom()) return false;

            for (TwoVarsInequality c : this.constraints) {
                if (!other.satisfies(c)) return false;
            }
            return true;
        }

        @Override
        public boolean isTop() {
            return constraints.isEmpty();
        }

        @Override
        public boolean isBottom() {
            if (constraints.size() == 1) {
                TwoVarsInequality c = constraints.iterator().next();
                return c.a == 0 && c.b == 0 && c.x == null && c.y == null && c.c < 0;
            }
            return false;
        }

        private Set<TwoVarsInequality> eliminateRedundancies(Set<TwoVarsInequality> constraints) {
            return new HashSet<>(constraints); // TO DO
        }

        private boolean checkSatisfiability(Set<TwoVarsInequality> constraints) {
            return true; // TO DO
        }

        private boolean satisfies(TwoVarsInequality c) {
            return true; // TO DO
        }

        @Override
        public StructuredRepresentation representation() {
            if (isTop()) return Lattice.topRepresentation();
            if (isBottom()) return Lattice.bottomRepresentation();
            return new StringRepresentation(constraints.toString());
        }
    }
}
