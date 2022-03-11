package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.ScopeToken;

public class AvailableExpressions implements DataflowElement<
		PossibleForwardDataflowDomain<AvailableExpressions>,
		AvailableExpressions>{

	private final ValueExpression expression;
	
	public AvailableExpressions() {
		this(null);
	}

	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AvailableExpressions other = (AvailableExpressions) obj;
		return Objects.equals(expression, other.expression);
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		/*Set<Identifier> result = new HashSet<>();
		result.add(id);
		return result;*/
		return null;
	}

	// the interesting part
	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, 
			PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();
		AvailableExpressions rd = new AvailableExpressions(expression);
		result.add(rd);
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();

		for (AvailableExpressions rd : domain.getDataflowElements())
			if (rd.expression.equals(expression))
				result.add(rd);

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	
	// code outside of the scope of the course
	
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
}

