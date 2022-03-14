package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final Identifier id;
	private final CodeLocation point;
	private final ValueExpression expression;

	public AvailableExpressions() {
		this(null,null, null);
	}

	public AvailableExpressions(ValueExpression expr) {
		this(null,null, expr);
	}

	public AvailableExpressions(Identifier id, CodeLocation point, ValueExpression expr) {
		this.id = id;
		this.point = point;
		this.expression = expr;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AvailableExpressions that = (AvailableExpressions) o;

		return Objects.equals(id, that.id) && Objects.equals(point, that.point) && Objects.equals(expression, that.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, point, expression);
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getVars(this.expression);
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Collection<AvailableExpressions> genRes = new HashSet<>();
		AvailableExpressions AE = new AvailableExpressions(expression);
		genRes.add(AE);
		return genRes;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	private Collection<Identifier> getVars(SymbolicExpression expression) {
		Collection<Identifier> vars = new HashSet<>();
		if (expression.getClass().isInstance(UnaryExpression.class)) {
			if (!expression.getDynamicType().isNumericType())
				vars.add((Identifier) expression);
		}
		if (expression.getClass().isInstance(BinaryExpression.class)) {
			vars.addAll(getVars(((BinaryExpression) expression).getLeft()));
			vars.addAll(getVars(((BinaryExpression) expression).getRight()));
		}
		if (expression.getClass().isInstance(TernaryExpression.class)) {
			vars.addAll(getVars(((TernaryExpression) expression).getLeft()));
			vars.addAll(getVars(((TernaryExpression) expression).getMiddle()));
			vars.addAll(getVars(((TernaryExpression) expression).getRight()));
		}
		return vars;
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Collection<AvailableExpressions> killRes = new HashSet<>();
		Collection<Identifier> vars = getVars(expression);

		for (AvailableExpressions AE: domain.getDataflowElements()) {
			Collection<Identifier> AEVars = getInvolvedIdentifiers();

			if (AEVars.contains(id))
				killRes.add(AE);

		}

		return killRes;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	////////////////

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

