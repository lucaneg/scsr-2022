package it.unive.scsr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {
	
	private Set<Identifier> identifiers; 
	private ValueExpression expression;
	private ProgramPoint programPoint; 

	
	public AvailableExpressions() {
		
		this.identifiers = new HashSet<Identifier>(); 
		this.expression = null;
		this.programPoint = null;
		
	}

	
	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
		this.identifiers  = new HashSet<Identifier>(); 
	}
		
	
	public AvailableExpressions(Set<Identifier> identifiers, ValueExpression expression, ProgramPoint programPoint) {
		super();
		this.identifiers = identifiers;
		this.expression = expression;
		this.programPoint = programPoint;
	}


	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope));
	}

	
	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> results = new HashSet<>();
		results.addAll(this.identifiers);
		return results;
	}


	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<AvailableExpressions>();
		Set<Identifier> identifiers = new HashSet<Identifier>();
		identifiers.add(id);
		result.add(new AvailableExpressions(identifiers,expression, pp)); 
		
		return result;
	}

	
	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<AvailableExpressions>();
		result.add(new AvailableExpressions(new HashSet<>(),expression, pp)); 
		return result; 
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<AvailableExpressions>();
		for(AvailableExpressions availableExpression: domain.getDataflowElements()) {
			 
			if(availableExpression.getInvolvedIdentifiers().contains(id)) { 
				result.add(availableExpression);
			}
		
		}
		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return  new HashSet<AvailableExpressions>();

	}


}

