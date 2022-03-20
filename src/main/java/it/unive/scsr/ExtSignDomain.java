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
	
	private final ExtSign sign;
	enum ExtSign {
		BOTTOM, ZERO, MINUS_ZERO, PLUS_ZERO, MINUS, PLUS, TOP;
	}
	
	//CONSTRUCTORS
	public ExtSignDomain() {
		this.sign = ExtSign.TOP;
	}
	public ExtSignDomain(ExtSign value) {
		this.sign = value;
	}
	////////////////////////////
	
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}

	@Override
	public ExtSignDomain top() {
		return new ExtSignDomain(ExtSign.TOP); 
	}

	@Override
	public ExtSignDomain bottom() {
		return new ExtSignDomain(ExtSign.BOTTOM); 
	}

	@Override
	protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException { 
		switch (this.sign){
	        case ZERO:
	            switch(other.sign){
	                case MINUS:
	                    return new ExtSignDomain(ExtSign.MINUS_ZERO); 
	                case PLUS:
	                    return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                case MINUS_ZERO:
	                    return new ExtSignDomain(ExtSign.MINUS_ZERO);    
	                case PLUS_ZERO:
	                    return new ExtSignDomain(ExtSign.PLUS_ZERO);   
	                default: return new ExtSignDomain(ExtSign.TOP);
	            }
	        case MINUS:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                case MINUS_ZERO:
	                    return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                default: return new ExtSignDomain(ExtSign.TOP);
	            }
	        case PLUS:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                case PLUS_ZERO:
	                    return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                default: return new ExtSignDomain(ExtSign.TOP);
	            }
	        case MINUS_ZERO:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                case MINUS:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);	
	                default: return new ExtSignDomain(ExtSign.TOP);
	            }
	        case PLUS_ZERO:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                case PLUS:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);	
	                default: return new ExtSignDomain(ExtSign.TOP);
	            }
	        default: return new ExtSignDomain(ExtSign.TOP);  
	    }
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		switch(this.sign){
			case MINUS:
				switch(other.sign) {
					case MINUS_ZERO:
						return true;
					case MINUS:
						return true;
					default:
						return false;
				}
			case PLUS:
				switch(other.sign) {
					case PLUS_ZERO:
						return true;
					case PLUS:
						return true;
					default:
						return false;
				}
			case ZERO:
				switch(other.sign) {
					case PLUS_ZERO:
						return true;
					case MINUS_ZERO:
						return true;
					case ZERO:
						return true;
					default:
						return false;
				}
			default:
				return false;
		}
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
		if (this.sign != other.sign)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sign);
	}
	
	@Override
	protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
            int value= (Integer) constant.getValue();
            if(value > 0)
                return new ExtSignDomain(ExtSignDomain.ExtSign.PLUS);
            else if (value == 0)
                return new ExtSignDomain(ExtSignDomain.ExtSign.ZERO);
            else
                return new ExtSignDomain(ExtSignDomain.ExtSign.MINUS);
        }else return top();
	}
	
	private ExtSignDomain negate() {
		if (this.sign == ExtSign.MINUS)
			return new ExtSignDomain(ExtSign.PLUS);
		else if (this.sign == ExtSign.PLUS)
			return new ExtSignDomain(ExtSign.MINUS);
		else if(this.sign == ExtSign.PLUS_ZERO)
			return new ExtSignDomain(ExtSign.MINUS_ZERO);
		else if(this.sign == ExtSign.MINUS_ZERO)
			return new ExtSignDomain(ExtSign.PLUS_ZERO);
		else
			return this;
		
	}
	
	@Override
	protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp)
			throws SemanticException {
		if(operator instanceof NumericNegation)
			return arg.negate(); 
		return top();
	}
	
	@Override
	protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
			ProgramPoint pp) throws SemanticException {
		if(operator instanceof AdditionOperator) {
			switch (left.sign) {
				case ZERO:
					return right;
				case PLUS:
					switch(right.sign) {
						case PLUS:return new ExtSignDomain(ExtSign.PLUS); 
						case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS); 
						case ZERO:return new ExtSignDomain(ExtSign.PLUS); 
						default: return new ExtSignDomain(ExtSign.TOP); 
					}
				case PLUS_ZERO:
					switch(right.sign) {
						case PLUS:return new ExtSignDomain(ExtSign.PLUS); 
						case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
						case ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
						default: return new ExtSignDomain(ExtSign.TOP);
					}
				case MINUS:
					switch(right.sign) {
						case MINUS: return new ExtSignDomain(ExtSign.MINUS); 
						case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS); 
						case ZERO:return new ExtSignDomain(ExtSign.MINUS); 
						default: return new ExtSignDomain(ExtSign.TOP); 
					}
				case MINUS_ZERO:
					switch(right.sign) {
						case MINUS:return new ExtSignDomain(ExtSign.MINUS); 
						case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
						case ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
						default: return new ExtSignDomain(ExtSign.TOP); 
					}
				default: return new ExtSignDomain(ExtSign.TOP); 
			}
		}
		if(operator instanceof SubtractionOperator) {
			switch (left.sign) {
			case ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS);  
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case PLUS:
				switch(right.sign) { 
					case ZERO:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case MINUS:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case MINUS:
				switch(right.sign) {
					case ZERO:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS);
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					case ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					default: return new ExtSignDomain(ExtSign.TOP);
				}
			default: return new ExtSignDomain(ExtSign.TOP); 
			}
		}
		if(operator instanceof Multiplication) {
			switch (left.sign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO); 
			case PLUS:
				switch(right.sign) {
					case MINUS: return new ExtSignDomain(ExtSign.MINUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case PLUS:return new ExtSignDomain(ExtSign.PLUS);
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO);
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP);
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP);
				}
			case MINUS:
				switch(right.sign) {
					case MINUS: return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					case PLUS:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return new ExtSignDomain(ExtSign.TOP);
				}
			default: return new ExtSignDomain(ExtSign.TOP); 
		}
		}

		if(operator instanceof DivisionOperator) { 
			switch (left.sign) {
			case ZERO:
				switch(right.sign) {
					case ZERO:return new ExtSignDomain(ExtSign.BOTTOM); 
					default: return new ExtSignDomain(ExtSign.ZERO); 
				}
			case PLUS:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.BOTTOM); 
					case MINUS:return new ExtSignDomain(ExtSign.MINUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					default:return new ExtSignDomain(ExtSign.TOP); 
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.BOTTOM); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case MINUS:
				switch(right.sign) {
					case MINUS: return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.BOTTOM); 
					case PLUS:return new ExtSignDomain(ExtSign.MINUS);
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					case MINUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.BOTTOM); 
					default: return new ExtSignDomain(ExtSign.TOP); 
				}
			default: return new ExtSignDomain(ExtSign.TOP); 
		}
		}
		return this.top();
	}
}
