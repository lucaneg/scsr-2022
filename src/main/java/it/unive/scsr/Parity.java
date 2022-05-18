package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Module;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class Parity extends BaseNonRelationalValueDomain<Parity> {

	private static final Parity EVEN = new Parity((byte) 3);
	private static final Parity ODD = new Parity((byte) 2);
	private static final Parity TOP = new Parity((byte) 0);
	private static final Parity BOTTOM = new Parity((byte) 1);

	private final byte parity;

	/**
	 * Builds the parity abstract domain, representing the top of the parity
	 * abstract domain.
	 */
	public Parity() {
		this((byte) 0);
	}

	private Parity(byte parity) {
		this.parity = parity;
	}

	@Override
	public Parity top() {
		return TOP;
	}

	@Override
	public Parity bottom() {
		return BOTTOM;
	}

	@Override
	public DomainRepresentation representation() {
		if (isBottom())
			return Lattice.BOTTOM_REPR;
		if (isTop())
			return Lattice.TOP_REPR;

		String repr;
		if (this == EVEN)
			repr = "Even";
		else
			repr = "Odd";

		return new StringRepresentation(repr);
	}

	@Override
	protected Parity evalNullConstant(ProgramPoint pp) {
		return top();
	}

	@Override
	protected Parity evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			Integer i = (Integer) constant.getValue();
			return i % 2 == 0 ? EVEN : ODD;
		}

		return top();
	}

	private boolean isEven() {
		return this == EVEN;
	}

	private boolean isOdd() {
		return this == ODD;
	}

	@Override
	protected Parity evalUnaryExpression(UnaryOperator operator, Parity arg, ProgramPoint pp) {
		if (operator == NumericNegation.INSTANCE)
			return arg;
		return top();
	}

	@Override
	protected Parity evalBinaryExpression(BinaryOperator operator, Parity left, Parity right, ProgramPoint pp) {
		if (left.isTop() || right.isTop())
			return top();

		if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator)
			if (right.equals(left))
				return EVEN;
			else
				return ODD;
		else if (operator instanceof Multiplication)
			if (left.isEven() || right.isEven())
				return EVEN;
			else
				return ODD;
		else if (operator instanceof DivisionOperator)
			if (left.isOdd())
				return right.isOdd() ? ODD : EVEN;
			else
				return right.isEven() ? EVEN : TOP;
		else if (operator instanceof Module)
			return TOP;

		return TOP;
	}

	@Override
	protected Parity lubAux(Parity other) throws SemanticException {
		return TOP;
	}

	@Override
	protected Parity wideningAux(Parity other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(Parity other) throws SemanticException {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + parity;
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
		Parity other = (Parity) obj;
		if (parity != other.parity)
			return false;
		return true;
	}

	@Override
	protected ValueEnvironment<Parity> assumeBinaryExpression(
			ValueEnvironment<Parity> environment, BinaryOperator operator, ValueExpression left,
			ValueExpression right, ProgramPoint pp) throws SemanticException {
		if (operator == ComparisonEq.INSTANCE) {
			if (left instanceof Identifier)
				environment = environment.assign((Identifier) left, right, pp);
			else if (right instanceof Identifier)
				environment = environment.assign((Identifier) right, left, pp);
			return environment;
		}
		return environment;
	}
}
