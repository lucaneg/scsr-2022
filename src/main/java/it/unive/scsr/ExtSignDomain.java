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
 
    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);

    enum ExtSign {
        BOTTOM, NEGATIVE, POSITIVE, NIL, NILPOSITIVE, NILNEGATIVE, TOP
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
            case NEGATIVE:
                switch(other.sign){
                    case NIL:
                    case NILNEGATIVE:
                        return new ExtSignDomain(ExtSign.NILNEGATIVE);
                    case POSITIVE:
                    case NILPOSITIVE:
                    default:
                        return TOP;
                }
            case NIL:
                switch(other.sign){
                    case NILNEGATIVE:
                    case NEGATIVE:
                        return new ExtSignDomain(ExtSign.NILNEGATIVE);
                    case NILPOSITIVE:
                    case POSITIVE:
                        return new ExtSignDomain(ExtSign.NILPOSITIVE);
                    default: 
                        return TOP;
                }
            case POSITIVE:
                switch(other.sign){
                    case NILPOSITIVE:
                    case NIL:
                        return new ExtSignDomain(ExtSign.NILPOSITIVE);
                    case NEGATIVE:
                    case NILNEGATIVE:
                    default:
                        return TOP;
                }
            case NILNEGATIVE:
                switch(other.sign){
                    case NIL:
                    case NEGATIVE:
                        return new ExtSignDomain(ExtSign.NILNEGATIVE);
                    case POSITIVE:
                    case NILPOSITIVE:
                    default:
                        return TOP;
                }
            case NILPOSITIVE:
                switch(other.sign) {
                    case NIL:
                    case POSITIVE:
                        return new ExtSignDomain(ExtSign.NILPOSITIVE);
                    case NEGATIVE:
                    case NILNEGATIVE:
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
        return (this.sign == NEGATIVE && other.sign == NILNEGATIVE) ||
                (this.sign == NIL && other.sign == NILNEGATIVE) || 
                (this.sign == NIL && other.sign == ExtSign.NILPOSITIVE) || 
                (this.sign == POSITIVE && other.sign == NILPOSITIVE); 
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
        if (sign == ExtSign.NEGATIVE)
            return new ExtSignDomain(ExtSign.POSITIVE);
        if (sign == ExtSign.POSITIVE)
            return new ExtSignDomain(ExtSign.NEGATIVE);
        if (sign == NILPOSITIVE)
            return new ExtSignDomain(NILNEGATIVE);
        if (sign == NILNEGATIVE)
            return new ExtSignDomain(NILPOSITIVE);
        else
            return this;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(POSITIVE);
            if (v == 0)
                return new ExtSignDomain(NIL);
            else
                return new ExtSignDomain(NEGATIVE);
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
                case NEGATIVE:
                    switch (right.sign) {
                        case NIL:
                        case NEGATIVE:
                        case NILNEGATIVE:
                            return left;
                        case POSITIVE:
                        case TOP:
                        case NILPOSITIVE:
                        default:
                            return TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case POSITIVE:
                        case NIL:
                        case NILPOSITIVE:
                            return left;
                        case NEGATIVE:
                        case TOP:
                        case NILNEGATIVE:
                        default:
                            return TOP;
                    }
                case NILPOSITIVE:
                    switch (right.sign){
                        case NILNEGATIVE:
                        case NIL:
                            return left;
                        case NEGATIVE:
                            return right;
                        case NILPOSITIVE:
                        case POSITIVE:
                        case TOP:
                        default:
                            return TOP;
                    }
                case NILNEGATIVE:
                    switch (right.sign) {
                        case NILPOSITIVE:
                        case NIL:
                            return left;
                        case POSITIVE:
                            return right;
                        case NILNEGATIVE:
                        case NEGATIVE:
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case NIL:
                    return right;
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    switch (right.sign) {
                        case NIL:
                        case POSITIVE:
                        case NILPOSITIVE:
                            return left;
                        case NEGATIVE:
                        case TOP:
                        case NILNEGATIVE:
                        default:
                            return TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case NIL:
                            return left;
                        case POSITIVE:
                        case TOP:
                        default:
                            return TOP;
                    }
                case NILNEGATIVE:
                    switch (right.sign) {
                        case NILPOSITIVE:
                        case NIL:
                            return left;
                        case POSITIVE:
                            return new ExtSignDomain(NEGATIVE);
                        case NILNEGATIVE:
                        case NEGATIVE:
                        case TOP:
                        default:
                            return TOP;
                    }
                case NILPOSITIVE:
                    switch (right.sign) {
                        case NILNEGATIVE:
                        case NIL:
                            return left;
                        case NEGATIVE:
                            return new ExtSignDomain(POSITIVE);
                        case NILPOSITIVE:
                        case POSITIVE:
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case NIL:
                    return right.negate();
                default:
                    return TOP;
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case NEGATIVE:
                    return right.negate();
                case POSITIVE:
                    return right;
                case TOP:
                    return TOP;
                case NILNEGATIVE:
                    switch (right.sign){
                        case NILPOSITIVE:
                        case POSITIVE:
                            return left;
                        case NILNEGATIVE:
                        case NEGATIVE:
                            return new ExtSignDomain(NILPOSITIVE);
                        case NIL:
                            return right;
                        case TOP:
                        default:
                            return TOP;
                    }
                case NILPOSITIVE:
                    switch (right.sign){
                        case NILPOSITIVE:
                        case POSITIVE:
                            return left;
                        case NILNEGATIVE:
                        case NIL:
                            return right;
                        case NEGATIVE:
                            return new ExtSignDomain(NILNEGATIVE);
                        case TOP:
                        default:
                            return TOP;

                    }
                case NIL:
                    return new ExtSignDomain(NIL);
                default:
                    return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == NIL ||
                    right.sign == NILPOSITIVE ||
                    right.sign == NILNEGATIVE)
                return BOTTOM;

            switch (left.sign) {
                case NEGATIVE:
                    return right.negate();
                case POSITIVE:
                    return right;
                case NILNEGATIVE:
                    switch (right.sign){
                        case POSITIVE:
                            return left;
                        case NEGATIVE:
                            return new ExtSignDomain(NILPOSITIVE);
                        case TOP:
                        default:
                            return TOP;
                    }
                case NILPOSITIVE:
                    switch (right.sign){
                        case POSITIVE:
                            return left;
                        case NEGATIVE:
                            return new ExtSignDomain(NILNEGATIVE);
                        case TOP:
                        default:
                            return TOP;

                    }
                case TOP:
                    return TOP;
                case NIL:
                    return new ExtSignDomain(NIL);
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
