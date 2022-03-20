package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Operator;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;


public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {
    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);
    private static final ExtSignDomain ZM = new ExtSignDomain(ExtSignDomain.ExtSign.ZM);
    private static final ExtSignDomain ZP = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);
    enum ExtSign {
        BOTTOM, MINUS, ZERO, PLUS,ZM,ZP,TOP;
    }

    private final ExtSignDomain.ExtSign sign;

    public ExtSignDomain() {
        this(ExtSignDomain.ExtSign.TOP);
    }

    public ExtSignDomain(ExtSignDomain.ExtSign sign) {
        this.sign = sign;
    }
    @Override
    public int hashCode() {
        return Objects.hash(sign);
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
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        switch (this.sign){
            case ZERO:
                switch (other.sign){
                   /* case ZERO:
                        return this; // o top*/
                    case MINUS:
                        return ZM;
                    case PLUS:
                        return ZP;
                    default: return TOP;
                }
            case MINUS:
                switch (other.sign){
                    case ZERO:
                        return ZM;
                    case MINUS:
                        return this; // o ZM*/
                    /*case PLUS:
                        return TOP;*/
                    case ZM:
                        return other;
                    /*case ZP:*/
                    default: return TOP;
                }
            case PLUS:
                switch (other.sign){
                    case ZERO:
                        return ZP;
                    //case MINUS:
                   /* case PLUS:
                        return this; */
                   // case ZM:
                    case ZP:
                        return other;
                    default: return TOP;
                }
            case ZM:
                switch (other.sign){
                    case ZERO:
                        return this; // o top
                    case MINUS:
                        return this;
                   // case PLUS:
                   /* case ZM:
                        return this; //o top*/
                   // case ZP:
                    default: return TOP;
                }
            case ZP:
                switch (other.sign){
                    case ZERO:
                        return this;
                   // case MINUS:
                    case PLUS:
                        return this;
                   // case ZM:
                   /* case ZP:
                        return this;*/
                    default: return TOP;
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
        switch (this.sign){
            case ZERO:
                switch (other.sign){
                    case ZERO:
                        return true;
                    case ZM:
                        return true;
                    case ZP:
                        return true;
                    default: return false;
                }
            case MINUS:
                switch (other.sign){
                    case MINUS:
                        return true;
                    case ZM:
                        return true;
                    default: return false;
                }
            case PLUS:
                switch (other.sign){
                    case PLUS:
                        return true;
                    case ZP:
                        return true;
                    default: return false;
                }

            default: return false;

        }
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
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException{
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(ExtSignDomain.ExtSign.PLUS);
            else if (v == 0)
                return new ExtSignDomain(ExtSignDomain.ExtSign.ZERO);
            else
                return new ExtSignDomain(ExtSignDomain.ExtSign.MINUS);
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (sign == ExtSignDomain.ExtSign.MINUS)
            return new ExtSignDomain(ExtSignDomain.ExtSign.PLUS);
        else if (sign == ExtSignDomain.ExtSign.PLUS)
            return new ExtSignDomain(ExtSignDomain.ExtSign.MINUS);
        else if (sign == ExtSignDomain.ExtSign.ZM)
            return new ExtSignDomain(ExtSignDomain.ExtSign.ZP);
        else if (sign == ExtSignDomain.ExtSign.ZP)
            return new ExtSignDomain(ExtSignDomain.ExtSign.ZM);
        return this;
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();

        return top();
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp)
            throws SemanticException{
        // +
        if(operator instanceof AdditionOperator) {
            switch (left.sign) {
                case ZERO:
                    return right;
                case MINUS:
                    switch (right.sign){
                        case ZERO:
                            return left;
                        case MINUS:
                            return left;
                        case ZM:
                            return right;
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        //case MINUS:
                        case PLUS:
                            return left;
                        //case ZM:
                        case ZP:
                            return right;
                        default:
                            return TOP;
                    }
                case ZM:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        case MINUS:
                            return right;
                      //  case PLUS:
                        case ZM:
                            return left;
                       // case ZP:
                        default:
                            return TOP;
                    }
                case ZP:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        //case MINUS:
                        case PLUS:
                            return right;
                        //case ZM:
                        case ZP:
                            return left;
                        default:
                            return TOP;
                    }
               // case TOP:
                default:
                    return TOP;
            }
        }
        // -
        if(operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case ZERO:
                    return right.negate();
                case MINUS:
                    switch (right.sign){
                        case ZERO:
                            return left;
                       // case MINUS:
                        case PLUS:
                            return left;
                        //case ZM:
                        case ZP:
                            return left;
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        case MINUS:
                            return left;
                       // case PLUS:
                        case ZM:
                            return left;
                        //case ZP:
                        default:
                            return TOP;
                    }
                case ZM:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        //case MINUS:
                        case PLUS:
                            return right.negate();
                       // case ZM:
                        case ZP:
                            return left;
                        default:
                            return TOP;
                    }
                case ZP:
                    switch(right.sign){
                        case ZERO:
                            return left;
                        case MINUS:
                            return right.negate();
                        //case PLUS:
                        case ZM:
                            return left;
                      //  case ZP:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        }

        if(operator instanceof Multiplication || operator instanceof DivisionOperator) {
            if(operator instanceof Multiplication && right.sign==ExtSign.ZERO) return right;

            if(operator instanceof DivisionOperator && left.sign==ExtSign.ZERO && !(right.sign==ExtSign.ZERO)) return left;

            if(operator instanceof DivisionOperator && right.sign==ExtSign.ZERO) return BOTTOM;

            switch (left.sign) {
                case ZERO:
                    return left;

                case MINUS:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case PLUS:
                            return left;
                        case ZM:
                            return right.negate();
                        case ZP:
                            return right.negate();
                        default:
                            return TOP;
                    }

                case PLUS:
                    switch(right.sign){
                        case MINUS:
                            return right;
                        case PLUS:
                            return left;
                        case ZM:
                            return right;
                        case ZP:
                            return right;
                        default:
                            return TOP;
                    }
                case ZM:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case PLUS:
                            return left;
                        case ZM:
                            return left.negate();
                        case ZP:
                            return left;
                        default:
                            return TOP;
                    }
                case ZP:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case PLUS:
                            return left;
                        case ZM:
                            return right;
                        case ZP:
                            return left;
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        }

        return TOP;
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
