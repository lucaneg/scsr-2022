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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain>{
	
	private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);
	private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);
	private static final ExtSignDomain ZEROMINUS = new ExtSignDomain(ExtSign.ZEROMINUS);
	private static final ExtSignDomain ZEROPLUS = new ExtSignDomain(ExtSign.ZEROPLUS);
	private static final ExtSignDomain ZERO = new ExtSignDomain(ExtSign.ZERO);
	private static final ExtSignDomain MINUS = new ExtSignDomain(ExtSign.MINUS);
	private static final ExtSignDomain PLUS = new ExtSignDomain(ExtSign.PLUS);
	
	enum ExtSign{
		BOTTOM, MINUS, ZERO, PLUS, ZEROMINUS, ZEROPLUS, TOP;
	}
	
	private final ExtSign extSign;
	
	public ExtSignDomain() {
		this(ExtSign.TOP);
	}
	
	public ExtSignDomain(ExtSign extSign) {
		this.extSign = extSign;
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
		
		if(this.extSign ==  ExtSign.MINUS) {
			if(other.extSign ==  ExtSign.ZERO)
				return ZEROMINUS;
			if(other.extSign == ExtSign.PLUS)
				return TOP;
		}
		
		if(this.extSign == ExtSign.ZERO) {
			if(other.extSign == ExtSign.MINUS)
				return ZEROMINUS;
			if(other.extSign == ExtSign.PLUS)
				return ZEROPLUS;
		}
		
		if(this.extSign == ExtSign.PLUS) {
			if(other.extSign == ExtSign.ZERO)
				return ZEROPLUS;
			if(other.extSign == ExtSign.MINUS)
				return TOP;
		}
		
		return TOP;
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		return TOP;
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		if(this.extSign == ExtSign.MINUS  && other.extSign == ExtSign.ZEROMINUS)
			return true;
		
		if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZEROPLUS)
			return true;
		
		if(this.extSign == ExtSign.ZERO && (other.extSign == ExtSign.ZEROMINUS || other.extSign == ExtSign.ZEROPLUS))
			return true;
		
		if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZEROMINUS)
			return true;
		
		if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.ZEROPLUS)
			return true;
		
		return false;
	}
	
	@Override
	protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException{
		if(constant.getValue() instanceof Integer) {
			int v = (Integer) constant.getValue();
			if(v > 0 )
				return new ExtSignDomain(ExtSign.PLUS);
			else if (v == 0)
				return new ExtSignDomain(ExtSign.ZERO);
			else if(v < 0)
				return new ExtSignDomain(ExtSign.MINUS);
			else if(v >= 0)
				return new ExtSignDomain(ExtSign.ZEROPLUS);
			else 
				return new ExtSignDomain(ExtSign.ZEROMINUS);
		}
		return top();
	}
	
	private ExtSignDomain negate() {
		if (this.extSign == ExtSign.MINUS)
			return new ExtSignDomain(ExtSign.PLUS);
		else if (this.extSign == ExtSign.PLUS)
			return new ExtSignDomain(ExtSign.MINUS);
		else if(this.extSign == ExtSign.ZEROPLUS)
			return new ExtSignDomain(ExtSign.ZEROMINUS);
		else if(this.extSign == ExtSign.ZEROMINUS)
			return new ExtSignDomain(ExtSign.ZEROPLUS);
		else
			return this;
		
	}
	
	@Override
	protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException{
		if(operator instanceof NumericNegation)
			return arg.negate();
		return top();
	}
	
	@Override
	protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException{
		if(operator instanceof AdditionOperator) {
			switch(left.extSign) {
			case MINUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return MINUS;
				case PLUS:
					return TOP;
				case ZEROPLUS:
					return TOP;
				case ZEROMINUS:
					return MINUS;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case PLUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return TOP;
				case PLUS:
					return right;
				case ZEROPLUS:
					return left;
				case ZEROMINUS:
					return TOP;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZERO:;
				return right;
			case ZEROPLUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return TOP;
				case PLUS:
					return PLUS;
				case ZEROPLUS:
					return left;
				case ZEROMINUS:
					return TOP;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZEROMINUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return MINUS;
				case PLUS:
					return TOP;
				case ZEROPLUS:
					return TOP;
				case ZEROMINUS:
					return left;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case TOP:
				return TOP;
			default:
				return TOP;
			}
		}
		else if(operator instanceof SubtractionOperator) {
			switch(left.extSign) {
			case MINUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return TOP;
				case PLUS:
					return left;
				case ZEROPLUS:
					return MINUS;
				case ZEROMINUS:
					return TOP;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case PLUS:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return left;
				case PLUS:
					return TOP;	
				case ZEROPLUS:
					return TOP;
				case ZEROMINUS:
					return PLUS;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZERO:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
					return right.negate();
				case PLUS:
					return right.negate();
				case ZEROPLUS:
					return right.negate();
				case ZEROMINUS:
					return right.negate();
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZEROPLUS:
				switch(right.extSign){
				case ZERO:
					return left;
				case MINUS:
					return PLUS;
				case PLUS:
					return TOP;
				case ZEROPLUS:
					return TOP;
				case ZEROMINUS:
					return ZEROPLUS;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZEROMINUS:
				switch(right.extSign) {
				case ZERO: 
					return left;
				case MINUS:
					return TOP;
				case PLUS:
					return MINUS;
				case ZEROPLUS:
					return ZEROMINUS;
				case ZEROMINUS:
					return TOP;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case TOP:
				return TOP;
			default:
				return TOP;
			}
		}
		else if(operator instanceof Multiplication) {
			switch(left.extSign) {
			case MINUS:
				switch(right.extSign) {
				case ZERO:
					return ZERO;
				default:
					return right.negate();
				}
			case PLUS:
				return right;
			case ZERO:
				return ZERO;
			case ZEROPLUS:
				switch(right.extSign) {
				case ZERO:
					return ZERO;
				case MINUS:
					return ZEROMINUS;
				case PLUS:
					return ZEROPLUS;
				case ZEROPLUS:
					return ZEROPLUS;
				case ZEROMINUS:
					return ZEROPLUS;
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case ZEROMINUS:
				switch(right.extSign) {
				case ZERO:
					return ZERO;
				case MINUS:
					return ZEROPLUS;
				case PLUS:
					return ZEROMINUS;
				case ZEROPLUS:
					return ZEROMINUS;
				case ZEROMINUS:
					return ZEROPLUS;
				case TOP:
					return TOP;
				default: 
					return TOP;
				}
			case TOP:
				return TOP;
			default:
				return TOP;
			}
		}
		else if(operator instanceof DivisionOperator) {
			switch(left.extSign) {
			case MINUS:
				switch(right.extSign) {
				case ZERO:
					return BOTTOM;
				case MINUS:
					return PLUS;
				case PLUS:
					return MINUS;
				case ZEROPLUS:
					return BOTTOM; //check this
				case ZEROMINUS:
					return BOTTOM; //check this
				case TOP:
					return TOP;
				default:
					return TOP;
				}
			case PLUS:
				switch(right.extSign) {
				case ZERO:
					return ZERO;
				case MINUS:
					return MINUS;
				case PLUS:
					return PLUS;
				case ZEROPLUS:
					return BOTTOM; //check this
				case ZEROMINUS:
					return BOTTOM; //check this
				case TOP:
					return TOP;
				default: 
					return TOP;		
				}
			case ZERO:
				return BOTTOM;
			case ZEROPLUS:
				return BOTTOM; //check this
			case ZEROMINUS:
				return BOTTOM; //check this
			case TOP:
				switch(right.extSign) {
				case ZERO:
					return left;
				case MINUS:
				case PLUS:
				case ZEROPLUS:
				case ZEROMINUS:
				case TOP:
				default:
					return TOP;
				}
			default:
				return TOP;
			}
		}
		
		return top();
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
		return extSign == other.extSign;
	}
	
	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to get
	// your code to compile. Beware that the code is written expecting that a field named 
	// "sign" containing an enumeration (similar to the one saw during class) exists in 
	// this class: if you name it differently, change also the code below to make it work 
	// by just using the name of your choice instead of "sign"
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(extSign);
	}
}
