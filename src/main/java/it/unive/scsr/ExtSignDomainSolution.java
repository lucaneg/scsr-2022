package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
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

public class ExtSignDomainSolution extends BaseNonRelationalValueDomain<ExtSignDomainSolution> {

	private final Sign _sign;

	public ExtSignDomainSolution() {
		this(Sign.TOP);
	}

	private ExtSignDomainSolution(Sign _sign) {
		this._sign = _sign;
	}

	enum Sign {

		BOTTOM {

			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				return this;
			}

			@Override
			Sign mul(Sign other) {
				return this;
			}

			@Override
			Sign div(Sign other) {
				return this;
			}

			@Override
			public String toString() {
				return Lattice.BOTTOM_STRING;
			}
		},

		TOP {

			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				// add(top, bottom) = bottom
				// add(top, top) = top;
				// add(top, +) = top
				// add(top, 0) = top
				// add(top, -) = top
				// add(top, 0+) = top
				// add(top, 0-) = top
				return other == BOTTOM ? other : this;
			}

			@Override
			Sign mul(Sign other) {
				// mul(top, bottom) = bottom
				// mul(top, top) = top;
				// mul(top, +) = top
				// mul(top, 0) = 0
				// mul(top, -) = top
				// mul(top, 0+) = top
				// mul(top, 0-) = top
				return other == BOTTOM ? other : other == EMPTY ? EMPTY : TOP;
			}

