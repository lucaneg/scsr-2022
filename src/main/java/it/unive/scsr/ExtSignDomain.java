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

import static it.unive.scsr.ExtSignDomain.ExtSign.*;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    // Creating just two single instances of TOP and BOTTOM (Singleton pattern)
    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);

    enum ExtSign {
        BOTTOM, MINUS, PLUS, ZERO, ZEROPLUS, ZEROMINUS, TOP
    }

    private final ExtSign sign;

    public ExtSignDomain(ExtSign sign) {
        this.sign = sign;
    }

    public ExtSignDomain() {
        this(null);
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        switch(this.sign){
            case MINUS:
                switch(other.sign){
                    case ZERO:
                    case ZEROMINUS:
                        return new ExtSignDomain(ExtSign.ZEROMINUS);
                    case PLUS:
                    case ZEROPLUS:
                    default:
                        return TOP;
                }
            case ZERO:
                switch(other.sign){
                    case ZEROMINUS:
                    case MINUS:
                        return new ExtSignDomain(ExtSign.ZEROMINUS);
                    case ZEROPLUS:
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZEROPLUS);
                    default: 
                        return TOP;
                }
            case PLUS:
                switch(other.sign){
                    case ZEROPLUS:
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZEROPLUS);
                    case MINUS:
                    case ZEROMINUS:
                    default:
                        return TOP;
                }
            case ZEROMINUS:
                switch(other.sign){
                    case ZERO:
                    case MINUS:
                        return new ExtSignDomain(ExtSign.ZEROMINUS);
                    case PLUS:
                    case ZEROPLUS:
                    default:
                        return TOP;
                }
            case ZEROPLUS:
                switch(other.sign) {
                    case ZERO:
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZEROPLUS);
                    case MINUS:
                    case ZEROMINUS:
                    default:
                        return TOP;
                }
            default: return TOP;
        }
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return (this.sign == MINUS && other.sign == ZEROMINUS) || // Minus
                (this.sign == ZERO && other.sign == ZEROMINUS) || // ZERO
                (this.sign == ZERO && other.sign == ExtSign.ZEROPLUS) || // ZERO
                (this.sign == PLUS && other.sign == ZEROPLUS); //PLUS
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtSignDomain)) return false;
        ExtSignDomain that = (ExtSignDomain) o;
        return sign == that.sign;
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

    private ExtSignDomain negate() {
        if (sign == ExtSign.MINUS)
            return new ExtSignDomain(ExtSign.PLUS);
        else if (sign == ExtSign.PLUS)
            return new ExtSignDomain(ExtSign.MINUS);
        else if (sign == ZEROPLUS)
            return new ExtSignDomain(ZEROMINUS);
        else if (sign == ZEROMINUS)
            return new ExtSignDomain(ZEROPLUS);
        else
            return this;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(PLUS);
            else if (v == 0)
                return new ExtSignDomain(ZERO);
            else
                return new ExtSignDomain(MINUS);
        }
        return top();
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        // We evaluate just this case since we are considering just the integer numbers
        if (operator instanceof NumericNegation)
            return arg.negate();

        return top();
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case MINUS:
                        case ZEROMINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case PLUS:
                        case ZERO:
                        case ZEROPLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign){
                        case ZEROMINUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return right;
                        case ZEROPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROMINUS:
                    switch (right.sign) {
                        case ZEROPLUS:
                        case ZERO:
                            return left;
                        case PLUS:
                            return right;
                        case ZEROMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZERO:
                    return right;
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case PLUS:
                        case ZEROPLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO:
                            return left;
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROMINUS:
                    switch (right.sign) {
                        case ZEROPLUS:
                        case ZERO:
                            return left;
                        case PLUS:
                            return new ExtSignDomain(MINUS);
                        case ZEROMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign) {
                        case ZEROMINUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(PLUS);
                        case ZEROPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZERO:
                    return right.negate();
                default:
                    return TOP;
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return TOP;
                case ZEROMINUS:
                    switch (right.sign){
                        case ZEROPLUS:
                        case PLUS:
                            return left;
                        case ZEROMINUS:
                        case MINUS:
                            return new ExtSignDomain(ZEROPLUS);
                        case ZERO:
                            return right;
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign){
                        case ZEROPLUS:
                        case PLUS:
                            return left;
                        case ZEROMINUS:
                        case ZERO:
                            return right;
                        case MINUS:
                            return new ExtSignDomain(ZEROMINUS);
                        case TOP:
                        default:
                            return TOP;

                    }
                case ZERO:
                    return new ExtSignDomain(ZERO);
                default:
                    return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == ZERO ||
                    right.sign == ZEROPLUS ||
                    right.sign == ZEROMINUS)
                return BOTTOM;

            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case ZEROMINUS:
                    switch (right.sign){
                        case PLUS:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(ZEROPLUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign){
                        case PLUS:
                            return left;
                        case MINUS:
                            return new ExtSignDomain(ZEROMINUS);
                        case TOP:
                        default:
                            return TOP;

                    }
                case TOP:
                    return TOP;
                case ZERO:
                    return new ExtSignDomain(ZERO);
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
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}
}
