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

    enum ExtSign {
        BOTTOM, MINUS, ZERO, PLUS, ZERO_MINUS, ZERO_PLUS, TOP
    }


    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);

    private final ExtSign extSign;

    public ExtSignDomain(ExtSign sign){
        this.extSign = sign;
    }

    public ExtSignDomain(){
        //If no value is passed that default initialization is defined with value TOP (no info can be inferred)
        this(ExtSign.TOP);
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.ZERO) {
            return new ExtSignDomain(ExtSign.ZERO_MINUS);
        }else if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZERO) {
            return new ExtSignDomain(ExtSign.ZERO_PLUS);
        }else {
            return new ExtSignDomain(ExtSign.TOP);
        }
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.ZERO_MINUS) {
            return true;
        }else if(this.extSign == ExtSign.ZERO && (other.extSign == ExtSign.ZERO_MINUS || other.extSign == ExtSign.ZERO_PLUS)) {
            return true;
        }else return this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZERO_PLUS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtSignDomain that = (ExtSignDomain) o;
        return extSign == that.extSign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extSign);
    }

    @Override
    public ExtSignDomain top() {
        //Returns constant instance of TOP value
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        //Returns constant instance of BOTTOM value
        return BOTTOM;
    }

    private ExtSignDomain negate(){
        //If value can be negated return proper opposite element. If not, return current value (if top or bottom are provided)
        return switch (this.extSign) {
            case MINUS -> new ExtSignDomain(ExtSign.PLUS);
            case PLUS -> new ExtSignDomain(ExtSign.MINUS);
            case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
            case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
            default -> this;
        };
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(ExtSign.PLUS);
            else if (v == 0)
                return new ExtSignDomain(ExtSign.ZERO);
            else
                return new ExtSignDomain(ExtSign.MINUS);
        }
        else return top();
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
        //Evaluate operation in the form a + b
        if (operator instanceof AdditionOperator){
            switch (left.extSign){
                case MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (-a) + 0
                        //Evaluate operation in the form (-a) + (-b)
                        //Evaluate operation in the form (-a) + (0+)
                        //Evaluate operation in the form (-a) + (0-)
                        case ZERO, MINUS, ZERO_PLUS, ZERO_MINUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (-a) + (T | B | b)
                        default -> top();
                    };
                case PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form a + 0
                        //Evaluate operation in the form a + b
                        //Evaluate operation in the form a + (0+)
                        //Evaluate operation in the form a + (0-)
                        case ZERO, PLUS, ZERO_PLUS, ZERO_MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form a + (T | B | (-b))
                        default -> top();
                    };
                case ZERO:
                    return switch (right.extSign) {
                        //Evaluate operation in the form 0 + 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form 0 + b
                        case PLUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form 0 + (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form 0 + (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 + (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form 0 + (T | B)
                        default -> top();
                    };
                case ZERO_MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0-) + 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) + b
                        case PLUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (0-) + (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form (0-) + (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) + (T | B | (-b))
                        default -> top();
                    };
                case ZERO_PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0+) + 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) + b
                        case PLUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (0+) + (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) + (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form (0+) + (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (0+) + (T | B)
                        default -> top();
                    };
                default: return top();
            }
        }
        else if (operator instanceof SubtractionOperator){
            switch (left.extSign){
                case MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (-a) - 0
                        //Evaluate operation in the form (-a) - (0+)
                        //Evaluate operation in the form (-a) - (0-)
                        //Evaluate operation in the form (-a) - (b)
                        case ZERO, ZERO_PLUS, ZERO_MINUS, PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (-a) - (T | B | (-b))
                        default -> top();
                    };
                case PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form a - 0
                        //Evaluate operation in the form a - (-b)
                        //Evaluate operation in the form a - (0+)
                        //Evaluate operation in the form a - (0-)
                        case ZERO, MINUS, ZERO_PLUS, ZERO_MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form a - (T | B | b)
                        default -> top();
                    };
                case ZERO:
                    return switch (right.extSign) {
                        //Evaluate operation in the form 0 - 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 - b
                        case PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form 0 - (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 - (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form 0 - (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form 0 - (T | B)
                        default -> top();
                    };
                case ZERO_MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0-) - 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) - b
                        case PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (0-) - (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) - (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS); //TODO check correctness
                        //Evaluate operation in the form (0-) - (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (0-) - (T | B)
                        default -> top();
                    };
                case ZERO_PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0+) - 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) - b
                        case PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (0+) - (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form (0+) - (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS); //TODO check correctness
                        //Evaluate operation in the form (0+) - (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (0+) - (T | B)
                        default -> top();
                    };
                default: return top();
            }
        }
        else if (operator instanceof Multiplication) {
            switch (left.extSign) {
                case MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (-a) * (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (-a) * 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form (-a) * (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (-a) * (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (-a) * (b)
                        case PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (-a) * (T | B)
                        default -> top();
                    };
                case PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form a * (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form a * 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form a * (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form a * (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form a * (b)
                        case PLUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form a * (T | B)
                        default -> top();
                    };
                case ZERO:
                    return switch (right.extSign) {
                        //Evaluate operation in the form 0 * (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 * 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO);
                        //Evaluate operation in the form 0 * b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form 0 * (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form 0 * (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 * (T | B)
                        default -> top();
                    };
                case ZERO_MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0-) * 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) * b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) * (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) * (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0-) * (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0-) * (T | B)
                        default -> top();
                    };
                case ZERO_PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0+) * 0
                        case ZERO -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) * b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) * (0+)
                        case ZERO_PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) * (0-)
                        case ZERO_MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0+) * (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0+) * (T | B)
                        default -> top();
                    };
                default:
                    return top();
            }
        }
        else if (operator instanceof DivisionOperator){
            //If divisor is zero than division is undefined so bottom element is returned
            if (right.extSign == ExtSign.ZERO || right.extSign == ExtSign.ZERO_PLUS || right.extSign == ExtSign.ZERO_MINUS) {
                return BOTTOM;
            }
            switch (left.extSign) {
                case MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (-a) / (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form (-a) / (b)
                        case PLUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form (-a) / (T | B)
                        default -> top();
                    };
                case PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form a / (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.MINUS);
                        //Evaluate operation in the form a / (b)
                        case PLUS -> new ExtSignDomain(ExtSign.PLUS);
                        //Evaluate operation in the form a / (T | B)
                        default -> top();
                    };
                case ZERO:
                    return switch (right.extSign) {
                        //Evaluate operation in the form 0 / (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form 0 / b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form 0 / (T | B)
                        default -> top();
                    };
                case ZERO_MINUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0-) / b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0-) / (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0-) / (T | B)
                        default -> top();
                    };
                case ZERO_PLUS:
                    return switch (right.extSign) {
                        //Evaluate operation in the form (0+) / b
                        case PLUS -> new ExtSignDomain(ExtSign.ZERO_PLUS);
                        //Evaluate operation in the form (0+) / (-b)
                        case MINUS -> new ExtSignDomain(ExtSign.ZERO_MINUS);
                        //Evaluate operation in the form (0+) / (T | B)
                        default -> top();
                    };
                default:
                    return top();
            }
        }
        else return top();
    }

    /************************************************/
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(extSign);
	}
}
