package it.unive.scsr;

import java.util.Objects;

import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;

public class Suffix extends BaseNonRelationalValueDomain<Suffix>{
	private static final Suffix BOTTOM = new Suffix(null);
	private static final Suffix TOP = new Suffix("");
	private String suf;
	
	public Suffix(String suf) {
		this.suf = suf;
	}
	
	public Suffix() {
		this(null);
	}
	
	@Override
	public Suffix top() {
		return TOP;
	}

	@Override
	public Suffix bottom() {
		return BOTTOM;
	}
	
	
	@Override
	protected Suffix lubAux(Suffix other) throws SemanticException {
		String result = "";
		boolean uguali = true;
		
		if(this.suf.equals("") || other.suf.equals("")) 
			return new Suffix("");
		
		
		if(this.suf.length() < other.suf.length()) {
			for(int i = this.suf.length() - 1; i > 0 && uguali; i--)
				if(this.suf.charAt(i) == other.suf.charAt(i))
					result += this.suf.charAt(i);
				else
					uguali = false;
		}
		else {
			for(int i = other.suf.length() - 1; i > 0 && uguali; i--)
				if(this.suf.charAt(i) == other.suf.charAt(i))
					result += this.suf.charAt(i);
				else
					uguali = false;
		}
		
		result = reverse(result);
		return new Suffix(result);
	}
	
	
	@Override
	protected Suffix glbAux(Suffix other) throws SemanticException {
		if(!this.lessOrEqualAux(other))
			return bottom();
		else
			if(this.suf.length() < other.suf.length())
				return this;
			else
				return other;
		
	}
	
	@Override
	protected Suffix wideningAux(Suffix other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(Suffix other) throws SemanticException {
		boolean uguali = true;
		System.out.println("this.suf: " + this.suf);
		System.out.println("other.suf: " + other.suf + "\n");
		if(other.suf.equals("") || this.suf.equals(""))
			return false;
		else {
			for(int i = other.suf.length() - 1; i > 0 && uguali; i--) {
				if(other.suf.charAt(i) != this.suf.charAt(i))
					uguali = false;
			}
		
			return uguali;
		}
		
	}
	
	
	//gestisce new String
	@Override
	protected Suffix evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if(constant.getValue() instanceof String) {
			String v = (String)constant.getValue();
			v.replace("\"","");
			return new Suffix((String) constant.getValue());
		}
		
		return top();
	}

	@Override
	protected Suffix evalBinaryExpression(BinaryOperator operator, Suffix left, Suffix right, ProgramPoint pp)
			throws SemanticException {
		if(operator instanceof StringConcat) {
			return right;
		}
		
		return top();
	}

	@Override
	protected Suffix evalTernaryExpression(TernaryOperator operator, Suffix left, Suffix middle, Suffix right,
			ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringSubstring) {
			return new Suffix("");
		}
		
		return top();
	}
	
	//gestisce contains
	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Suffix left, Suffix right,
			ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringContains)
			if(right.suf.length() == 1)
				if(left.suf.contains(right.suf))
					return Satisfiability.SATISFIED;
		return Satisfiability.UNKNOWN;
	}

	private String reverse(String s) {
		String result = "";
		for(int i = s.length() - 1; i > 0; i--)
			result += s.charAt(i);
		return result;
	}

	

	@Override
	public int hashCode() {
		return Objects.hash(suf);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Suffix other = (Suffix) obj;
		return Objects.equals(suf, other.suf);
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(this.suf);
	}

}
