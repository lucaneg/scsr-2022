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
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.Sign.BOTTOM);
    private static final ExtSignDomain ZEROMINUS = new ExtSignDomain(ExtSignDomain.Sign.ZMINUS);
    private static final ExtSignDomain ZEROPLUS = new ExtSignDomain(ExtSignDomain.Sign.ZPLUS);



    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    enum Sign {
        BOTTOM, MINUS, ZERO, PLUS, TOP, ZMINUS, ZPLUS;
    }
    private final ExtSignDomain.Sign sign;



    public ExtSignDomain() {
        this(ExtSignDomain.Sign.TOP);
    }

    public ExtSignDomain(ExtSignDomain.Sign sign) {
        this.sign = sign;
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        switch(this.sign){
            case MINUS:
                switch(other.sign){
                   /* case MINUS:
                        return this;*/
                    case ZERO:
                        return ZEROMINUS;
                    case PLUS:
                    case ZPLUS:
                        return TOP;
                    case ZMINUS:
                        return other;
                }
            case ZERO:
                switch(other.sign){
                    case MINUS:
                        return ZEROMINUS;
                 /*  case ZERO:
                       return this;*/
                    case PLUS:
                        return ZEROPLUS;
                    case ZMINUS:
                    case ZPLUS:
                        return other;
                }
            case PLUS:
                switch(other.sign){
                    case MINUS:
                    case ZMINUS:
                        return TOP;
                    case ZERO:
                        return ZEROPLUS;
                  /* case PLUS:
                       return this;*/
                    case ZPLUS:
                        return other;
                }
            case ZMINUS:
                switch(other.sign){
                    case MINUS:
                    case ZERO:
                   /*case ZMINUS:
                       return this;*/
                    case PLUS:
                    case ZPLUS:
                        return TOP;
                }
            case ZPLUS:
                switch(other.sign){
                    case MINUS:
                    case ZMINUS:
                        return TOP;
                    case ZERO:
                    case ZPLUS:
                  /* case PLUS:
                       return this;*/
                }
        }
        return TOP;
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
      /*  if(lubAux(other)==this || lubAux(other)==other)
            return true;
        else
            return false;
    */
        switch (this.sign){
            case ZERO:
                switch (other.sign){
                    case ZERO:
                    case ZPLUS:
                    case ZMINUS:
                        return true;
                    default: return false;
                }
            case MINUS:
                switch (other.sign){
                    case MINUS:
                    case ZMINUS:
                        return true;
                    default: return false;
                }
            case PLUS:
                switch (other.sign){
                    case PLUS:
                    case ZPLUS:
                        return true;
                    default: return false;
                }

            default: return false;

        }
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
        return top();
    }

    private ExtSignDomain negate() {
        if (sign == ExtSignDomain.Sign.MINUS)
            return new ExtSignDomain(ExtSignDomain.Sign.PLUS);
        else if (sign == ExtSignDomain.Sign.PLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.MINUS);
        else if (sign == ExtSignDomain.Sign.ZPLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.ZMINUS);
        else if (sign == ExtSignDomain.Sign.ZMINUS)
            return new ExtSignDomain(ExtSignDomain.Sign.ZPLUS);

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
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO:
                        case ZMINUS:
                            return left;
                        case ZPLUS:
                        case TOP:
                        case PLUS:
                        default:
                            return TOP;
                    }
                case ZERO:
                    return right;
                case PLUS:
                    switch(right.sign){
                        case ZERO:
                        case ZPLUS:
                        case PLUS:
                            return left;
                        case TOP:
                            return right;
                        case MINUS:
                        case ZMINUS:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZMINUS:
                    switch(right.sign){
                        case MINUS:
                            return right;
                        case ZERO:
                        case ZMINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZPLUS:
                        default:
                            return TOP;
                    }
                case ZPLUS:
                    switch(right.sign){
                        case ZERO:
                        case ZPLUS:
                            return left;
                        case PLUS:
                        case TOP:
                            return right;
                        case ZMINUS:
                        case MINUS:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case PLUS:
                        case ZPLUS:
                            return left;
                        case ZMINUS:
                        case TOP:
                        case MINUS:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO:
                        case PLUS:
                        case ZMINUS:
                        case ZPLUS:
                            return right.negate();
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case ZERO:
                        case ZMINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZPLUS:
                        case MINUS:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZMINUS:
                    switch (right.sign) {
                        case ZERO:
                        case ZPLUS:
                            return left;
                        case PLUS:
                            return right.negate();
                        case ZMINUS:
                        case TOP:
                        case MINUS:
                        default:
                            return TOP;
                    }
                case ZPLUS:
                    switch (right.sign) {
                        case MINUS:
                            return right.negate();
                        case ZERO:
                        case ZMINUS:
                            return left;
                        case ZPLUS:
                        case TOP:
                        case PLUS:
                        default:
                            return TOP;
                    }
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
                case ZERO:
                    return new ExtSignDomain(ExtSignDomain.Sign.ZERO);
                case ZMINUS:
                    switch(right.sign){
                        case MINUS:
                        case ZMINUS:
                            return left.negate();
                        case ZERO:
                            return right;
                        case PLUS:
                        case ZPLUS:
                            return left;
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZPLUS:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case ZERO:
                        case ZMINUS:
                            return right;
                        case PLUS:
                        case ZPLUS:
                            return left;
                        case TOP:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == ExtSignDomain.Sign.ZERO)
                return BOTTOM;
            if (right.sign == ExtSignDomain.Sign.ZPLUS || right.sign == ExtSignDomain.Sign.ZMINUS)
                return BOTTOM;

            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return TOP;
                case ZERO:
                    return new ExtSignDomain(ExtSignDomain.Sign.ZERO);
                case ZMINUS:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case PLUS:
                            return left;
                        case TOP:
                            return TOP;
                    }
                case ZPLUS:
                    switch(right.sign){
                        case MINUS:
                            return left.negate();
                        case PLUS:
                            return left;
                        case TOP:
                            return TOP;
                    }
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
