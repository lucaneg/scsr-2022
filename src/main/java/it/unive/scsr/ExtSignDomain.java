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

import static it.unive.scsr.ExtSignDomain.ExtSignDom.*;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain>{

    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDom.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDom.BOTTOM);

    enum ExtSignDom {
        BOTTOM, MINUS, ZERO, PLUS, ZEROMINUS, ZEROPLUS, TOP
    }

    private final ExtSignDom extSign;

    public ExtSignDomain() {
        this(ExtSignDom.TOP);
    }

    public ExtSignDomain(ExtSignDom extSign){
        this.extSign = extSign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extSign);
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
        return extSign == other.extSign;
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
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if(this.extSign == MINUS){
            if(other.extSign == PLUS)
                return TOP;
            else if( other.extSign == ZERO)
                return new ExtSignDomain(ExtSignDom.ZEROMINUS);
        }
        if( (this.extSign == PLUS) && (other.extSign == ZERO) )
                return new ExtSignDomain(ExtSignDom.ZEROPLUS);
        return TOP;
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if( (this.extSign == MINUS)  && (other.extSign == ZEROMINUS) )
            return true;

        if( (this.extSign == PLUS) && (other.extSign == ZEROPLUS) )
            return true;

        if( (this.extSign == ZERO ) && (other.extSign == ZEROMINUS) )
            return true;

        if( (this.extSign == ZERO ) && (other.extSign == ZEROPLUS) )
            return true;

        return false;
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(extSign);
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(ExtSignDom.PLUS);
            else if (v == 0)
                return new ExtSignDomain(ExtSignDom.ZERO);
            else
                return new ExtSignDomain(ExtSignDom.MINUS);
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (extSign == MINUS)
            return new ExtSignDomain(ExtSignDom.PLUS);
        else if (extSign == ExtSignDom.PLUS)
            return new ExtSignDomain(ExtSignDom.MINUS);
        else if (extSign == ExtSignDom.ZEROMINUS)
            return new ExtSignDomain(ExtSignDom.ZEROMINUS);
        else if (extSign == ExtSignDom.ZEROPLUS)
            return new ExtSignDomain(ExtSignDom.ZEROPLUS);
        else
            return this;
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();
        return top();
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp)
            throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.extSign) {
                case MINUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ZEROMINUS);
                        case MINUS:
                        case PLUS:
                        case TOP:
                        case ZEROMINUS:
                            return left;
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                            return left;
                        default:
                            return TOP;
                    }
                case ZERO:
                    return right;
                case ZEROMINUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return right;
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.extSign) {
                case MINUS:
                    switch (right.extSign) {
                        case PLUS:
                            return left;
                        case ZERO:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return left.negate();
                        case TOP:
                        case ZEROMINUS:
                            return left;
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.extSign) {
                    case PLUS:
                    case ZERO:
                    case MINUS:
                    case TOP:
                    case ZEROMINUS:
                    case ZEROPLUS:
                    default:
                        return TOP;
                }
                case ZEROPLUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case ZEROMINUS:
                    switch (right.extSign) {
                        case PLUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(PLUS);
                        case TOP:
                        case ZEROMINUS:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof Multiplication) {
            switch (left.extSign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case ZERO:
                    return new ExtSignDomain(ExtSignDom.ZERO);
                case ZEROMINUS:
                case ZEROPLUS:
                default:
                    return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if( (right.extSign == ExtSignDomain.ExtSignDom.ZERO) ||
                    (right.extSign == ExtSignDomain.ExtSignDom.ZEROPLUS) ||
                    (right.extSign == ExtSignDom.ZEROMINUS))
                return BOTTOM;

            switch (left.extSign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case ZERO:
                    return new ExtSignDomain(ExtSignDom.ZERO);
                case ZEROMINUS:
                    return BOTTOM;
                case ZEROPLUS:
                    return BOTTOM;
                default:
                    return TOP;
            }
        }

        return top();
    }


    // IMPLEMENTATION NOTE:
    // the code below is outside of the scope of the course. You can uncomment it to get
    // your code to compile. Beware that the code is written expecting that a field named
    // "sign" containing an enumeration (similar to the one saw during class) exists in
    // this class: if you name it differently, change also the code below to make it work
    // by just using the name of your choice instead of "sign"

//	@Override
//	public DomainRepresentation representation() {
//		return new StringRepresentation(sign);
//	}
}
