package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.management.ValueExp;

import com.fasterxml.jackson.annotation.JacksonInject.Value;

import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.ExpressionVisitor;
import it.unive.lisa.symbolic.value.Identifier;

public class AvailableExpressions
		implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final ValueExpression expression;
	private final Identifier id;

	public AvailableExpressions() {
		this(null);
	}

	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
		this.id = null;
	}

	public AvailableExpressions(Identifier id, ValueExpression expression) {
		this.id = id;
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
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();
		AvailableExpressions ae = new AvailableExpressions(id, expression);
		result.add(ae);
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		// Set<AvailableExpressions> result = new HashSet<>();
		// AvailableExpressions ae = new AvailableExpressions(expression);
		// result.add(ae);
		return new HashSet<>();
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();
		for (AvailableExpressions ae : domain.getDataflowElements()) {
			//if (ae.id != null && ae.expression.accept()) {
				result.add(ae);
			//}
		}

		return result;
		//return new HashSet<>();
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
				
		// Set<AvailableExpressions> result = new HashSet<>();
		// for (AvailableExpressions ae : domain.getDataflowElements()) {
		// 	if (ae.expression.equals(expression)) {
		// 		result.add(ae);
		// 	}
		// }

		// return result;
		return new HashSet<>();
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return null;
	}
}
