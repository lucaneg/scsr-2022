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
	 private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
	 private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);
	enum ExtSign {
		BOTTOM, ZERO, MINUS_ZERO , PLUS_ZERO , MINUS, PLUS, TOP;
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
	                default: return top();
	            }
	        case MINUS:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                case MINUS_ZERO:
	                    return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                default: return top();
	            }
	        case PLUS:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                case PLUS_ZERO:
	                    return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                default: return top();
	            }
	        case MINUS_ZERO:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);
	                case MINUS:
	                	return new ExtSignDomain(ExtSign.MINUS_ZERO);	
	                default: return top();
	            }
	        case PLUS_ZERO:
	            switch (other.sign){
	                case ZERO:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);
	                case PLUS:
	                	return new ExtSignDomain(ExtSign.PLUS_ZERO);	
	                default: return top();
	            }
	        default: return top();  
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
        if (this == obj) return true;
        if (!(obj instanceof ExtSignDomain)) return false;
        ExtSignDomain that = (ExtSignDomain) obj;
        return sign == that.sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign);
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
        if (sign == ExtSign.MINUS)
            return new ExtSignDomain(ExtSign.PLUS);
        else if (sign == ExtSign.PLUS)
            return new ExtSignDomain(ExtSign.MINUS);
        else if (sign == ExtSign.PLUS_ZERO)
            return new ExtSignDomain(ExtSign.MINUS_ZERO);
        else if (sign == ExtSign.MINUS_ZERO)
            return new ExtSignDomain(ExtSign.PLUS_ZERO);
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
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if (value > 0)
                return new ExtSignDomain(ExtSign.PLUS);
            else if (value == 0)
                return new ExtSignDomain(ExtSign.ZERO);
            else
                return new ExtSignDomain(ExtSign.MINUS);
        }
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
						default: return top(); 
					}
				case PLUS_ZERO:
					switch(right.sign) {
						case PLUS:return new ExtSignDomain(ExtSign.PLUS); 
						case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
						case ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
						default: return top();
					}
				case MINUS:
					switch(right.sign) {
						case MINUS: return new ExtSignDomain(ExtSign.MINUS); 
						case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS); 
						case ZERO:return new ExtSignDomain(ExtSign.MINUS); 
						default: return top(); 
					}
				case MINUS_ZERO:
					switch(right.sign) {
						case MINUS:return new ExtSignDomain(ExtSign.MINUS); 
						case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
						case ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
						default: return top(); 
					}
				default: return top(); 
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
					default: return top(); 
				}
			case PLUS:
				switch(right.sign) { 
					case ZERO:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS); 
					default: return top(); 
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case MINUS:return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					default: return top(); 
				}
			case MINUS:
				switch(right.sign) {
					case ZERO:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS); 
					default: return top(); 
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS);
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					case ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					default: return top();
				}
			default: return top(); 
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
					default: return top();
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return top();
				}
			case MINUS:
				switch(right.sign) {
					case MINUS: return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					case PLUS:return new ExtSignDomain(ExtSign.MINUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					default: return top();
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:return new ExtSignDomain(ExtSign.ZERO); 
					default: return top();
				}
			default: return top();
		}
		}

		if(operator instanceof DivisionOperator) { 
			switch (left.sign) {
			case ZERO:
				switch(right.sign) {
					case ZERO:bottom();
					default: return new ExtSignDomain(ExtSign.ZERO); 
				}
			case PLUS:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:bottom();
					case MINUS:return new ExtSignDomain(ExtSign.MINUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					default:return top();
				}
			case PLUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case ZERO:bottom();
					default: return top();
				}
			case MINUS:
				switch(right.sign) {
					case MINUS: return new ExtSignDomain(ExtSign.PLUS); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:bottom();
					case PLUS:return new ExtSignDomain(ExtSign.MINUS);
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					default: return top(); 
				}
			case MINUS_ZERO:
				switch(right.sign) {
					case PLUS:return new ExtSignDomain(ExtSign.MINUS_ZERO); 
					case PLUS_ZERO:return new ExtSignDomain(ExtSign.MINUS_ZERO);
					case MINUS:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case MINUS_ZERO:return new ExtSignDomain(ExtSign.PLUS_ZERO); 
					case ZERO:bottom();
					default: return top();
				}
			default: return top();
		}
		}
		return this.top();
	}
    // IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to get
	// your code to compile. Beware that the code is written expecting that a field named 
	// "sign" containing an enumeration (similar to the one saw during class) exists in 
	// this class: if you name it differently, change also the code below to make it work 
	// by just using the name of your choice instead of "sign"
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}
}