package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
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



public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>,AvailableExpressions>{

	private final ValueExpression expression;

	public AvailableExpressions() {
		this.expression = null;
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

		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		AvailableExpressions other = (AvailableExpressions) obj;
		return Objects.equals(this.expression, other.expression);

	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getInvolvedIdentifiers(this.expression);
	}

	public Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
		Set<Identifier> result = new HashSet<>();

		if (expression instanceof Identifier) {
			result.add((Identifier) expression);
		} else if (expression instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression) expression;
			result.addAll(getInvolvedIdentifiers((ValueExpression) unary.getExpression()));
		} else if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			result.addAll(getInvolvedIdentifiers((ValueExpression) binary.getLeft()));
			result.addAll(getInvolvedIdentifiers((ValueExpression) binary.getRight()));
		} else if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;
			result.addAll(getInvolvedIdentifiers((ValueExpression) ternary.getLeft()));
			result.addAll(getInvolvedIdentifiers((ValueExpression) ternary.getRight()));
			result.addAll(getInvolvedIdentifiers((ValueExpression) ternary.getMiddle()));
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();
		AvailableExpressions ae = new AvailableExpressions(expression);
		if (!ae.getInvolvedIdentifiers().contains(id))
				result.add(ae);
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

		for(AvailableExpressions ae : domain.getDataflowElements()) {
			if (ae.expression.equals(expression)) {
				result.add(ae);
			}
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}
	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to get
	// your code to compile. Beware that the code is written expecting that a field named 
	// "expression" of type ValueExpression exists in this class: if you name it differently,
	// change also the code below to make it work by just using the name of your choice instead
	// of "expression". If you don't have a field of type ValueExpression in your solution,
	// then you should make sure that what you are doing is correct :)
	
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

