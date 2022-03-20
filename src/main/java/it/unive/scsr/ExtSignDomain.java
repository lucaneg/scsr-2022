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

	// because these are fixpoints
	private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
	private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);

	enum Sign {
		BOTTOM, MINUS, ZERO, PLUS, ZEROMINUS, ZEROPLUS, TOP;
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
		if (leftSign(this) && leftSign(other)) {
			return new ExtSignDomain(Sign.ZEROMINUS);
		} else if (rightSign(this) && rightSign(other)) {
			return new ExtSignDomain(Sign.ZEROPLUS);
		}
		else {
			return TOP;
		}
	}

	protected boolean leftSign(ExtSignDomain check) {
		return check.sign == Sign.MINUS || check.sign == Sign.ZERO || check.sign == Sign.ZEROMINUS;
	}

	protected boolean rightSign(ExtSignDomain check) {
		return check.sign == Sign.PLUS || check.sign == Sign.ZERO || check.sign == Sign.ZEROPLUS;
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		// fixpoint?
		return TOP;
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		if (this.sign == Sign.MINUS){
			return (other.sign == Sign.ZEROMINUS);
		} else if (this.sign == Sign.PLUS){
			return (other.sign == Sign.ZEROPLUS);
		} else if (this.sign == Sign.ZERO){
			return (other.sign == Sign.ZEROMINUS || other.sign == Sign.ZEROPLUS);
		} else {
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
		//zerominus or zeroplus?
		//done. for multiplication
		if (sign == Sign.MINUS)
			return new ExtSignDomain(Sign.PLUS);
		else if (sign == Sign.PLUS)
			return new ExtSignDomain(Sign.MINUS);
		else if (sign == Sign.ZEROMINUS)
			return new ExtSignDomain(Sign.ZEROPLUS);
		else if (sign == Sign.ZEROPLUS)
			return new ExtSignDomain(Sign.ZEROMINUS);
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
		if (operator instanceof AdditionOperator) {
			return binaryAddition(left, right);
		} else if (operator instanceof SubtractionOperator) {
			return binarySubstraction(left, right);
		} else if (operator instanceof Multiplication) {
			return binaryMultiplication(left, right);
		} else if (operator instanceof DivisionOperator) {
			return binaryDivision(left, right);
		}

		return top();
	}

	private ExtSignDomain binaryAddition(ExtSignDomain left, ExtSignDomain right) {
		switch (left.sign) {
		case MINUS:
			switch (right.sign) {
			case ZERO:
			case MINUS:
			case ZEROMINUS:
				return left;
			case PLUS:
			case TOP:
			case ZEROPLUS:
			default:
				return TOP;
			}
		case PLUS:
			switch (right.sign) {
			case PLUS:
			case ZERO:
			case ZEROPLUS:
				return left;
			case MINUS:
			case TOP:
			case ZEROMINUS:
			default:
				return TOP;
			}
		case ZEROMINUS:
		switch (right.sign) {
			case ZERO:
			case ZEROMINUS:
				return left;
			case MINUS:
				return new ExtSignDomain(Sign.MINUS);
			case PLUS:
			case TOP:
			case ZEROPLUS:
			default:
				return TOP;
			}
		case ZEROPLUS:
		switch (right.sign) {
			case ZERO:
			case ZEROPLUS:
				return left;
			case PLUS:
				return new ExtSignDomain(Sign.PLUS);
			case MINUS:
			case TOP:
			case ZEROMINUS:
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
	}

	private ExtSignDomain binarySubstraction(ExtSignDomain left, ExtSignDomain right) {
		switch (left.sign) {
		case MINUS:
			switch (right.sign) {
			case PLUS:
			case ZERO:
			case ZEROPLUS:
				return left;
			case MINUS:
			case TOP:
			case ZEROMINUS:
			default:
				return TOP;
			}
		case PLUS:
			switch (right.sign) {
			case ZERO:
			case MINUS:
			case ZEROMINUS:
				return left;
			case PLUS:
			case TOP:
			case ZEROPLUS:
			default:
				return TOP;
			}
		case ZEROMINUS:
			switch (right.sign) {
			case ZERO:
			case ZEROPLUS:
				return left;
			case PLUS:
				return new ExtSignDomain(Sign.MINUS);
			case MINUS:
			case TOP:
			case ZEROMINUS:
			default:
				return TOP;
			}
		case ZEROPLUS:
			switch (right.sign) {
			case ZERO:
			case ZEROMINUS:
				return left;
			case MINUS:
				return new ExtSignDomain(Sign.PLUS);
			case PLUS:
			case TOP:
			case ZEROPLUS:
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
	}

	private ExtSignDomain binaryMultiplication(ExtSignDomain left, ExtSignDomain right) {
		switch (left.sign) {
		case MINUS:
			return right.negate();
		case PLUS:
			return right;
		case ZEROMINUS:
			switch (right.sign) {
			case MINUS:
			case ZEROMINUS:
				return new ExtSignDomain(Sign.ZEROPLUS);
			case PLUS:
			case ZEROPLUS:
				return new ExtSignDomain(Sign.ZEROMINUS);
			default:
				return right.negate();
			}
		case ZEROPLUS:
			switch (right.sign) {
			case MINUS:
			case ZEROMINUS:
				return new ExtSignDomain(Sign.ZEROMINUS);
			case PLUS:
			case ZEROPLUS:
				return new ExtSignDomain(Sign.ZEROPLUS);
			default:
				return right;
			}
		case TOP:
			return TOP;
		case ZERO:
			return new ExtSignDomain(Sign.ZERO);
		default:
			return TOP;
		}
	}

	private ExtSignDomain binaryDivision(ExtSignDomain left, ExtSignDomain right) {
		if (right.sign == Sign.ZERO || right.sign == Sign.ZEROMINUS || right.sign == Sign.ZEROPLUS)
			return BOTTOM;
		switch (left.sign) {
		case MINUS:
			return right.negate();
		case PLUS:
			return right;
		case ZEROMINUS:
			switch (right.sign){
			case MINUS:
				return new ExtSignDomain(Sign.ZEROPLUS);
			case PLUS:
				return new ExtSignDomain(Sign.ZEROMINUS);
			case TOP:
			case BOTTOM:
			default:
				return right.negate();
			}
		case ZEROPLUS:
			switch (right.sign){
			case MINUS:
				return new ExtSignDomain(Sign.ZEROMINUS);
			case PLUS:
				return new ExtSignDomain(Sign.ZEROPLUS);
			case TOP:
			case BOTTOM:
			default:
				return right;
			}
		case TOP:
			return TOP;
		case ZERO:
			return new ExtSignDomain(Sign.ZERO);
		default:
			return TOP;
		}
	}
}
