package it.unive.scsr;

import java.util.Objects;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;


public class ExtSignDomain extends  BaseNonRelationalValueDomain<ExtSignDomain> {

	
	public static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);
	public static final ExtSignDomain NEG_ZERO = new ExtSignDomain(ExtSign.NEG_ZERO); // Negative case of zero (-)
	public static final ExtSignDomain POS_ZERO = new ExtSignDomain(ExtSign.POS_ZERO);  // Positive case of zero (+)
	public static final ExtSignDomain ZERO = new ExtSignDomain(ExtSign.ZERO);
	public static final ExtSignDomain MINUS = new ExtSignDomain(ExtSign.MINUS);
	public static final ExtSignDomain  PLUS= new ExtSignDomain(ExtSign.PLUS);
	public static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);


	enum ExtSign {
		TOP,NEG_ZERO, POS_ZERO,ZERO,MINUS,PLUS,BOTTOM;
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


	public ExtSignDomain negate() {

		if (this.extSign == ExtSign.MINUS) {
			return new ExtSignDomain(ExtSign.PLUS);
		}else if (this.extSign == ExtSign.PLUS) {
			return new ExtSignDomain(ExtSign.MINUS);
		}else if(this.extSign == ExtSign.NEG_ZERO) {
			return new ExtSignDomain(ExtSign.POS_ZERO);
		}else if(this.extSign == ExtSign.POS_ZERO) {
			return new ExtSignDomain(ExtSign.NEG_ZERO);
		}else {
			return this;
		}

	}




	@Override
	public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if(constant.getValue() instanceof Integer) {
			Integer operator_val = (Integer) constant.getValue(); 
			if(operator_val>0) {
				return new ExtSignDomain(ExtSign.PLUS);	
			}
			if(operator_val>=0) {
				return new ExtSignDomain(ExtSign.POS_ZERO);		
			}
			else if(operator_val == 0) {
				return new ExtSignDomain(ExtSign.ZERO);
			}else if(operator_val<0)
				return new ExtSignDomain(ExtSign.MINUS);
		}
		else
			{ 
			return new ExtSignDomain(ExtSign.NEG_ZERO);
			}
		return top();
	}      

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();

        return top();
    }
	
	
	public ExtSignDomain evalBinaryExpression(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case MINUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
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
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
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
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case TOP:
			default:
				return TOP;
			}
		case POS_ZERO:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case TOP:
			default:
				return TOP;
			}
		case NEG_ZERO:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case MINUS:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case TOP:
			default:
				return TOP;
			}
		default:
			return TOP;
		}
	}

	public ExtSignDomain SubstractionOperator(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case MINUS:
			switch (right.extSign) {
			case ZERO:
				return new ExtSignDomain(ExtSign.MINUS);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.MINUS);
			case NEG_ZERO:
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
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.PLUS);
			case NEG_ZERO:
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
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case POS_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case TOP:
			default:
				return TOP;
			}
		case NEG_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
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

	public ExtSignDomain AdditionOperator(ExtSignDomain left, ExtSignDomain right) {
		switch (left.extSign) {
		case TOP:
	    {
			return new ExtSignDomain(ExtSign.TOP); 
		}
		
		case MINUS:
		    {
		    	return left.negate(); 
			}
		    
		case POS_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO); 
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.POS_ZERO); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
			}
			
		case ZERO:
		   {
				return new ExtSignDomain(ExtSign.ZERO); 
			}
		   

		case NEG_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.POS_ZERO); 
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO); 
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
			}
		case PLUS:
		{
			return right;
			}

		default:
			return TOP;
		}
	}
	
	public ExtSignDomain MultiplicationOperator(ExtSignDomain left, ExtSignDomain right) {
		switch (right.extSign) {
		case MINUS:
		    {
		    	return right.negate(); 
			}
		    
		case POS_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO); 
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.POS_ZERO); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
			}
			
		case ZERO:
		   {
				return new ExtSignDomain(ExtSign.ZERO); 
			}
		   

		case NEG_ZERO:
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.POS_ZERO); 
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO); 
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.NEG_ZERO); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
			}
		case PLUS:
		{
			return right;
			}
		
		case TOP:{
			return new ExtSignDomain(ExtSign.TOP);
		}

		default:
			return TOP;
		}
	}
	
	public ExtSignDomain DivisionOperator(ExtSignDomain left, ExtSignDomain right) {
		switch (right.extSign) {
		case MINUS:
		    {
				switch (right.extSign) {
				case MINUS:
					return new ExtSignDomain(ExtSign.PLUS); 
				case ZERO:
					return new ExtSignDomain(ExtSign.BOTTOM);
				case POS_ZERO:
					return new ExtSignDomain(ExtSign.BOTTOM);
				case NEG_ZERO:
					return new ExtSignDomain(ExtSign.BOTTOM);
				case PLUS:
					return new ExtSignDomain(ExtSign.MINUS); 
				case TOP:
					return new ExtSignDomain(ExtSign.TOP);
				default:
					return TOP;
				}
			}
		    
		case POS_ZERO:
             {
			return new ExtSignDomain(ExtSign.BOTTOM); 
			}
			
		case ZERO:
		   {
				return new ExtSignDomain(ExtSign.BOTTOM); 
			}
		   

		case NEG_ZERO:
		   {
				return new ExtSignDomain(ExtSign.BOTTOM); 

			}
		case PLUS:
		{
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case ZERO:
				return new ExtSignDomain(ExtSign.ZERO);
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.BOTTOM);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.BOTTOM);
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
			}
			}
		case TOP:
		{
			switch (right.extSign) {
			case MINUS:
				return new ExtSignDomain(ExtSign.MINUS); 
			case ZERO:
				return left;
			case POS_ZERO:
				return new ExtSignDomain(ExtSign.POS_ZERO);
			case NEG_ZERO:
				return new ExtSignDomain(ExtSign.NEG_ZERO);
			case PLUS:
				return new ExtSignDomain(ExtSign.PLUS); 
			case TOP:
				return new ExtSignDomain(ExtSign.TOP);
			default:
				return TOP;
		}
		}
		default:
			return TOP;
		}
	}

	@Override
	public DomainRepresentation representation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		// TODO Auto-generated method stub
		return false;
	}

}