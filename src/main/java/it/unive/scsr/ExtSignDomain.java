package it.unive.scsr;

import java.util.Objects;

import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;


public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    enum Sign {
        BOTTOM, MINUS, ZERO, PLUS, ZERO_MINUS, ZERO_PLUS, TOP;
    }

    private final Sign sign;

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this.sign == Sign.MINUS && other.sign == Sign.ZERO || other.sign == Sign.MINUS && this.sign == Sign.ZERO
                || other.sign == Sign.MINUS && this.sign == Sign.ZERO_MINUS || this.sign == Sign.MINUS && other.sign == Sign.ZERO_MINUS
                || other.sign == Sign.ZERO && this.sign == Sign.ZERO_MINUS || this.sign == Sign.ZERO && other.sign == Sign.ZERO_MINUS) {
            return new ExtSignDomain(Sign.ZERO_MINUS);
        }
        if (this.sign == Sign.PLUS && other.sign == Sign.ZERO || other.sign == Sign.PLUS && this.sign == Sign.ZERO
                || other.sign == Sign.PLUS && this.sign == Sign.ZERO_PLUS || this.sign == Sign.PLUS && other.sign == Sign.ZERO_PLUS
                || other.sign == Sign.ZERO && this.sign == Sign.ZERO_PLUS || this.sign == Sign.ZERO && other.sign == Sign.ZERO_PLUS) {
            return new ExtSignDomain(Sign.ZERO_PLUS);
        }
        return TOP;
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this.sign == Sign.MINUS) {
            return other.sign == Sign.ZERO_MINUS;
        }
        if (this.sign == Sign.PLUS) {
            return other.sign == Sign.ZERO_PLUS;
        }
        if (this.sign == Sign.ZERO) {
            return other.sign == Sign.ZERO_MINUS || other.sign == Sign.ZERO_PLUS;
        }
        return false;
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
        return top();
    }

    private ExtSignDomain negate() {
        if (sign == Sign.MINUS)
            return new ExtSignDomain(ExtSignDomain.Sign.PLUS);
        else if (sign == Sign.PLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.MINUS);
        else if (sign == Sign.ZERO_PLUS)
            return new ExtSignDomain(ExtSignDomain.Sign.ZERO_MINUS);
        else if (sign == Sign.ZERO_MINUS)
            return new ExtSignDomain(Sign.ZERO_PLUS);
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
        boolean ZeroMinusZMinus = right.sign == Sign.ZERO || right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS;
        boolean ZeroPlusZPlus = right.sign == Sign.ZERO || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS;

        System.out.println(left);
        System.out.println(operator);

        if (operator instanceof AdditionOperator) {

            if (left.sign == Sign.MINUS || left.sign == Sign.ZERO_MINUS) {
                if (ZeroMinusZMinus) {
                    return left;
                }
            }
            if (left.sign == Sign.PLUS || left.sign == Sign.ZERO_PLUS) {
                if (ZeroPlusZPlus) {
                    return left;
                }
            }
            if (left.sign == Sign.ZERO) {
                return right;
            }

        } else if (operator instanceof SubtractionOperator) {

            if (left.sign == Sign.MINUS) {
                if (ZeroPlusZPlus) {
                    return new ExtSignDomain(Sign.MINUS);
                }

            }

            if (left.sign == Sign.ZERO_MINUS) {
                if (right.sign == Sign.ZERO_PLUS || right.sign == Sign.ZERO) {
                    return left;
                }
                if (right.sign == Sign.PLUS) {
                    return new ExtSignDomain(Sign.MINUS);
                }
            }

            if (left.sign == Sign.PLUS) {
                if (ZeroMinusZMinus) {
                    return left;
                }
            }

            if (left.sign == Sign.ZERO_PLUS) {
                if (right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO) {
                    return left;
                }
                if (right.sign == Sign.MINUS) {
                    return new ExtSignDomain(Sign.PLUS);
                }
            }
            if (left.sign == Sign.ZERO) {
                return right.negate();
            }

        } else if (operator instanceof Multiplication) {
            if (left.sign == Sign.MINUS) {
                return right.negate();
            }
            if (left.sign == Sign.PLUS) {
                return right;
            }
            if (left.sign == Sign.ZERO_PLUS) {
                if (right.sign == Sign.MINUS) {
                    return new ExtSignDomain(Sign.ZERO_MINUS);
                }
                if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS) {
                    return right;
                }
                if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS) {
                    return left;
                }
            }
            if (left.sign == Sign.ZERO) {
                return new ExtSignDomain(Sign.ZERO);
            }
            if (left.sign == Sign.ZERO_MINUS) {
                if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS) {
                    return left;
                }
                if (right.sign == Sign.ZERO_MINUS || right.sign == Sign.MINUS) {
                    return new ExtSignDomain(Sign.ZERO_PLUS);
                }
                if (right.sign == Sign.ZERO) {
                    return right;
                }
            }

        } else if (operator instanceof DivisionOperator) {
            if (right.sign == Sign.ZERO)
                return BOTTOM;

            if (left.sign == Sign.MINUS) {
                return right.negate();
            }
            if (left.sign == Sign.PLUS) {
                return right;
            }
            if (left.sign == Sign.ZERO){
                return left;
            }
            if (left.sign == Sign.ZERO_MINUS){
                if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS){
                    return left.negate();
                }
                if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS){
                    return left;
                }
            }
            if (left.sign == Sign.ZERO_PLUS) {
                if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS){
                    return left.negate();
                }
                if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS){
                    return left;
                }
            }
        }
        return top();
    }

    // IMPLEMENTATION NOTE:
    // the code below is outside the scope of the course. You can uncomment it to get
    // your code to compile. Beware that the code is written expecting that a field named
    // "sign" containing an enumeration (similar to the one saw during class) exists in
    // this class: if you name it differently, change also the code below to make it work
    // by just using the name of your choice instead of "sign"

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(sign);
    }

}
