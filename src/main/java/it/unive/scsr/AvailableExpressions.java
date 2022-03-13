package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressions
		implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final ValueExpression expression;

	public AvailableExpressions() {
		this(null);
	}

	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getIdentifiersUsed(this.expression);
	}

	public Collection<Identifier> getIdentifiersUsed(ValueExpression expression) {
		Set<Identifier> result = new HashSet<>();

		if (expression == null) {
			return result;
		}

		if (expression instanceof Identifier) {
			result.add((Identifier) expression);
		}

		if (expression instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression) expression;
			result.addAll(getIdentifiersUsed((ValueExpression) unary.getExpression()));
		}

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			result.addAll(getIdentifiersUsed((ValueExpression) binary.getLeft()));
			result.addAll(getIdentifiersUsed((ValueExpression) binary.getRight()));
		}

		if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;
			result.addAll(getIdentifiersUsed((ValueExpression) ternary.getLeft()));
			result.addAll(getIdentifiersUsed((ValueExpression) ternary.getRight()));
			result.addAll(getIdentifiersUsed((ValueExpression) ternary.getMiddle()));
		}

		return result;
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
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();
		AvailableExpressions availableExpressions = new AvailableExpressions(expression);
		if (availableExpressions.getInvolvedIdentifiers().contains(id) == false)
			result.add(availableExpressions);
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		Set<AvailableExpressions> result = new HashSet<>();
		// gett all the domain elements from dataflow
		Set<AvailableExpressions> domainElements = domain.getDataflowElements();

		// loop through them
		for (Iterator<AvailableExpressions> ae = domainElements.iterator(); ae.hasNext();) {

			AvailableExpressions availableExpressions = ae.next();
			// if the expression contains that particular identifier which is being killed,
			// add it to the result set for the graph
			if (availableExpressions.getInvolvedIdentifiers().contains(id)) {
				result.add(availableExpressions);
			}
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
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
}
