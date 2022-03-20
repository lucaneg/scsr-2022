package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.Sign.TOP);
    /*
    private static final ExtSignDomain OPLUS = new ExtSignDomain(ExtSignDomain.Sign.OPLUS);
    private static final ExtSignDomain OMINUS = new ExtSignDomain(ExtSignDomain.Sign.OMINUS);
    private static final ExtSignDomain PLUS= new ExtSignDomain(ExtSignDomain.Sign.PLUS);
    private static final ExtSignDomain MINUS= new ExtSignDomain(ExtSignDomain.Sign.MINUS);*/
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.Sign.BOTTOM);

    enum Sign {
        BOTTOM, MINUS, ZERO, PLUS, OMINUS, OPLUS, TOP
    }

    private final ExtSignDomain.Sign sign;

    // constructor initializes at TOP
    public ExtSignDomain() {
        this(ExtSignDomain.Sign.TOP);
    }

    public ExtSignDomain(ExtSignDomain.Sign sign) {
        this.sign = sign;
    }

    // lub btw two elements, knowing: other is not null, other and this are neither top nor bottom, this and other are not the same object
    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if( (this.sign == Sign.MINUS || this.sign == Sign.ZERO || this.sign == Sign.OMINUS) &&
                (other.sign == Sign.MINUS || other.sign == Sign.ZERO || other.sign == Sign.OMINUS))
            return new ExtSignDomain(ExtSignDomain.Sign.OMINUS);
        else if( (this.sign == Sign.ZERO || this.sign == Sign.PLUS || this.sign == Sign.OPLUS) &&
                (other.sign == Sign.ZERO || other.sign == Sign.PLUS || other.sign == Sign.OPLUS))
            return new ExtSignDomain(ExtSignDomain.Sign.OPLUS);
        else return TOP;
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return TOP;
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return (other.sign == Sign.OMINUS && (this.sign == Sign.MINUS || this.sign == Sign.ZERO)) ||
                (other.sign == Sign.OPLUS && (this.sign == Sign.PLUS || this.sign == Sign.ZERO));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtSignDomain other = (ExtSignDomain) obj;
        return sign == other.sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign);
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(ExtSignDomain.Sign.PLUS);
            else if (v == 0)
                return new ExtSignDomain(ExtSignDomain.Sign.ZERO);
            else
                return new ExtSignDomain(ExtSignDomain.Sign.MINUS);
        }
        else return top();
    }

    private ExtSignDomain negate() {
        if (sign == Sign.MINUS)
            return new ExtSignDomain(ExtSignDomain.Sign.PLUS);
        else if (sign == Sign.PLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.MINUS);
        else if (sign == Sign.OMINUS)
            return new ExtSignDomain(ExtSignDomain.Sign.OPLUS);
        else if (sign == Sign.OPLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.OMINUS);
        else
            return this;
    }


    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();
        else return top();
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp)
            throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case MINUS:
                        case OMINUS:
                            return left;
                        case PLUS:
                        case OPLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case OPLUS:
                        case PLUS:
                        case ZERO:
                            return left;
                        case OMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case OMINUS:
                    switch (right.sign){
                        case OMINUS: // adding (0 or -) to (0 or -) gets still (0 or -)
                        case ZERO:
                            return left;
                        case MINUS:
                            return right; // adding (-) to (0 or -) gets (-)
                        case OPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case OPLUS:
                    switch (right.sign) {
                        case OPLUS:
                        case ZERO:
                            return left;
                        case PLUS:
                            return right;
                        case OMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    return right;
                case TOP:
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case PLUS:
                        case OPLUS:
                            return left;
                        case OMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case OMINUS:
                        case MINUS:
                        case ZERO:
                            return left;
                        case OPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case OMINUS:
                    switch (right.sign) {
                        case OPLUS:
                        case ZERO:
                            return left;
                        case PLUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.MINUS);
                        case OMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case OPLUS:
                    switch (right.sign) {
                        case OMINUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.PLUS);
                        case OPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    return right.negate();
                case TOP:
                default:
                    return TOP;
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case OMINUS:
                    switch (right.sign){
                        case OPLUS:
                        case PLUS:
                            return left;
                        case OMINUS:
                        case MINUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.OPLUS);
                        case ZERO:
                            return right;
                        case TOP:
                        default:
                            return TOP;
                    }
                case OPLUS:
                    switch (right.sign){
                        case OPLUS:
                        case PLUS:
                            return left;
                        case OMINUS:
                        case ZERO:
                            return right;
                        case MINUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.OMINUS);
                        case TOP:
                        default:
                            return TOP;

                    }
                case ZERO:
                    return left;
                case TOP:
                default:
                    return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == ExtSignDomain.Sign.ZERO || right.sign == ExtSignDomain.Sign.OPLUS || right.sign == Sign.OMINUS)
                return BOTTOM;

            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case OMINUS:
                    switch (right.sign){
                        case PLUS:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.OPLUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case OPLUS:
                    switch (right.sign){
                        case PLUS:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(ExtSignDomain.Sign.OMINUS);
                        case TOP:
                        default:
                            return TOP;

                    }
                case ZERO:
                    return new ExtSignDomain(ExtSignDomain.Sign.ZERO);
                case TOP:
                default:
                    return TOP;
            }
        }

        return top();
    }

    // code outside the scope of the course
    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(sign);
    }
}
