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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

	private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
	private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);
	private static final ExtSignDomain ZERO_PLUS = new ExtSignDomain(Sign.ZERO_PLUS);
	private static final ExtSignDomain ZERO_MINUS = new ExtSignDomain(Sign.ZERO_MINUS);

	enum Sign {
		BOTTOM, MINUS, ZERO, PLUS, ZERO_MINUS, ZERO_PLUS, TOP;
	}

	private final Sign sign;

	public ExtSignDomain() {
		this(Sign.TOP);
	}

	public ExtSignDomain(Sign sign) {
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
	public ExtSignDomain top() {
		return TOP;
	}

	@Override
	public ExtSignDomain bottom() {
		return BOTTOM;
	}

	@Override
	protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {

		// Check this and other Sign domain and calculate lub
		switch (this.sign) {
			case MINUS:
				switch (other.sign) {
					case ZERO:
						// same level between minus and zero, so lub is zero_minus
						return ZERO_MINUS;
					default:
						// Consider all the other cases
						return TOP;
				}
			case ZERO:
				switch (other.sign) {
					case MINUS:
						// same level between zero and minus, so lub is zero_minus
						return ZERO_MINUS;
					case PLUS:
						// same level between zero and plus, so lub is zero_minus
						return ZERO_PLUS;
					default:
						// Consider all the other cases
						return TOP;
				}
			case PLUS:
				switch (other.sign) {
					case ZERO:
						// same level between plus and zero, so lub is zero_minus
						return ZERO_PLUS;
					default:
						// Consider all the other cases
						return TOP;
				}
			default:
				return TOP;
		}
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		return TOP;
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {

		// Cofront this and other Sings anche check relation between these two
		switch (this.sign) {
			case MINUS:
				switch (other.sign) {
					case ZERO_MINUS:
						// By CFG, MINUS <= ZERO_MINUS so return true
						return true;
					default:
						// All the other cases
						return false;
				}
			case PLUS:
				switch (other.sign) {
					case ZERO_PLUS:
						// By CFG, PLUS <= ZERO_PLUS so return true
						return true;
					default:
						// All the other cases
						return false;
				}
			case ZERO:
				switch (other.sign) {
					case ZERO_PLUS:
					case ZERO_MINUS:
						// By CFG, MINUS <= ZERO_MINUS and PLUS <= ZERO_PLUS so return true
						return true;
					default:
						// All the other cases
						return false;
				}
			default:
				// In default takes ZERO_MINUS and ZERO_PLUS. So return false by default
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
			int v = (Integer) constant.getValue();
			if (v > 0)
				return new ExtSignDomain(Sign.PLUS);
			else if (v == 0)
				return new ExtSignDomain(Sign.ZERO);
			else
				return new ExtSignDomain(Sign.MINUS);
		}
		return top();
	}

	private ExtSignDomain negate() {
		if (sign == Sign.MINUS)
			return new ExtSignDomain(Sign.PLUS);
		else if (sign == Sign.PLUS)
			return new ExtSignDomain(Sign.MINUS);
		else
			return this;
	}

	@Override
	protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof NumericNegation)
			return arg.negate();

		return top();
	}

	@Override
	protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
			ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof AdditionOperator) {
			switch (left.sign) {
				case MINUS:
					switch (right.sign) {
						case ZERO:
						case ZERO_MINUS:
						case MINUS:
							return new ExtSignDomain(Sign.MINUS);
						default:
							return TOP;
					}
				case PLUS:
					switch (right.sign) {
						case ZERO_PLUS:
						case PLUS:
						case ZERO:
							return new ExtSignDomain(Sign.PLUS);
						default:
							return TOP;
					}
				case ZERO:
					return right;
				default:
					return TOP;
			}
		} else if (operator instanceof SubtractionOperator) {
			switch (left.sign) {
				case MINUS:
					switch (right.sign) {
						case PLUS:
						case ZERO_PLUS:
						case ZERO:
							return new ExtSignDomain(Sign.MINUS);
						case ZERO_MINUS:
							return new ExtSignDomain(Sign.ZERO_PLUS);
						default:
							return TOP;
					}
				case PLUS:
					switch (right.sign) {
						case ZERO_PLUS:
						case PLUS:
						case ZERO:
							return new ExtSignDomain(Sign.PLUS);
						default:
							return TOP;
					}
				case ZERO:
					return right;
				default:
					return TOP;
			}
		} else if (operator instanceof Multiplication) {
			switch (left.sign) {
				case MINUS:
					switch (right.sign) {
						case MINUS:
							return new ExtSignDomain(Sign.PLUS);
						case PLUS:
							return new ExtSignDomain(Sign.MINUS);
						case ZERO:
							return new ExtSignDomain(Sign.ZERO);
						case ZERO_MINUS:
							return new ExtSignDomain(Sign.ZERO_PLUS);
						case ZERO_PLUS:
							return new ExtSignDomain(Sign.ZERO_MINUS);
						case TOP:
						default:
							return TOP;

					}
				case PLUS:
					switch (right.sign) {
						case MINUS:
							return new ExtSignDomain(Sign.PLUS);
						case PLUS:
							return new ExtSignDomain(Sign.MINUS);
						case ZERO:
							return new ExtSignDomain(Sign.ZERO);
						case ZERO_MINUS:
							return new ExtSignDomain(Sign.ZERO_PLUS);
						case ZERO_PLUS:
							return new ExtSignDomain(Sign.ZERO_MINUS);
						case TOP:
						default:
							return TOP;

					}
				case ZERO:
					switch (right.sign) {
						case MINUS:
							return new ExtSignDomain(Sign.PLUS);
						case PLUS:
							return new ExtSignDomain(Sign.MINUS);
						case ZERO:
							return new ExtSignDomain(Sign.ZERO);
						case ZERO_MINUS:
							return new ExtSignDomain(Sign.ZERO_PLUS);
						case ZERO_PLUS:
							return new ExtSignDomain(Sign.ZERO_MINUS);
						case TOP:
						default:
							return TOP;

					}
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
					return new ExtSignDomain(Sign.ZERO);
				default:
					return TOP;
			}
		}

		return top();
	}
}