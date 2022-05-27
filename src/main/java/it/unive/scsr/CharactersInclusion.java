package it.unive.scsr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

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

public class CharactersInclusion extends BaseNonRelationalValueDomain<CharactersInclusion>{
	private Set<Character> certainlyContainedCharacter;
	private Set<Character> maybeContainedCharacter;
	
	private static Set<Character> alfabeto = new HashSet<>();
	
	
	public CharactersInclusion() {
		this(null, alfabeto);
	}
	
	public CharactersInclusion(Set<Character> certainlyContainedCharacter, Set<Character> maybeContainedCharacter) {
		this.certainlyContainedCharacter = certainlyContainedCharacter;
		this.maybeContainedCharacter = maybeContainedCharacter;
		for(char c : "abcdefghijklmnopqrstuvwsyz".toCharArray())
			CharactersInclusion.alfabeto.add(c);
	}

	@Override
	public CharactersInclusion top() {
		return new CharactersInclusion(null, alfabeto);
	}
	
	@Override
	public CharactersInclusion bottom() {
		return new CharactersInclusion(null, null);
	}
	
	

	@Override
	public boolean isTop() {
		if(this.maybeContainedCharacter != null)
			return this.certainlyContainedCharacter == null && this.maybeContainedCharacter.equals(alfabeto);
		else
			return false;
	}
	

	@Override
	public boolean isBottom() {
		return this.certainlyContainedCharacter == null && this.maybeContainedCharacter == null;
	}

	@Override
	protected CharactersInclusion lubAux(CharactersInclusion other) throws SemanticException {
		Set<Character> intersection = new HashSet<>(this.certainlyContainedCharacter);
		Set<Character> union = new HashSet<>(this.maybeContainedCharacter);
		
		intersection.retainAll(other.certainlyContainedCharacter);
		union.addAll(other.maybeContainedCharacter);
		
		return new CharactersInclusion(intersection, union);
	}

	@Override
	protected CharactersInclusion glbAux(CharactersInclusion other) throws SemanticException {
		if(containedEqual(this.certainlyContainedCharacter, other.maybeContainedCharacter) 
			&& containedEqual(other.certainlyContainedCharacter, this.maybeContainedCharacter)) {
			Set<Character> union = this.certainlyContainedCharacter;
			Set<Character> intersection = this.maybeContainedCharacter;
			
			union.addAll(other.certainlyContainedCharacter);
			intersection.retainAll(other.maybeContainedCharacter);
			
			return new CharactersInclusion(union, intersection);
		}
		else
			return bottom();
		
	}

	@Override
	protected CharactersInclusion wideningAux(CharactersInclusion other) throws SemanticException {
		return lubAux(other);
	}
	
	@Override
	protected boolean lessOrEqualAux(CharactersInclusion other) throws SemanticException {
		return (containedEqual(other.certainlyContainedCharacter, this.certainlyContainedCharacter) 
				&& containedEqual(this.maybeContainedCharacter, other.maybeContainedCharacter));
	}
	
	private boolean containedEqual(Set<Character> s1, Set<Character> s2) {
		boolean result = true;
		Iterator<Character> it = s1.iterator();
		
		while(it.hasNext() && result) {
			char c = it.next();
			if(!s2.contains(c))
				result = false;
		}
		
		return result;
	}
	
	//handle new String
	protected CharactersInclusion evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException{
		if(constant.getValue() instanceof String) {
			String v = (String) constant.getValue();
			v.replace("\"", "");
			Set<Character> result = new HashSet<>();
			for(char c : v.toCharArray())
				result.add(c);
			return new CharactersInclusion(result, result);
		}
		return top();
	}
	
	@Override
	protected CharactersInclusion evalBinaryExpression(BinaryOperator operator, CharactersInclusion left, 
			CharactersInclusion right, ProgramPoint pp) throws SemanticException{
		if(operator instanceof StringConcat) {
			Set<Character> unionCertainlyContained = new HashSet<>(left.certainlyContainedCharacter);
			Set<Character> unionMaybeContained = new HashSet<>(left.maybeContainedCharacter);
			
			unionCertainlyContained.addAll(right.certainlyContainedCharacter);
			unionMaybeContained.addAll(right.maybeContainedCharacter);
			
			return new CharactersInclusion(unionCertainlyContained, unionMaybeContained);
			
		}
		
		return top();
	}
	
	//handle contains
	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, CharactersInclusion left,
			CharactersInclusion right, ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringContains) {
			if(!right.isTop() && !left.isTop())
				if((right.certainlyContainedCharacter.size() == 1 && right.maybeContainedCharacter.size() == 1) 
						&& right.certainlyContainedCharacter.equals(right.maybeContainedCharacter)) {
					char c = right.certainlyContainedCharacter.iterator().next();
					if(left.certainlyContainedCharacter.contains(c))
						return Satisfiability.SATISFIED;
					if(!left.maybeContainedCharacter.contains(c))
						return Satisfiability.NOT_SATISFIED;
				}
		}
		
		return Satisfiability.UNKNOWN;
	}
	
	//use "" on indices when testing this
	@Override
	protected CharactersInclusion evalTernaryExpression(TernaryOperator operator, CharactersInclusion left,
			CharactersInclusion middle, CharactersInclusion right, ProgramPoint pp) throws SemanticException {
		if(operator instanceof StringSubstring) {
			Set<Character> l = new HashSet<Character>(left.maybeContainedCharacter);
			CharactersInclusion result = new CharactersInclusion(null, l);
			return result;
		}
		return top();
	}

	@Override
	public DomainRepresentation representation() {
		Pair<Set<Character>, Set<Character>> p = new Pair<>(certainlyContainedCharacter ,maybeContainedCharacter);
		return new StringRepresentation(p);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(alfabeto, certainlyContainedCharacter, maybeContainedCharacter);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CharactersInclusion other = (CharactersInclusion) obj;
		return Objects.equals(alfabeto, CharactersInclusion.alfabeto)
				&& Objects.equals(certainlyContainedCharacter, other.certainlyContainedCharacter)
				&& Objects.equals(maybeContainedCharacter, other.maybeContainedCharacter);
	}
}