			@Override
			Sign div(Sign other) {
				// div(top, bottom) = bottom
				// div(top, top) = top;
				// div(top, +) = top
				// div(top, 0) = bottom
				// div(top, -) = top
				// div(top, 0+) = top
				// div(top, 0-) = top
				return other == EMPTY || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return Lattice.TOP_STRING;
			}
		},

		POSITIVE {

			@Override
			Sign minus() {
				return NEGATIVE;
			}

			@Override
			Sign add(Sign other) {
				// add(+, bottom) = bottom
				// add(+, top) = top;
				// add(+, +) = +
				// add(+, 0) = +
				// add(+, -) = top
				// add(+, 0+) = +
				// add(+, 0-) = top
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POSITIVE || other == POSITIVE_OR_EMPTY || other == EMPTY)
					return this;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(+, bottom) = bottom
				// mul(+, top) = top;
				// mul(+, +) = +
				// mul(+, 0) = 0
				// mul(+, -) = -
				// mul(+, 0+) = 0+
				// mul(+, 0-) = 0-
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(+, bottom) = bottom
				// div(+, top) = top;
				// div(+, +) = +
				// div(+, 0) = bottom
				// div(+, -) = -
				// div(+, 0+) = +
				// div(+, 0-) = -
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return POSITIVE;
				if (other == NEGATIVE || other == NEGATIVE_OR_EMPTY)
					return NEGATIVE;
				return BOTTOM;
			}

			@Override
			public String toString() {
				return "+";
			}
		},

		NEGATIVE {

			@Override
			Sign minus() {
				return POSITIVE;
			}

			@Override
			Sign add(Sign other) {
				// add(-, bottom) = bottom
				// add(-, top) = top;
				// add(-, +) = top
				// add(-, 0) = -
				// add(-, -) = -
				// add(-, 0+) = top
				// add(-, 0-) = -
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == NEGATIVE || other == EMPTY || other == NEGATIVE_OR_EMPTY)
					return NEGATIVE;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(-, bottom) = bottom
				// mul(-, top) = top;
				// mul(-, +) = -
				// mul(-, 0) = 0
				// mul(-, -) = +
				// mul(-, 0+) = 0-
				// mul(-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE)
					return this;

				if (other == EMPTY)
					return other;

				if (other == NEGATIVE)
					return POSITIVE;

				if (other == POSITIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;
				return POSITIVE_OR_EMPTY;
			}

			@Override
			Sign div(Sign other) {
				// div(-, bottom) = bottom
				// div(-, top) = top;
				// div(-, +) = -
				// div(-, 0) = bottom
				// div(-, -) = +
				// div(-, 0+) = -
				// div(-, 0-) = +
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return NEGATIVE;

				if (other == NEGATIVE || other == NEGATIVE_OR_EMPTY)
					return POSITIVE;

				return BOTTOM;
			}

			@Override
			public String toString() {
				return "-";
			}
		},

		EMPTY {

			@Override
			Sign minus() {
				return EMPTY;
			}

			@Override
			Sign add(Sign other) {
				// add(0, bottom) = bottom
				// add(0, top) = top;
				// add(0, +) = +
				// add(0, 0) = 0
				// add(0, -) = -
				// add(0, 0+) = 0+
				// add(0, 0-) = 0-
				return other;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0, bottom) = bottom
				// mul(0, top) = 0;
				// mul(0, +) = 0
				// mul(0, 0) = 0
				// mul(0, -) = 0
				// mul(0, 0+) = 0
				// mul(0, 0-) = 0
				return other == BOTTOM ? other : EMPTY;
			}

			@Override
			Sign div(Sign other) {
				// div(0, bottom) = bottom
				// div(0, top) = 0;
				// div(0, +) = 0
				// div(0, 0) = bottom
				// div(0, -) = 0
				// div(0, 0+) = 0
				// div(0, 0-) = 0
				return other == EMPTY || other == BOTTOM ? BOTTOM : EMPTY;
			}

			@Override
			public String toString() {
				return "0";
			}
		},

		POSITIVE_OR_EMPTY {

			@Override
			Sign minus() {
				return NEGATIVE_OR_EMPTY;
			}

			@Override
			Sign add(Sign other) {
				// add(0+, bottom) = bottom
				// add(0+, top) = top;
				// add(0+, +) = +
				// add(0+, 0) = 0+
				// add(0+, -) = top
				// add(0+, 0+) = 0+
				// add(0+, 0-) = top
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return other;

				if (other == EMPTY)
					return POSITIVE_OR_EMPTY;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0+, bottom) = bottom
				// mul(0+, top) = top;
				// mul(0+, +) = 0+
				// mul(0+, 0) = 0
				// mul(0+, -) = 0-
				// mul(0+, 0+) = 0+
				// mul(0+, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return POSITIVE_OR_EMPTY;

				if (other == NEGATIVE || other == NEGATIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;

				return EMPTY;
			}

			@Override
			Sign div(Sign other) {
				// div(0+, bottom) = bottom
				// div(0+, top) = top;
				// div(0+, +) = 0+
				// div(0+, 0) = bottom
				// div(0+, -) = 0-
				// div(0+, 0+) = 0+
				// div(0+, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return POSITIVE_OR_EMPTY;

				if (other == NEGATIVE || other == NEGATIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;

				return BOTTOM;
			}

			@Override
			public String toString() {
				return "0+";
			}
		},

		NEGATIVE_OR_EMPTY {

			@Override
			Sign minus() {
				return POSITIVE_OR_EMPTY;
			}

			@Override
			Sign add(Sign other) {
				// add(0-, bottom) = bottom
				// add(0-, top) = top;
				// add(0-, +) = top
				// add(0-, 0) = 0-
				// add(0-, -) = -
				// add(0-, 0+) = top
				// add(0-, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == EMPTY || other == NEGATIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;

				if (other == NEGATIVE)
					return other;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0-, bottom) = bottom
				// mul(0-, top) = top;
				// mul(0-, +) = 0-
				// mul(0-, 0) = 0
				// mul(0-, -) = 0+
				// mul(0-, 0+) = 0-
				// mul(0-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;

				if (other == EMPTY)
					return other;

				return POSITIVE_OR_EMPTY;
			}

			@Override
			Sign div(Sign other) {
				// div(0-, bottom) = bottom
				// div(0-, top) = top;
				// div(0-, +) = 0-
				// div(0-, 0) = bottom
				// div(0-, -) = 0+
				// div(0-, 0+) = 0-
				// div(0-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == POSITIVE_OR_EMPTY)
					return NEGATIVE_OR_EMPTY;

				if (other == EMPTY)
					return BOTTOM;
				return POSITIVE_OR_EMPTY;
			}

			@Override
			public String toString() {
				return "0-";
			}
		};

		abstract Sign minus();

		abstract Sign add(Sign other);

		abstract Sign mul(Sign other);

		abstract Sign div(Sign other);

		@Override
		public abstract String toString();
	}

	@Override
	public ExtSignDomainSolution top() {
		return new ExtSignDomainSolution(Sign.TOP);
	}

	@Override
	public ExtSignDomainSolution bottom() {
		return new ExtSignDomainSolution(Sign.BOTTOM);
	}

	@Override
	public boolean isTop() {
		return this._sign == Sign.TOP;
	}

	@Override
	public boolean isBottom() {
		return this._sign == Sign.BOTTOM;
	}

	@Override
	protected ExtSignDomainSolution evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			int c = (int) constant.getValue();
			if (c == 0)
				return new ExtSignDomainSolution(Sign.EMPTY);
			else if (c > 0)
				return new ExtSignDomainSolution(Sign.POSITIVE);
			else
				return new ExtSignDomainSolution(Sign.NEGATIVE);
		}
		return top();
	}

	@Override
	protected ExtSignDomainSolution evalUnaryExpression(UnaryOperator operator, ExtSignDomainSolution arg,
			ProgramPoint pp) {
		if (operator instanceof NumericNegation)
			return new ExtSignDomainSolution(arg._sign.minus());
		return top();
	}

	@Override
	protected ExtSignDomainSolution evalBinaryExpression(BinaryOperator operator, ExtSignDomainSolution left,
			ExtSignDomainSolution right,
			ProgramPoint pp) {
		if (operator instanceof AdditionOperator)
			return new ExtSignDomainSolution(left._sign.add(right._sign));
		if (operator instanceof DivisionOperator)
			return new ExtSignDomainSolution(left._sign.div(right._sign));
		if (operator instanceof Multiplication)
			return new ExtSignDomainSolution(left._sign.mul(right._sign));
		if (operator instanceof SubtractionOperator)
			return new ExtSignDomainSolution(left._sign.add(right._sign.minus()));
		return top();
	}

	@Override
	protected ExtSignDomainSolution lubAux(ExtSignDomainSolution other) throws SemanticException {
		if (lessOrEqual(other))
			return other;
		if (other.lessOrEqual(this))
			return this;

		if (_sign == Sign.EMPTY) {
			if (other._sign == Sign.POSITIVE)
				return new ExtSignDomainSolution(Sign.POSITIVE_OR_EMPTY);
			else if (other._sign == Sign.NEGATIVE)
				return new ExtSignDomainSolution(Sign.NEGATIVE_OR_EMPTY);
		}

		if (other._sign == Sign.EMPTY) {
			if (_sign == Sign.POSITIVE)
				return new ExtSignDomainSolution(Sign.POSITIVE_OR_EMPTY);
			else if (_sign == Sign.NEGATIVE)
				return new ExtSignDomainSolution(Sign.NEGATIVE_OR_EMPTY);
		}

		return top();
	}

	@Override
	protected ExtSignDomainSolution wideningAux(ExtSignDomainSolution other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomainSolution other) throws SemanticException {
		switch (_sign) {
		case NEGATIVE:
			if (other._sign == Sign.NEGATIVE_OR_EMPTY)
				return true;
			return false;
		case POSITIVE:
			if (other._sign == Sign.POSITIVE_OR_EMPTY)
				return true;
			return false;
		case EMPTY:
			if (other._sign == Sign.POSITIVE_OR_EMPTY || other._sign == Sign.NEGATIVE_OR_EMPTY)
				return true;
			return false;
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int _prime = 31;
		int result = 1;
		result = _prime * result + ((_sign == null) ? 0 : _sign.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtSignDomainSolution other = (ExtSignDomainSolution) obj;
		if (_sign != other._sign)
			return false;
		return true;
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(_sign);
	}
}
