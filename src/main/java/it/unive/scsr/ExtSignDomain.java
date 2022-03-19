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

    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);
    private static final ExtSignDomain ZERO_MINUS = new ExtSignDomain(ExtSign.ZERO_MINUS);
    private static final ExtSignDomain ZERO_PLUS = new ExtSignDomain(ExtSign.ZERO_PLUS);
    private static final ExtSignDomain PLUS = new ExtSignDomain(ExtSign.PLUS);
    private static final ExtSignDomain MINUS = new ExtSignDomain(ExtSign.MINUS);

    enum ExtSign {
       BOTTOM, MINUS, ZERO, ZERO_MINUS, ZERO_PLUS, PLUS, TOP;
    }

    private final ExtSign sign;

    public ExtSignDomain() {
        this(ExtSign.TOP);
    }

    public ExtSignDomain(ExtSign sign) {
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
    public ExtSignDomain top() { return TOP; }

    @Override
    public ExtSignDomain bottom() { return BOTTOM; }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        // ZERO & MINUS
        if ((other.sign == ExtSign.MINUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.MINUS))
            return ZERO_MINUS;

        // ZERO_MINUS & ZERO
        else if ((other.sign == ExtSign.ZERO_MINUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.ZERO_MINUS))
            return ZERO_MINUS;

        // ZERO_MINUS & MINUS
        else if ((other.sign == ExtSign.ZERO_MINUS && this.sign == ExtSign.MINUS) || (other.sign == ExtSign.MINUS && this.sign == ExtSign.ZERO_MINUS))
            return ZERO_MINUS;

        // ZERO & PLUS
        else if ((other.sign == ExtSign.PLUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.PLUS))
            return ZERO_PLUS;

        // ZERO_PLUS & ZERO
        else if ((other.sign == ExtSign.ZERO_PLUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.ZERO_PLUS))
            return ZERO_PLUS;

        // ZERO_PLUS & PLUS
        else if ((other.sign == ExtSign.ZERO_PLUS && this.sign == ExtSign.PLUS) || (other.sign == ExtSign.PLUS && this.sign == ExtSign.ZERO_PLUS))
            return ZERO_PLUS;

        // ANYTHING ELSE
        else
            return top();
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        // ZERO & MINUS
        if ((other.sign == ExtSign.MINUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.MINUS))
            return ZERO_MINUS;

        // ZERO_MINUS & ZERO
        else if ((other.sign == ExtSign.ZERO_MINUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.ZERO_MINUS))
            return ZERO_MINUS;

        // ZERO_MINUS & MINUS
        else if ((other.sign == ExtSign.ZERO_MINUS && this.sign == ExtSign.MINUS) || (other.sign == ExtSign.MINUS && this.sign == ExtSign.ZERO_MINUS))
            return ZERO_MINUS;

        // ZERO & PLUS
        else if ((other.sign == ExtSign.PLUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.PLUS))
            return ZERO_PLUS;

        // ZERO_PLUS & ZERO
        else if ((other.sign == ExtSign.ZERO_PLUS && this.sign == ExtSign.ZERO) || (other.sign == ExtSign.ZERO && this.sign == ExtSign.ZERO_PLUS))
            return ZERO_PLUS;

        // ZERO_PLUS & PLUS
        else if ((other.sign == ExtSign.ZERO_PLUS && this.sign == ExtSign.PLUS) || (other.sign == ExtSign.PLUS && this.sign == ExtSign.ZERO_PLUS))
            return ZERO_PLUS;

        // ANYTHING ELSE
        else
            return top();
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        // ZERO & ZERO_MINUS
        if (this.sign == ExtSign.ZERO && other.sign == ExtSign.ZERO_MINUS)
            return true;
        // MINUS & ZERO_MINUS
        else if (this.sign == ExtSign.MINUS && other.sign == ExtSign.ZERO_MINUS)
            return true;
        // ZERO & ZERO_PLUS
        else if (this.sign == ExtSign.ZERO && other.sign == ExtSign.ZERO_PLUS)
            return true;
        // PLUS & ZERO_PLUS
        else if (this.sign == ExtSign.PLUS && other.sign == ExtSign.ZERO_PLUS)
            return true;
        // ANYTHING ELSE
        else
            return false;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(ExtSign.ZERO_PLUS);
            else if (v == 0)
                return new ExtSignDomain(ExtSign.ZERO);
            else
                return new ExtSignDomain(ExtSign.ZERO_MINUS);
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (sign == ExtSign.MINUS)
            return new ExtSignDomain(ExtSign.PLUS);
        else if (sign == ExtSign.PLUS)
            return new ExtSignDomain(ExtSign.MINUS);
        else if (sign == ExtSign.ZERO_MINUS)
            return new ExtSignDomain(ExtSign.ZERO_PLUS);
        else if (sign == ExtSign.ZERO_PLUS)
            return new ExtSignDomain(ExtSign.ZERO_MINUS);
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
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case MINUS:
                        case ZERO_MINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZERO_PLUS:
                        default:
                            return top();
                    }
                case PLUS:
                    switch (right.sign) {
                        case PLUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZERO_MINUS:
                        default:
                            return top();
                    }
                case TOP:
                    return top();
                case ZERO:
                    return right;
                case ZERO_MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case ZERO_MINUS:
                            return left;
                        case MINUS:
                            return right;

                        case PLUS:
                        case TOP:
                        case ZERO_PLUS:
                        default:
                            return top();
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case ZERO:
                        case ZERO_PLUS:
                            return left;
                        case PLUS:
                            return right;
                        case MINUS:
                        case TOP:
                        case ZERO_MINUS:
                        default:
                            return top();
                    }
                default:
                    return top();
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case PLUS:
                        case ZERO_PLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZERO_MINUS:
                        default:
                            return top();
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO:
                        case ZERO_MINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZERO_PLUS:
                        default:
                            return top();
                    }
                case TOP:
                    return top();
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                            return PLUS;
                        case PLUS:
                            return MINUS;
                        case ZERO:
                            return left;
                        case ZERO_MINUS:
                            return ZERO_PLUS;
                        case ZERO_PLUS:
                            return ZERO_MINUS;
                        case TOP:
                        default:
                            return top();
                    }
                case ZERO_MINUS:
                    switch (right.sign) {
                        case PLUS:
                            return MINUS;
                        case ZERO:
                            return left;
                        case ZERO_PLUS:
                            return ZERO_MINUS;
                        case ZERO_MINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return top();
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return PLUS;
                        case ZERO:
                        case ZERO_MINUS:
                            return left;
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return top();
                    }
                default:
                    return top();
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return top();
                case ZERO:
                    return new ExtSignDomain(ExtSign.ZERO);
                case ZERO_MINUS:
                    switch (right.sign) {
                        case ZERO:
                            return right;
                        case MINUS:
                        case ZERO_MINUS:
                            return ZERO_PLUS;
                        case ZERO_PLUS:
                        case PLUS:
                            return ZERO_MINUS;
                        case TOP:
                        default:
                            return top();
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case ZERO:
                            return right;
                        case MINUS:
                        case ZERO_MINUS:
                            return ZERO_MINUS;
                        case PLUS:
                        case ZERO_PLUS:
                            return ZERO_PLUS;
                        case TOP:
                        default:
                            return top();
                    }
                default:
                    return top();
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == ExtSign.ZERO || right.sign == ExtSign.ZERO_MINUS || right.sign == ExtSign.ZERO_PLUS)
                return bottom();

            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return top();
                case ZERO:
                    return new ExtSignDomain(ExtSign.ZERO);
                case ZERO_MINUS:
                    switch (right.sign) {
                        case MINUS:
                            return ZERO_PLUS;
                        case PLUS:
                            return left;
                        case TOP:
                        default:
                            return top();
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return ZERO_MINUS;
                        case PLUS:
                            return left;
                        case TOP:
                        default:
                            return top();
                    }
                default:
                    return top();
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