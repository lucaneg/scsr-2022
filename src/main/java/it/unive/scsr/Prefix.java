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

public class Prefix extends BaseNonRelationalValueDomain<Prefix>{
	private static final Prefix BOTTOM = new Prefix(null);
	private static final Prefix TOP = new Prefix("");
	private String pr;
	
	public Prefix(String p) {
		this.pr = p;
	}
	
	public Prefix() {
		this(null);
	}
	

	@Override
	public Prefix top() {
		return TOP;
	}

	@Override
	public Prefix bottom() {
		return BOTTOM;
	}
	
	@Override
	protected Prefix lubAux(Prefix other) throws SemanticException {
		String result = "";
		boolean uguali = true;
		
		if(other.pr.length() < this.pr.length()) {
			for(int i = 0; i < other.pr.length() && uguali; i++) {
				if(this.pr.charAt(i) == other.pr.charAt(i))
					result += this.pr.charAt(i);
				else 
					uguali = false;
			}
		}
		else {
			for(int i = 0; i < this.pr.length() && uguali; i++) {
				if(this.pr.charAt(i) == other.pr.charAt(i)) {
					result += this.pr.charAt(i);
				}
				else 
					uguali = false;
			}
		}
		
		return new Prefix(result);
	}
	
	

	@Override
	protected Prefix glbAux(Prefix other) throws SemanticException {
		if(lessOrEqualAux(other))
			return this;
		if(other.lessOrEqualAux(this))
			return other;
		
		return bottom();
	}
	
	@Override
	protected Prefix wideningAux(Prefix other) throws SemanticException {
		return lubAux(other);
	}
	
	@Override
	protected boolean lessOrEqualAux(Prefix other) throws SemanticException {
		return ((other.pr.length() == this.pr.length() ||  other.pr.length() < this.pr.length()) 
				&& checkCharacter(this.pr, other.pr));
	}
	
	private boolean checkCharacter(String s1, String s2) {
		boolean uguali = true;
		for(int i = 0; i < s2.length() - 1 && uguali; i++)
			if(s2.charAt(i) != s1.charAt(i))
				uguali = false;
		
		return uguali;
	}

	//gestisce new String
	@Override
	protected Prefix evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if(constant.getValue() instanceof String) {
			String v = (String)constant.getValue();
			v.replace("\"","");
			
			return new Prefix(v);
		}
		
		return top();
	}


	@Override
	protected Prefix evalBinaryExpression(BinaryOperator operator, Prefix left, Prefix right, ProgramPoint pp)
			throws SemanticException {
		if(operator instanceof StringConcat) {
			return left;
		}
		
		return top();
	}
	
	//gestisce contains
	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Prefix left, Prefix right,
			ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringContains)
			if(right.pr.length() == 1)
				if(this.pr.contains(right.pr))
					return Satisfiability.SATISFIED;
		
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Prefix evalTernaryExpression(TernaryOperator operator, Prefix left, Prefix middle, Prefix right,
			ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringSubstring) {
			int b = Integer.parseInt(middle.pr.replace("\"", ""));
			int e = Integer.parseInt(right.pr.replace("\"", ""));
			if(e == left.pr.length() || e < left.pr.length()) {
				String result = left.pr.substring(b, e);
				return new Prefix(result);
			}
			
			if(e > left.pr.length() && b < left.pr.length()) {
				String result = left.pr.substring(b, left.pr.length() - 1);
				return new Prefix(result);
			}
				
			return new Prefix("");
		}
		
		return top();
	}

	@Override
	public int hashCode() {
		return Objects.hash(pr);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prefix other = (Prefix) obj;
		return Objects.equals(pr, other.pr);
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(this.pr);
	}

}
