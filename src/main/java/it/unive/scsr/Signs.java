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

public class Signs extends BaseNonRelationalValueDomain<Signs> {

	private static final Signs TOP = new Signs(Sign.TOP);
	private static final Signs BOTTOM = new Signs(Sign.BOTTOM);

	enum Sign {
		TOP, BOTTOM, POSITIVE, NEGATIVE, ZERO;
	}

	private final Sign _sign;

	public Signs() {
		this(Sign.TOP);
	}

	public Signs(Sign _sign) {
		this._sign = _sign;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_sign);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signs other = (Signs) obj;
		return _sign == other._sign;
	}

	@Override
	public Signs top() {
		return TOP;
	}

	@Override
	public Signs bottom() {
		return BOTTOM;
	}

	@Override
	protected Signs lubAux(Signs other) throws SemanticException {
		return TOP;
	}

	@Override
	protected Signs wideningAux(Signs other) throws SemanticException {
		return TOP;
	}

	@Override
	protected boolean lessOrEqualAux(Signs other) throws SemanticException {
		return false;
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(_sign);
	}

	@Override
	protected Signs evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			int v = (Integer) constant.getValue();
			if (v > 0)
				return new Signs(Sign.POSITIVE);
			else if (v == 0)
				return new Signs(Sign.ZERO);
			else
				return new Signs(Sign.NEGATIVE);
		}
		return top();
	}

	private Signs negate() {
		if (_sign == Sign.NEGATIVE)
			return new Signs(Sign.POSITIVE);
		else if (_sign == Sign.POSITIVE)
			return new Signs(Sign.NEGATIVE);
		else
			return this;
	}

	@Override
	protected Signs evalUnaryExpression(UnaryOperator operator, Signs arg, ProgramPoint pp) throws SemanticException {
		if (operator instanceof NumericNegation)
			return arg.negate();

		return top();
	}

	@Override
	protected Signs evalBinaryExpression(BinaryOperator operator, Signs left, Signs right, ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof AdditionOperator) {
			if (left._sign == Sign.NEGATIVE) {
				if (right._sign == Sign.ZERO || right._sign == Sign.NEGATIVE) {
					return left;
				} else if (right._sign == Sign.POSITIVE || right._sign == Sign.TOP) {
					return TOP;
				}
			} else if (left._sign == Sign.POSITIVE) {
				if (right._sign == Sign.ZERO || right._sign == Sign.POSITIVE) {
					return left;
				} else if (right._sign == Sign.TOP || right._sign == Sign.NEGATIVE) {
					return TOP;
				}
			} else if (left._sign == Sign.TOP) {
				return TOP;
			} else if (left._sign == Sign.ZERO) {
				return right;
			} else {
				return TOP;
			}
		} else if (operator instanceof SubtractionOperator) {
			if (left._sign == Sign.POSITIVE) {
				if (right._sign == Sign.ZERO || right._sign == Sign.NEGATIVE) {
					return left;
				} else if (right._sign == Sign.POSITIVE || right._sign == Sign.TOP) {
					return TOP;
				}
			} else if (left._sign == Sign.NEGATIVE) {
				if (right._sign == Sign.ZERO || right._sign == Sign.POSITIVE) {
					return left;
				} else if (right._sign == Sign.TOP || right._sign == Sign.NEGATIVE) {
					return TOP;
				}
			} else if (left._sign == Sign.TOP) {
				return TOP;
			} else if (left._sign == Sign.ZERO) {
				return right;
			} else {
				return TOP;
			}
		} else if (operator instanceof Multiplication) {
			if (left._sign == Sign.POSITIVE) {
				return right;
			} else if (left._sign == Sign.NEGATIVE) {
				return right.negate();
			} else if (left._sign == Sign.TOP) {
				return TOP;
			} else if (left._sign == Sign.ZERO) {
				return new Signs(Sign.ZERO);
			} else {
				return TOP;
			}
		} else if (operator instanceof DivisionOperator) {
			if (right._sign == Sign.ZERO)
				return BOTTOM;
			if (left._sign == Sign.POSITIVE) {
				return right;
			} else if (left._sign == Sign.NEGATIVE) {
				return right.negate();
			} else if (left._sign == Sign.TOP) {
				return TOP;
			} else if (left._sign == Sign.ZERO) {
				return new Signs(Sign.ZERO);
			} else {
				return TOP;
			}
		}

		return top();
	}
}
