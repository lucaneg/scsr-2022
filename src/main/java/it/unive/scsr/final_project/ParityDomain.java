package it.unive.scsr.final_project;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Module;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ParityDomain extends BaseNonRelationalValueDomain<ParityDomain> {

    public static final ParityDomain EVEN = new ParityDomain((byte) 3);
    public static final ParityDomain ODD = new ParityDomain((byte) 2);
    public static final ParityDomain TOP = new ParityDomain((byte) 0);
    public static final ParityDomain BOTTOM = new ParityDomain((byte) 1);

    private final byte parity;

    /**
     * Builds the parity abstract domain, representing the top of the parity
     * abstract domain.
     */
    public ParityDomain() {
        this((byte) 0);
    }

    public ParityDomain(byte parity) {
        this.parity = parity;
    }

    @Override
    public ParityDomain top() {
        return TOP;
    }

    @Override
    public boolean isTop() {
        return equals(TOP);
    }

    @Override
    public boolean isBottom() {
        return equals(BOTTOM);
    }

    @Override
    public ParityDomain bottom() {
        return BOTTOM;
    }

    @Override
    public DomainRepresentation representation() {
        if (isBottom())
            return Lattice.BOTTOM_REPR;
        if (isTop())
            return Lattice.TOP_REPR;
        String repr;
        if (this == EVEN)
            repr = "Even";
        else
            repr = "Odd";

        return new StringRepresentation(repr);
    }

    @Override
    protected ParityDomain evalNullConstant(ProgramPoint pp) {
        return top();
    }

    @Override
    protected ParityDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            Integer i = (Integer) constant.getValue();
            return i % 2 == 0 ? EVEN : ODD;
        }
        return top();
    }

    public boolean isEven() {
        return this.equals(EVEN);
    }

    public boolean isOdd() {
        return this.equals(ODD);
    }

    public static ParityDomain fromInt(Integer x) {
        return x % 2 == 0 ? EVEN : ODD;
    }

    public static ParityDomain oppositeFromInt(Integer x) {
        return x % 2 == 0 ? ODD : EVEN;
    }

    @Override
    protected ParityDomain evalUnaryExpression(UnaryOperator operator, ParityDomain arg, ProgramPoint pp) {
        if (operator == NumericNegation.INSTANCE)
            return arg;
        return top();
    }

    @Override
    protected ParityDomain evalBinaryExpression(BinaryOperator operator, ParityDomain left, ParityDomain right, ProgramPoint pp) {
        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator)
            if (left.equals(TOP) || right.equals(TOP))
                return TOP;
            else if (right.equals(left))
                return EVEN;
            else
                return ODD;
        else if (operator instanceof Multiplication)
            if (left.equals(EVEN) || right.equals(EVEN))
                return EVEN;
            else if (left.equals(TOP) || right.equals(TOP))
                return TOP;
            else
                return ODD;
        else if (operator instanceof DivisionOperator)
            if (left.equals(TOP) || right.equals(TOP))
                return TOP;
            else if (left.equals(ODD))
                return right.equals(ODD) ? ODD : EVEN;
            else
                return right.equals(ODD) ? EVEN : TOP;
        else if (operator instanceof Module)
            // even even -> even
            // even odd -> odd 12 % 7 = 7
            // odd even -> odd 11 % 6 = 5
            // odd odd -> top 27 % 11 = 5 and 101 % 95 = 6
            if (right.equals(EVEN))
                return left;
            else if (left.equals(EVEN))
                return right;
            else
                return TOP;
        return TOP;
    }

    @Override
    protected ParityDomain lubAux(ParityDomain other) {
        return TOP;
    }

    @Override
    protected ParityDomain glbAux(ParityDomain other) {
        return BOTTOM;
    }

    @Override
    protected ParityDomain wideningAux(ParityDomain other) {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ParityDomain other) {
        return false;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesUnaryExpression(UnaryOperator operator, ParityDomain arg, ProgramPoint pp) {
        if (arg.equals(TOP))
            return SemanticDomain.Satisfiability.UNKNOWN;
        if (arg.equals(BOTTOM))
            return SemanticDomain.Satisfiability.NOT_SATISFIED;

        // -OOD -> ODD  |    -EVEN -> EVEN
        if (operator == NumericNegation.INSTANCE)
            return SemanticDomain.Satisfiability.SATISFIED;

        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ParityDomain left, ParityDomain right, ProgramPoint pp) {
        if (left.isTop() || right.isTop())
            return SemanticDomain.Satisfiability.UNKNOWN;

        if (operator == ComparisonEq.INSTANCE) // ==
            return (left.equals(right)) ? SemanticDomain.Satisfiability.SATISFIED : SemanticDomain.Satisfiability.NOT_SATISFIED;
        if (operator == ComparisonGe.INSTANCE || operator == ComparisonLe.INSTANCE) // >= <=
            return SemanticDomain.Satisfiability.UNKNOWN; // ODD >= ODD (1 >= 3   3 >= 3)       ODD >= EVEN (7>= 2   1>=2)
        if (operator == ComparisonGt.INSTANCE || operator == ComparisonLt.INSTANCE) // > <
            return SemanticDomain.Satisfiability.UNKNOWN;
        if (operator == ComparisonNe.INSTANCE) // !=
            return (left.equals(right)) ? SemanticDomain.Satisfiability.NOT_SATISFIED : SemanticDomain.Satisfiability.SATISFIED;

        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected ValueEnvironment<ParityDomain> assumeBinaryExpression(
            ValueEnvironment<ParityDomain> environment, BinaryOperator operator, ValueExpression left,
            ValueExpression right, ProgramPoint pp) throws SemanticException {

        // ==                   -> only one to handle
        // <, >, <=, >=, !=     -> cannot infer anything about the identifier (i.e. <(i, 10) -> i=?)

        if (operator == ComparisonEq.INSTANCE) {
            if (left instanceof Identifier)
                environment = environment.assign((Identifier) left, right, pp);
            else if (right instanceof Identifier)
                environment = environment.assign((Identifier) right, left, pp);
        }

        if (operator == ComparisonNe.INSTANCE) { // left != right
            if (left instanceof Identifier) { // i != c
                ParityDomain parityDomain = eval(right, environment, pp);
                if (parityDomain.equals(EVEN))
                    environment.assign((Identifier) left, new Constant(right.getStaticType(), ODD, right.getCodeLocation()), pp);
                else
                    environment.assign((Identifier) left, new Constant(right.getStaticType(), EVEN, right.getCodeLocation()), pp);
            } else if (right instanceof Identifier) { // c != i
                ParityDomain parityDomain = eval(left, environment, pp);
                if (parityDomain.equals(EVEN))
                    environment.assign((Identifier) right, new Constant(left.getStaticType(), ODD, left.getCodeLocation()), pp);
                else
                    environment.assign((Identifier) right, new Constant(left.getStaticType(), EVEN, left.getCodeLocation()), pp);
            }
        }

        return environment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParityDomain that = (ParityDomain) o;
        return parity == that.parity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parity);
    }
}