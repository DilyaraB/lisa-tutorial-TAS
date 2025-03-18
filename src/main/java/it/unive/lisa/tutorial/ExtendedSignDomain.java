package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignDomain implements BaseNonRelationalValueDomain<ExtendedSignDomain> {

    private final Sign sign;

    public static final ExtendedSignDomain BOTTOM = new ExtendedSignDomain(Sign.BOTTOM);
    public static final ExtendedSignDomain TOP = new ExtendedSignDomain(Sign.TOP);
    public static final ExtendedSignDomain ZERO = new ExtendedSignDomain(Sign.ZERO);
    public static final ExtendedSignDomain POSITIVE = new ExtendedSignDomain(Sign.POSITIVE);
    public static final ExtendedSignDomain NEGATIVE = new ExtendedSignDomain(Sign.NEGATIVE);
    public static final ExtendedSignDomain ZERO_NEGATIVE = new ExtendedSignDomain(Sign.ZERO_NEGATIVE);
    public static final ExtendedSignDomain ZERO_POSITIVE = new ExtendedSignDomain(Sign.ZERO_POSITIVE);

    public static ExtendedSignDomain getInstance(Sign sign) {
        switch (sign) {
            case TOP: return TOP;
            case BOTTOM: return BOTTOM;
            case ZERO: return ZERO;
            case POSITIVE: return POSITIVE;
            case NEGATIVE: return NEGATIVE;
            case ZERO_NEGATIVE: return ZERO_NEGATIVE;
            case ZERO_POSITIVE: return ZERO_POSITIVE;
            default: throw new IllegalArgumentException("Invalid sign: " + sign);
        }
    }

    public ExtendedSignDomain(Sign sign) {
        this.sign = sign;
    }

    @Override
    public ExtendedSignDomain lubAux(ExtendedSignDomain other) {
        return getInstance(Sign.lub(this.sign, other.sign));
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignDomain other) {
        return this.sign.isSubsetOf(other.sign);
    }

    @Override
    public ExtendedSignDomain top() {
        return TOP;
    }

    @Override
    public ExtendedSignDomain bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        return new StringRepresentation(this.sign.toString());
    }

    @Override
    public ExtendedSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) {
        if (constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if (value > 0) return POSITIVE;
            if (value < 0) return NEGATIVE;
            return ZERO;
        }
        return TOP;
    }

    @Override
        public ExtendedSignDomain evalBinaryExpression(BinaryOperator operator, ExtendedSignDomain left, ExtendedSignDomain right, ProgramPoint pp, SemanticOracle oracle) {
            if (left == TOP || right == TOP) return TOP;
            if (left == BOTTOM || right == BOTTOM) return BOTTOM;

            //Gestion de l'addition
            if (operator instanceof AdditionOperator) {
                if (left == ZERO_NEGATIVE && right == ZERO_NEGATIVE ) {
                    return ZERO_NEGATIVE ;
                }
                else if (left == NEGATIVE || left == ZERO_NEGATIVE) {
                    if (right == NEGATIVE || right == ZERO_NEGATIVE || right == ZERO ) return NEGATIVE;
                    return TOP;
                } else if (left == ZERO_POSITIVE && right == ZERO_POSITIVE ) {
                    return ZERO_POSITIVE ;
                }else if (left == POSITIVE || left == ZERO_POSITIVE) {
                    if (right == POSITIVE || right == ZERO_POSITIVE || right == ZERO) return POSITIVE;
                    return TOP;
                } else if (left == ZERO) {
                    return right;
                }
                return TOP;
            }

            //Gestion de la soustraction
            if (operator instanceof SubtractionOperator) {
                if (left == NEGATIVE) {
                    if (right == POSITIVE || right == ZERO_POSITIVE || right == ZERO) return NEGATIVE;
                    return TOP;
                } else if (left == POSITIVE) {
                    if (right == NEGATIVE || right == ZERO_NEGATIVE || right == ZERO) return POSITIVE;
                    return TOP;
                } else if (left == ZERO) {
                    return right.negate();
                } else if (left == ZERO_NEGATIVE) {
                    if (right == ZERO_POSITIVE) return ZERO_NEGATIVE;
                    return right.negate();
                } else if (left == ZERO_POSITIVE) {
                    if (right == ZERO_NEGATIVE) return ZERO_POSITIVE;
                    return right.negate();
                }
                return TOP;
            }

        //Gestion de la multiplication (*) et Gestion de la division (/)
        if (operator instanceof MultiplicationOperator || operator instanceof DivisionOperator) {
            if (right == ZERO && operator instanceof DivisionOperator) return BOTTOM;
            if (left == ZERO || right == ZERO) return ZERO;
            if (left == POSITIVE) return right;
            if (right == POSITIVE) return left;
            if (left == NEGATIVE) return right.negate();
            if (right == NEGATIVE) return left.negate();
            //0 * -9 = -9; -9 * -1 = 9; -9 * 0 = 0; -9 * 1= -9;
            if (left == ZERO_NEGATIVE) {
                if (right == ZERO_NEGATIVE) return ZERO_POSITIVE;
                return ZERO_NEGATIVE;
            }
            if (left == ZERO_POSITIVE) {
                if (right == ZERO_NEGATIVE) return ZERO_NEGATIVE;
                return ZERO_POSITIVE;
            }
            return TOP;
        }

        return TOP;
    }

    public ExtendedSignDomain negate() {
        switch (this.sign) {
            case POSITIVE:
                return NEGATIVE;
            case NEGATIVE:
                return POSITIVE;
            case ZERO_NEGATIVE:
                return ZERO_POSITIVE;
            case ZERO_POSITIVE:
                return ZERO_NEGATIVE;
            default:
                return this;
        }
    }

    public enum Sign {
        TOP,
        ZERO, // 0
        POSITIVE, // > 0
        NEGATIVE, // < 0
        ZERO_NEGATIVE, // ≤ 0
        ZERO_POSITIVE, // ≥ 0
        BOTTOM;

        // Déterminer si un élément est inclus dans un autre
        public boolean isSubsetOf(Sign other) {
            if (this == other || other == TOP) return true;
            if (this == BOTTOM) return true;
            if (this == ZERO_NEGATIVE && (other == NEGATIVE || other == ZERO)) return true;
            if (this == ZERO_POSITIVE && (other == POSITIVE || other == ZERO)) return true;
            return this == TOP && (other == POSITIVE || other == NEGATIVE);
        }

        // least upper bound
        public static Sign lub(Sign a, Sign b) {
            if (a == b) return a;
            if (a == BOTTOM) return b;
            if (b == BOTTOM) return a;
            if (a == TOP || b == TOP) return TOP;
            if ((a == NEGATIVE && b == ZERO) || (a == ZERO && b == NEGATIVE)) return ZERO_NEGATIVE;
            if ((a == POSITIVE && b == ZERO) || (a == ZERO && b == POSITIVE)) return ZERO_POSITIVE;
            return TOP;
        }

        public static Sign multiply(Sign a, Sign b) {
            if (a == BOTTOM || b == BOTTOM) return BOTTOM;
            if (a == ZERO || b == ZERO) return ZERO;
            if (a == POSITIVE) return b;
            if (b == POSITIVE) return a;
            if (a == NEGATIVE && b == NEGATIVE) return POSITIVE;
            return TOP;
        }
    }
}
