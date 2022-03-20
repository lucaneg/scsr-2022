package it.unive.scsr;

import java.util.Objects;

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

// IMPLEMENTATION NOTE:
// the code below is outside of the scope of the course. You can uncomment it to get
// your code to compile. Beware that the code is written expecting that a field named 
// "sign" containing an enumeration (similar to the one saw during class) exists in 
// this class: if you name it differently, change also the code below to make it work 
// by just using the name of your choice instead of "sign"

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

	private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
	private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);

	enum Sign {
		BOTTOM, MINUS, ZERO, PLUS, NEGATIVEZERO, POSTIVEZERO, TOP;
	}

	private final Sign sign;

	public ExtSignDomain() {
		this(Sign.TOP);
	}

	public ExtSignDomain(Sign sign) {
		this.sign = sign;
	}

	protected boolean isNegativeSign(ExtSignDomain sign) {
		return sign.sign == Sign.MINUS || sign.sign == Sign.ZERO || sign.sign == Sign.NEGATIVEZERO;
	}

	protected boolean isPositiveSign(ExtSignDomain sign) {
		return sign.sign == Sign.PLUS || sign.sign == Sign.ZERO || sign.sign == Sign.POSTIVEZERO;
	}

	private ExtSignDomain negateSign() {
		switch (sign) {
			case MINUS:
				return new ExtSignDomain(Sign.PLUS);
			case PLUS:
				return new ExtSignDomain(Sign.MINUS);
			case NEGATIVEZERO:
				return new ExtSignDomain(Sign.POSTIVEZERO);
			case POSTIVEZERO:
				return new ExtSignDomain(Sign.NEGATIVEZERO);
			default:
				return this;
		}

	}

	private ExtSignDomain additionOperation(ExtSignDomain leftDomain, ExtSignDomain rightDomain) {
		if (leftDomain.sign == Sign.MINUS) {
			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.MINUS
					|| rightDomain.sign == Sign.NEGATIVEZERO) {
				return leftDomain;
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.PLUS) {
			if (rightDomain.sign == Sign.PLUS || rightDomain.sign == Sign.ZERO
					|| rightDomain.sign == Sign.POSTIVEZERO) {
				return leftDomain;
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.NEGATIVEZERO) {
			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.NEGATIVEZERO) {
				return leftDomain;
			} else if (rightDomain.sign == Sign.MINUS) {
				return new ExtSignDomain(Sign.MINUS);
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.POSTIVEZERO) {

			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.POSTIVEZERO) {
				return leftDomain;
			} else if (rightDomain.sign == Sign.PLUS) {
				return new ExtSignDomain(Sign.PLUS);
			} else {
				return TOP;
			}

		} else if (leftDomain.sign == Sign.TOP) {
			return TOP;
		} else if (leftDomain.sign == Sign.ZERO) {
			return rightDomain;
		} else {
			return TOP;
		}

	}

	private ExtSignDomain substractionOperation(ExtSignDomain leftDomain, ExtSignDomain rightDomain) {
		if (leftDomain.sign == Sign.MINUS) {
			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.PLUS
					|| rightDomain.sign == Sign.POSTIVEZERO) {
				return leftDomain;
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.PLUS) {
			if (rightDomain.sign == Sign.MINUS || rightDomain.sign == Sign.ZERO
					|| rightDomain.sign == Sign.NEGATIVEZERO) {
				return leftDomain;
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.NEGATIVEZERO) {
			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.POSTIVEZERO) {
				return leftDomain;
			} else if (rightDomain.sign == Sign.PLUS) {
				return new ExtSignDomain(Sign.MINUS);
			} else {
				return TOP;
			}
		} else if (leftDomain.sign == Sign.POSTIVEZERO) {

			if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.NEGATIVEZERO) {
				return leftDomain;
			} else if (rightDomain.sign == Sign.MINUS) {
				return new ExtSignDomain(Sign.PLUS);
			} else {
				return TOP;
			}

		} else if (leftDomain.sign == Sign.TOP) {
			return TOP;
		} else if (leftDomain.sign == Sign.ZERO) {
			return rightDomain;
		} else {
			return TOP;
		}
	}

	private ExtSignDomain multiplicationOperation(ExtSignDomain leftDomain, ExtSignDomain rightDomain) {
		if (leftDomain.sign == Sign.MINUS) {
			return rightDomain.negateSign();
		} else if (leftDomain.sign == Sign.PLUS) {
			return rightDomain;
		} else if (leftDomain.sign == Sign.NEGATIVEZERO) {
			if (rightDomain.sign == Sign.MINUS || rightDomain.sign == Sign.NEGATIVEZERO) {
				return new ExtSignDomain(Sign.POSTIVEZERO);
			} else if (rightDomain.sign == Sign.PLUS || rightDomain.sign == Sign.POSTIVEZERO) {
				return new ExtSignDomain(Sign.NEGATIVEZERO);
			} else {
				return rightDomain.negateSign();
			}
		} else if (leftDomain.sign == Sign.POSTIVEZERO) {

			if (rightDomain.sign == Sign.MINUS || rightDomain.sign == Sign.NEGATIVEZERO) {
				return new ExtSignDomain(Sign.NEGATIVEZERO);
			} else if (rightDomain.sign == Sign.PLUS || rightDomain.sign == Sign.POSTIVEZERO) {
				return new ExtSignDomain(Sign.POSTIVEZERO);
			} else {
				return rightDomain;
			}

		} else if (leftDomain.sign == Sign.TOP) {
			return TOP;
		} else if (leftDomain.sign == Sign.ZERO) {
			return new ExtSignDomain(Sign.ZERO);
		} else {
			return TOP;
		}

	}

	private ExtSignDomain divisionOperation(ExtSignDomain leftDomain, ExtSignDomain rightDomain) {
		if (rightDomain.sign == Sign.ZERO || rightDomain.sign == Sign.NEGATIVEZERO
				|| rightDomain.sign == Sign.POSTIVEZERO)
			return BOTTOM;

		if (leftDomain.sign == Sign.MINUS) {
			return rightDomain.negateSign();
		} else if (leftDomain.sign == Sign.PLUS) {
			return rightDomain;
		} else if (leftDomain.sign == Sign.NEGATIVEZERO) {
			if (rightDomain.sign == Sign.MINUS) {
				return new ExtSignDomain(Sign.POSTIVEZERO);
			} else if (rightDomain.sign == Sign.PLUS) {
				return new ExtSignDomain(Sign.NEGATIVEZERO);
			} else {
				return rightDomain.negateSign();
			}
		} else if (leftDomain.sign == Sign.POSTIVEZERO) {

			if (rightDomain.sign == Sign.MINUS) {
				return new ExtSignDomain(Sign.NEGATIVEZERO);
			} else if (rightDomain.sign == Sign.PLUS) {
				return new ExtSignDomain(Sign.POSTIVEZERO);
			} else {
				return rightDomain;
			}

		} else if (leftDomain.sign == Sign.TOP) {
			return TOP;
		} else if (leftDomain.sign == Sign.ZERO) {
			return new ExtSignDomain(Sign.ZERO);
		} else {
			return TOP;
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
	public ExtSignDomain top() {
		return TOP;
	}

	@Override
	public ExtSignDomain bottom() {
		return BOTTOM;
	}

	@Override
	protected ExtSignDomain lubAux(ExtSignDomain otherSign) throws SemanticException {
		if (isNegativeSign(this) && isNegativeSign(otherSign)) {
			return new ExtSignDomain(Sign.NEGATIVEZERO);
		}

		if (isPositiveSign(this) && isPositiveSign(otherSign)) {
			return new ExtSignDomain(Sign.POSTIVEZERO);
		}

		return TOP;

	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain sign) throws SemanticException {
		return TOP;
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain otherSign) throws SemanticException {
		switch (this.sign) {
			case MINUS:
				return (otherSign.sign == Sign.NEGATIVEZERO);
			case PLUS:
				return (otherSign.sign == Sign.POSTIVEZERO);
			case ZERO:
				return (otherSign.sign == Sign.NEGATIVEZERO || otherSign.sign == Sign.POSTIVEZERO);
			default:
				return false;
		}
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}

	@Override
	protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			int value = (Integer) constant.getValue();
			if (value > 0) {
				return new ExtSignDomain(Sign.PLUS);
			}
			if (value == 0) {
				return new ExtSignDomain(Sign.ZERO);
			}

			return new ExtSignDomain(Sign.MINUS);
		}
		return top();
	}

	@Override
	protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arguments, ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof NumericNegation) {
			return arguments.negateSign();
		}
		return top();
	}

	@Override
	protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain leftDomain,
			ExtSignDomain rightDomain,
			ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof AdditionOperator) {
			return additionOperation(leftDomain, rightDomain);
		} else if (operator instanceof SubtractionOperator) {
			return substractionOperation(leftDomain, rightDomain);
		} else if (operator instanceof Multiplication) {
			return multiplicationOperation(leftDomain, rightDomain);
		} else if (operator instanceof DivisionOperator) {
			return divisionOperation(leftDomain, rightDomain);
		}

		return top();
	}
}