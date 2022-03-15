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
		BOTTOM, MINUS, ZERO, PLUS, TOP;
	}

	private final Sign sign;

	public Signs() {
		this(Sign.TOP);
	}

	public Signs(Sign sign) {
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
		Signs other = (Signs) obj;
		return sign == other.sign;
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
		return new StringRepresentation(sign);
	}

	@Override
	protected Signs evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			int v = (Integer) constant.getValue();
			if (v > 0)
				return new Signs(Sign.PLUS);
			else if (v == 0)
				return new Signs(Sign.ZERO);
			else
				return new Signs(Sign.MINUS);
		}
		return top();
	}

	private Signs negate() {
		if (sign == Sign.MINUS)
			return new Signs(Sign.PLUS);
		else if (sign == Sign.PLUS)
			return new Signs(Sign.MINUS);
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
			switch (left.sign) {
			case MINUS:
				switch (right.sign) {
				case ZERO:
				case MINUS:
					return left;
				case PLUS:
				case TOP:
				default:
					return TOP;
				}
			case PLUS:
				switch (right.sign) {
				case PLUS:
				case ZERO:
					return left;
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
					return left;
				case MINUS:
				case TOP:
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
			case TOP:
				return TOP;
			case ZERO:
				return right;
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
				return new Signs(Sign.ZERO);
			default:
				return TOP;
			}
		} else if (operator instanceof DivisionOperator) {
			if (right.sign == Sign.ZERO)
				return BOTTOM;

			switch (left.sign) {
			case MINUS:
				return right.negate();
			case PLUS:
				return right;
			case TOP:
				return TOP;
			case ZERO:
				return new Signs(Sign.ZERO);
			default:
				return TOP;
			}
		}

		return top();
	}
}
