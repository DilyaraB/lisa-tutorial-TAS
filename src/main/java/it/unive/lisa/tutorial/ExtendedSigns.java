package it.unive.lisa.tutorial;

import java.util.Objects;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSigns implements BaseNonRelationalValueDomain<ExtendedSigns> {

  private static final ExtendedSigns BOTTOM = new ExtendedSigns(-10);
  private static final ExtendedSigns NEGATIVE = new ExtendedSigns(-1);
  private static final ExtendedSigns ZERO = new ExtendedSigns(0);
  private static final ExtendedSigns POSITIVE = new ExtendedSigns(1);
  private static final ExtendedSigns LESS_OR_EQUAL_ZERO = new ExtendedSigns(-2);
  private static final ExtendedSigns GREATER_OR_EQUAL_ZERO = new ExtendedSigns(2);
  private static final ExtendedSigns TOP = new ExtendedSigns(10);

  private final int extendedSign;

  public ExtendedSigns() {
    this(10);
  }

  public ExtendedSigns(int sign) {
    this.extendedSign = sign;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ExtendedSigns extendedSigns = (ExtendedSigns) o;
    return extendedSign == extendedSigns.extendedSign;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(extendedSign);
  }

  @Override
  public ExtendedSigns top() {
    return TOP;
  }

  @Override
  public ExtendedSigns bottom() {
    return BOTTOM;
  }

  @Override
  public boolean lessOrEqualAux(ExtendedSigns other) throws SemanticException {
    if (this == BOTTOM) return true;
    if (other == TOP) return true;    
    if (this == other) return true;  

    if (this == NEGATIVE && other == LESS_OR_EQUAL_ZERO) return true;
    if (this == POSITIVE && other == GREATER_OR_EQUAL_ZERO) return true;

    if (this == ZERO && other == LESS_OR_EQUAL_ZERO) return true;
    if (this == ZERO && other == GREATER_OR_EQUAL_ZERO) return true;

    return false;
  }

  @Override
  public ExtendedSigns lubAux(ExtendedSigns other) throws SemanticException {
    if (this == other) return this;
    if (this == BOTTOM) return other;
    if (other == BOTTOM) return this;
    if (this == TOP || other == TOP) return TOP;

    if ((this == POSITIVE && other == ZERO) || (this == ZERO && other == POSITIVE)) return GREATER_OR_EQUAL_ZERO;
    if ((this == NEGATIVE && other == ZERO) || (this == ZERO && other == NEGATIVE)) return LESS_OR_EQUAL_ZERO;
    if ((this == NEGATIVE && other == POSITIVE) || (this == POSITIVE && other == NEGATIVE)) return TOP;

    return TOP;
  }

  @Override
  public StructuredRepresentation representation() {
    if (this == TOP) return Lattice.topRepresentation();
    if (this == BOTTOM) return Lattice.bottomRepresentation();
    if (this == POSITIVE) return new StringRepresentation("+");
    if (this == NEGATIVE) return new StringRepresentation("-");
    if (this == ZERO) return new StringRepresentation("0");
    if (this == LESS_OR_EQUAL_ZERO) return new StringRepresentation("<=0");
    if (this == GREATER_OR_EQUAL_ZERO) return new StringRepresentation(">=0");
    return new StringRepresentation("unknown");
  }

  @Override
  public ExtendedSigns evalNonNullConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    if (constant.getValue() instanceof Integer) {
      int v = (Integer) constant.getValue();
      if (v > 0) return POSITIVE;
      else if (v == 0) return ZERO;
      else return NEGATIVE;
    }
    return top();
  }

  private ExtendedSigns negate() {
    if (this == NEGATIVE) return POSITIVE;
    if (this == POSITIVE) return NEGATIVE;
    if (this == ZERO) return ZERO;
    if (this == LESS_OR_EQUAL_ZERO) return GREATER_OR_EQUAL_ZERO;
    if (this == GREATER_OR_EQUAL_ZERO) return LESS_OR_EQUAL_ZERO;
    return this;
  }

  @Override
  public ExtendedSigns evalUnaryExpression(UnaryOperator operator, ExtendedSigns arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    if (operator instanceof NumericNegation)
      return arg.negate();
    return TOP;
  }

  @Override
  public ExtendedSigns evalBinaryExpression(BinaryOperator operator, ExtendedSigns left, ExtendedSigns right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    if (operator instanceof AdditionOperator) {
      if (left == ZERO) return right;
      if (right == ZERO) return left;
      if (left == POSITIVE && right == POSITIVE) return POSITIVE;
      if (left == NEGATIVE && right == NEGATIVE) return NEGATIVE;
      if ((left == GREATER_OR_EQUAL_ZERO && right == POSITIVE) || (left == POSITIVE && right == GREATER_OR_EQUAL_ZERO)) return POSITIVE;
      if (left == GREATER_OR_EQUAL_ZERO && right == GREATER_OR_EQUAL_ZERO) return GREATER_OR_EQUAL_ZERO;
      if ((left == LESS_OR_EQUAL_ZERO && right == NEGATIVE) || (left == NEGATIVE && right == LESS_OR_EQUAL_ZERO)) return NEGATIVE;
      if (left == LESS_OR_EQUAL_ZERO && right == LESS_OR_EQUAL_ZERO) return LESS_OR_EQUAL_ZERO;
      return TOP;

    } else if (operator instanceof SubtractionOperator) {
      if (left == ZERO) return right.negate();
      if (right == ZERO) return left;
      if (left == POSITIVE && right == NEGATIVE) return POSITIVE;
      if (left == POSITIVE && right == LESS_OR_EQUAL_ZERO) return POSITIVE;
      if (left == GREATER_OR_EQUAL_ZERO && right == NEGATIVE) return POSITIVE;
      if (left == GREATER_OR_EQUAL_ZERO && right == LESS_OR_EQUAL_ZERO) return GREATER_OR_EQUAL_ZERO;
      if (left == NEGATIVE && right == POSITIVE) return NEGATIVE;
      if (left == NEGATIVE && right == GREATER_OR_EQUAL_ZERO) return NEGATIVE;
      if (left == LESS_OR_EQUAL_ZERO && right == POSITIVE) return NEGATIVE;
      if (left == LESS_OR_EQUAL_ZERO && right == GREATER_OR_EQUAL_ZERO) return LESS_OR_EQUAL_ZERO;
      return TOP;

    } else if (operator instanceof MultiplicationOperator) {
      if (left == ZERO || right == ZERO) return ZERO;
      if (left == POSITIVE && right == POSITIVE) return POSITIVE;
      if (left == NEGATIVE && right == NEGATIVE) return POSITIVE;
      if ((left == POSITIVE && right == NEGATIVE) || (left == NEGATIVE && right == POSITIVE)) return NEGATIVE;
      if ((left == POSITIVE && right == GREATER_OR_EQUAL_ZERO) || (left == GREATER_OR_EQUAL_ZERO && right == POSITIVE)) return GREATER_OR_EQUAL_ZERO;
      if ((left == POSITIVE && right == LESS_OR_EQUAL_ZERO) || (left == LESS_OR_EQUAL_ZERO && right == POSITIVE)) return LESS_OR_EQUAL_ZERO;
      if ((left == NEGATIVE && right == GREATER_OR_EQUAL_ZERO) || (left == GREATER_OR_EQUAL_ZERO && right == NEGATIVE)) return LESS_OR_EQUAL_ZERO;
      if ((left == NEGATIVE && right == LESS_OR_EQUAL_ZERO) || (left == LESS_OR_EQUAL_ZERO && right == NEGATIVE)) return GREATER_OR_EQUAL_ZERO;
      if (left == GREATER_OR_EQUAL_ZERO && right == GREATER_OR_EQUAL_ZERO) return GREATER_OR_EQUAL_ZERO;
      if (left == LESS_OR_EQUAL_ZERO && right == LESS_OR_EQUAL_ZERO) return GREATER_OR_EQUAL_ZERO;
      return TOP;

    } else if (operator instanceof DivisionOperator) {
      if (right == ZERO) return BOTTOM;
      if (left == ZERO) return ZERO;
      if (left == POSITIVE && right == POSITIVE) return POSITIVE;
      if (left == NEGATIVE && right == NEGATIVE) return POSITIVE;
      if ((left == POSITIVE && right == NEGATIVE) || (left == NEGATIVE && right == POSITIVE)) return NEGATIVE;
      if (left == GREATER_OR_EQUAL_ZERO && right == POSITIVE) return GREATER_OR_EQUAL_ZERO;
      if (left == GREATER_OR_EQUAL_ZERO && right == NEGATIVE) return LESS_OR_EQUAL_ZERO;
      if (left == LESS_OR_EQUAL_ZERO && right == POSITIVE) return LESS_OR_EQUAL_ZERO;
      if (left == LESS_OR_EQUAL_ZERO && right == NEGATIVE) return GREATER_OR_EQUAL_ZERO;
      return TOP;
    }
    return TOP;
  }

  @Override
  public Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ExtendedSigns left, ExtendedSigns right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    System.out.println("-------in satisfiesBinaryExpression operator ----- " + operator);
    if (operator instanceof ComparisonEq) { // ==
      if (left == right) return Satisfiability.SATISFIED;
      if ((left == ZERO && right == POSITIVE) || (left == POSITIVE && right == ZERO)) return Satisfiability.NOT_SATISFIED;
      if ((left == POSITIVE && right == NEGATIVE) || (left == NEGATIVE && right == POSITIVE)) return Satisfiability.NOT_SATISFIED;
      return Satisfiability.UNKNOWN;
    } else if (operator instanceof ComparisonGt) { // >
      if (left == POSITIVE && right == ZERO) return Satisfiability.SATISFIED;
      if (left == POSITIVE && right == LESS_OR_EQUAL_ZERO) return Satisfiability.SATISFIED;
      if (left == POSITIVE && right == NEGATIVE) return Satisfiability.SATISFIED;
      if (left == GREATER_OR_EQUAL_ZERO && right == NEGATIVE) return Satisfiability.SATISFIED;
      if (left == ZERO && right == NEGATIVE) return Satisfiability.SATISFIED;
      if (left == ZERO && right == POSITIVE) return Satisfiability.NOT_SATISFIED;
      if (left == LESS_OR_EQUAL_ZERO && right == POSITIVE) return Satisfiability.NOT_SATISFIED;
      if (left == NEGATIVE && right == POSITIVE) return Satisfiability.NOT_SATISFIED;
      if (left == NEGATIVE && right == GREATER_OR_EQUAL_ZERO) return Satisfiability.NOT_SATISFIED;
      if (left == NEGATIVE && right == ZERO) return Satisfiability.NOT_SATISFIED;
      return Satisfiability.UNKNOWN;
    } else if (operator instanceof ComparisonLt) { // <
      if (left == NEGATIVE && right == POSITIVE) return Satisfiability.SATISFIED;
      if (left == NEGATIVE && right == GREATER_OR_EQUAL_ZERO) return Satisfiability.SATISFIED;
      if (left == NEGATIVE && right == ZERO) return Satisfiability.SATISFIED;
      if (left == LESS_OR_EQUAL_ZERO && right == POSITIVE) return Satisfiability.SATISFIED;
      if (left == ZERO && right == POSITIVE) return Satisfiability.SATISFIED;
      if (left == ZERO && right == NEGATIVE) return Satisfiability.NOT_SATISFIED;
      if (left == GREATER_OR_EQUAL_ZERO && right == NEGATIVE) return Satisfiability.NOT_SATISFIED;
      if (left == POSITIVE && right == NEGATIVE) return Satisfiability.NOT_SATISFIED;
      if (left == POSITIVE && right == ZERO) return Satisfiability.NOT_SATISFIED;
      if (left == POSITIVE && right == LESS_OR_EQUAL_ZERO) return Satisfiability.NOT_SATISFIED;
      return Satisfiability.UNKNOWN;
    }
    return Satisfiability.UNKNOWN;
  }

  @Override
  public ValueEnvironment<ExtendedSigns> assumeBinaryExpression(ValueEnvironment<ExtendedSigns> environment, BinaryOperator operator, ValueExpression left, ValueExpression right, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
    System.out.println("-------in assumeBinaryExpression operator ----- " + operator);
    // Cas où l'identifiant est à gauche
    if (left instanceof Identifier) {
      Identifier id = (Identifier) left;
      ExtendedSigns rightVal = eval(right, environment, src, oracle);

      if (operator instanceof ComparisonEq) { // x == right
        return environment.putState(id, rightVal);
      } else if (operator instanceof ComparisonGt) { // x > right
        if (rightVal == POSITIVE) return environment.putState(id, POSITIVE);
        if (rightVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, POSITIVE);
        if (rightVal == ZERO) return environment.putState(id, POSITIVE);
        if (rightVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (rightVal == NEGATIVE) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
      } else if (operator instanceof ComparisonLt) { // x < right
        if (rightVal == POSITIVE) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (rightVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (rightVal == ZERO) return environment.putState(id, NEGATIVE);
        if (rightVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, NEGATIVE);
        if (rightVal == NEGATIVE) return environment.putState(id, NEGATIVE);
      } else if (operator instanceof ComparisonGe) { // x >= right
        if (rightVal == POSITIVE) return environment.putState(id, POSITIVE);
        if (rightVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (rightVal == ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (rightVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, TOP);
        if (rightVal == NEGATIVE) return environment.putState(id, TOP);
      } else if (operator instanceof ComparisonLe) { // x <= right
        if (rightVal == POSITIVE) return environment.putState(id, TOP);
        if (rightVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, TOP);
        if (rightVal == ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (rightVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (rightVal == NEGATIVE) return environment.putState(id, NEGATIVE);
      }
    }

    // Cas où l'identifiant est à droite
    if (right instanceof Identifier) {
      Identifier id = (Identifier) right;
      ExtendedSigns leftVal = eval(left, environment, src, oracle);

      if (operator instanceof ComparisonEq) { // left == x
        return environment.putState(id, leftVal);
      } else if (operator instanceof ComparisonGt) { // left > x
        if (leftVal == POSITIVE) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (leftVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (leftVal == ZERO) return environment.putState(id, NEGATIVE);
        if (leftVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, NEGATIVE);
        if (leftVal == NEGATIVE) return environment.putState(id, NEGATIVE);
      } else if (operator instanceof ComparisonLt) { // left < x
        if (leftVal == POSITIVE) return environment.putState(id, POSITIVE);
        if (leftVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, POSITIVE);
        if (leftVal == ZERO) return environment.putState(id, POSITIVE);
        if (leftVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (leftVal == NEGATIVE) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
      } else if (operator instanceof ComparisonGe) { // left >= x
        if (leftVal == POSITIVE) return environment.putState(id, TOP);
        if (leftVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, TOP);
        if (leftVal == ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (leftVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, LESS_OR_EQUAL_ZERO);
        if (leftVal == NEGATIVE) return environment.putState(id, NEGATIVE);
      } else if (operator instanceof ComparisonLe) { // left <= x
        if (leftVal == POSITIVE) return environment.putState(id, POSITIVE);
        if (leftVal == GREATER_OR_EQUAL_ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (leftVal == ZERO) return environment.putState(id, GREATER_OR_EQUAL_ZERO);
        if (leftVal == LESS_OR_EQUAL_ZERO) return environment.putState(id, TOP);
        if (leftVal == NEGATIVE) return environment.putState(id, TOP);
      }
    }
    
    return environment;
  }

  public static ExtendedSigns getZero(){
    return ZERO;
  }

  public static ExtendedSigns getPositive(){
    return POSITIVE;
  }

  public static ExtendedSigns getNegative(){
    return NEGATIVE;
  }

  public static ExtendedSigns getLessOrEqualZero(){
    return LESS_OR_EQUAL_ZERO;
  }

  public static ExtendedSigns getGreaterOrEqualZero(){
    return GREATER_OR_EQUAL_ZERO;
  }
}
