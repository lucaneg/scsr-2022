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
import it.unive.scsr.Signs.Sign;

public class ExtSignDomain extends  BaseNonRelationalValueDomain<ExtSignDomain> {

	private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);
	private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);

	enum ExtSign {
		BOTTOM, MINUS, ZERO, PLUS, MINUS_ZERO, PLUS_ZERO, TOP;
	}

	private final ExtSign extSign;

	public ExtSignDomain() {
		this.extSign = ExtSign.TOP; 
	}

	public ExtSignDomain(ExtSign extSign) {
		this.extSign = extSign; 
	}

	@Override
	public int hashCode() {
		return Objects.hash(extSign);
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
		if (extSign != other.extSign)
			return false;
		return true;
	}


	@Override
	public ExtSignDomain top() {
		return TOP;
	}

	@Override
	public ExtSignDomain bottom() {
		return BOTTOM;
	}


	private ExtSignDomain negate() {

		if (this.extSign == ExtSign.MINUS) {
			return new ExtSignDomain(ExtSign.PLUS);
		}else if (this.extSign == ExtSign.PLUS) {
			return new ExtSignDomain(ExtSign.MINUS);
		}else if(this.extSign == ExtSign.MINUS_ZERO) {
			return new ExtSignDomain(ExtSign.PLUS_ZERO);
		}else if(this.extSign == ExtSign.PLUS_ZERO) {
			return new ExtSignDomain(ExtSign.MINUS_ZERO);
		}else {
			return this;
		}

	}

	@Override
	protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
		if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.ZERO) {
			return new ExtSignDomain(ExtSign.MINUS_ZERO); 
		}else if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZERO) {
			return new ExtSignDomain(ExtSign.PLUS_ZERO); 
		}else {
			return new ExtSignDomain(ExtSign.TOP); 
		}
	}


	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.MINUS_ZERO) {
			return true; 
		}else if(this.extSign == ExtSign.ZERO && (other.extSign == ExtSign.MINUS_ZERO ||other.extSign == ExtSign.PLUS_ZERO)) {
			return true; 
		}else if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.PLUS_ZERO) {
			return true;
		}else {
			return false;
		}
	}


	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(extSign);
	}


	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		// TODO Auto-generated method stub
		return TOP;
	}


	@Override
	protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if(constant.getValue() instanceof Integer) {
			Integer value = (Integer) constant.getValue(); 
			if(value>0) {
				return new ExtSignDomain(ExtSign.PLUS);
			}else if(value == 0) {
				return new ExtSignDomain(ExtSign.ZERO);
			}else
				return new ExtSignDomain(ExtSign.MINUS);
		}
		return top();
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
			return evalBinaryExpressionAdditionOperator(left, right);
		} else if (operator instanceof SubtractionOperator) {
			return evalBinaryExpressionSubstraction(left, right);
		} else if (operator instanceof Multiplication) {
			return evalBinaryExpressionMultiplication(left, right);
		} else if (operator instanceof DivisionOperator) {
			if (right.extSign == ExtSign.ZERO || right.extSign == ExtSign.PLUS_ZERO || right.extSign == ExtSign.MINUS_ZERO) {
				return BOTTOM;
			}
			return evalBinaryExpressionMultiplication(left, right);
		}

		return top();
	}

	private ExtSignDomain evalBinaryExpressionMultiplication(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case MINUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS);
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS);
			case TOP:
			default:
				return TOP;
			}
		case PLUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS);
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS);
			case TOP:
			default:
				return TOP;
			}
		case TOP:
			return TOP;
		case ZERO:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case TOP:
			default:
				return TOP;
			}
		case PLUS_ZERO:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case TOP:
			default:
				return TOP;
			}
		case MINUS_ZERO:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case TOP:
			default:
				return TOP;
			}
		default:
			return TOP;
		}
	}

	private ExtSignDomain evalBinaryExpressionSubstraction(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case MINUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS);
			case MINUS:
			case TOP:
			default:
				return TOP;
			}
		case PLUS:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS);
			case ZERO:
				return new ExtSignDomain(ExtSign.PLUS);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS);
			case PLUS:
			case TOP:
			default:
				return TOP;
			}
		case TOP:
			return TOP;

		case ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case PLUS_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case MINUS_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		default:
			return TOP;
		}
	}

	private ExtSignDomain evalBinaryExpressionAdditionOperator(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case MINUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS); 
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS); 
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS); 
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS);
			case PLUS:
			case TOP:
			default:
				return TOP;
			}
		case PLUS:
			switch (right.extSign) {
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.PLUS); 
			case PLUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS); 
			case MINUS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS); 
			case MINUS:
			case TOP:
			default:
				return TOP;
			}
		case TOP:
			return TOP;

		case ZERO:
			switch (right.extSign) {
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(extSign.PLUS_ZERO); 
			case PLUS_ZERO:
				return new ExtSignDomain(extSign.PLUS_ZERO); 
			case MINUS_ZERO:
				return new ExtSignDomain(extSign.MINUS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case PLUS_ZERO:
			switch (right.extSign) {
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(extSign.PLUS_ZERO); 
			case PLUS_ZERO:
				return new ExtSignDomain(extSign.PLUS_ZERO); 
			case MINUS_ZERO:
				return new ExtSignDomain(extSign.ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case MINUS_ZERO:
			switch (right.extSign) {
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(extSign.MINUS_ZERO); 
			case PLUS_ZERO:
				return new ExtSignDomain(extSign.MINUS_ZERO); 
			case MINUS_ZERO:
				return new ExtSignDomain(extSign.ZERO);
			case MINUS:
			case TOP:
			default:
				return TOP;
			}

		default:
			return TOP;
		}
	}

}
