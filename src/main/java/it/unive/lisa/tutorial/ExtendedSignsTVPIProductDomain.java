package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.combination.ValueCartesianProduct;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.HashSet;
import java.util.Set;

public class ExtendedSignsTVPIProductDomain extends ValueCartesianProduct<
        ValueEnvironment<ExtendedSigns>,
        TwoVarsLinearInequality> {

    public ExtendedSignsTVPIProductDomain(ValueEnvironment<ExtendedSigns> left, TwoVarsLinearInequality right) {
        super(left, right);
    }

    @Override
    public ExtendedSignsTVPIProductDomain mk(ValueEnvironment<ExtendedSigns> left, TwoVarsLinearInequality right) {
        return new ExtendedSignsTVPIProductDomain(left, right).reduce();
    }

    @Override
    public ExtendedSignsTVPIProductDomain assign(Identifier id, ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ValueEnvironment<ExtendedSigns> newLeft = this.left.assign(id, expression, pp, oracle);
        TwoVarsLinearInequality newRight = this.right.assign(id, expression, pp, oracle);
        return new ExtendedSignsTVPIProductDomain(newLeft, newRight).reduce();
    }

    @Override
    public ExtendedSignsTVPIProductDomain assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle)
            throws SemanticException {
        ValueEnvironment<ExtendedSigns> newLeft = this.left.assume(expression, src, dest, oracle);
        TwoVarsLinearInequality newRight = this.right.assume(expression, src, dest, oracle);
        return new ExtendedSignsTVPIProductDomain(newLeft, newRight).reduce();
    }

    @Override
    public ExtendedSignsTVPIProductDomain forgetIdentifier(Identifier id) throws SemanticException {
        ValueEnvironment<ExtendedSigns> newLeft = this.left.forgetIdentifier(id);
        TwoVarsLinearInequality newRight = this.right.forgetIdentifier(id);
        return new ExtendedSignsTVPIProductDomain(newLeft, newRight).reduce();
    }

    @Override
    public StructuredRepresentation representation() {
        if (isTop()) return Lattice.topRepresentation();
        if (isBottom()) return Lattice.bottomRepresentation();
        return new StringRepresentation("ExtendedSigns: " + left.representation() + ", TVPI: " + right.representation());
    }

    private ExtendedSignsTVPIProductDomain reduce() {
        ValueEnvironment<ExtendedSigns> newLeft = this.left;
        TwoVarsLinearInequality newRight = this.right;

        if (newRight.isBottom() || newLeft.isBottom()) {
            return new ExtendedSignsTVPIProductDomain(newLeft.bottom(), newRight.bottom());
        }

        // Raffine TwoVarsLinearInequality depuis ExtendedSigns
        Set<TwoVarsLinearInequality.TwoVarsInequality> newConstraints = new HashSet<>(newRight.constraints);
        for (Identifier id : newLeft.getKeys()) {
            ExtendedSigns sign = newLeft.getState(id);
            if (sign.equals(ExtendedSigns.ZERO)) {
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(1, id, 0, null, 0));  // id <= 0
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(-1, id, 0, null, 0)); // id >= 0
            } else if (sign.equals(ExtendedSigns.POSITIVE)) {
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(-1, id, 0, null, -1)); // id >= 1
            } else if (sign.equals(ExtendedSigns.NEGATIVE)) {
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(1, id, 0, null, -1)); // id <= -1
            } else if (sign.equals(ExtendedSigns.GREATER_OR_EQUAL_ZERO)) {
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(-1, id, 0, null, 0)); // id >= 0
            } else if (sign.equals(ExtendedSigns.LESS_OR_EQUAL_ZERO)) {
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(1, id, 0, null, 0)); // id <= 0
            }
        }
        newRight = new TwoVarsLinearInequality(newConstraints);

        // Raffine ExtendedSigns depuis TwoVarsLinearInequality
        ValueEnvironment<ExtendedSigns> refinedLeft = newLeft;
        for (Identifier id : newLeft.getKeys()) {
            ExtendedSigns currentSign = newLeft.getState(id);
            Set<TwoVarsLinearInequality.TwoVarsInequality> constraints = newRight.constraints;

            int upperBound = Integer.MAX_VALUE;
            int lowerBound = Integer.MIN_VALUE;
            for (TwoVarsLinearInequality.TwoVarsInequality c : constraints) {
                if (c.x != null && c.x.equals(id) && c.y == null) {
                    if (c.a == 1) {
                        upperBound = Math.min(upperBound, c.c);
                    } else if (c.a == -1) {
                        lowerBound = Math.max(lowerBound, -c.c);
                    }
                }
            }

            ExtendedSigns refinedSign = currentSign;
            if (upperBound == 0 && lowerBound == 0) {
                refinedSign = ExtendedSigns.ZERO;
            } else if (lowerBound >= 1 && upperBound >= 1 && !currentSign.equals(ExtendedSigns.POSITIVE) && !currentSign.isTop()) {
                refinedSign = ExtendedSigns.POSITIVE;
            } else if (upperBound <= -1 && lowerBound <= -1 && !currentSign.equals(ExtendedSigns.NEGATIVE) && !currentSign.isTop()) {
                refinedSign = ExtendedSigns.NEGATIVE;
            } else if (lowerBound >= 0 && upperBound >= 0 && !currentSign.equals(ExtendedSigns.ZERO) &&
                       !currentSign.equals(ExtendedSigns.POSITIVE) && !currentSign.equals(ExtendedSigns.GREATER_OR_EQUAL_ZERO) &&
                       !currentSign.isTop()) {
                refinedSign = ExtendedSigns.GREATER_OR_EQUAL_ZERO;
            } else if (upperBound <= 0 && lowerBound <= 0 && !currentSign.equals(ExtendedSigns.ZERO) &&
                       !currentSign.equals(ExtendedSigns.NEGATIVE) && !currentSign.equals(ExtendedSigns.LESS_OR_EQUAL_ZERO) &&
                       !currentSign.isTop()) {
                refinedSign = ExtendedSigns.LESS_OR_EQUAL_ZERO;
            }

            if (!refinedSign.equals(currentSign)) {
                refinedLeft = refinedLeft.putState(id, refinedSign);
            }
        }

        return new ExtendedSignsTVPIProductDomain(refinedLeft, newRight);
    }
}