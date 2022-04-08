package it.unive.scsr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressionsSolution
		implements
		DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressionsSolution>, AvailableExpressionsSolution> {

	private final ValueExpression exp;

	public AvailableExpressionsSolution() {
		this(null);
	}

	private AvailableExpressionsSolution(ValueExpression exp) {
		this.exp = exp;
	}

	@Override
	public String toString() {
		return representation().toString();
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getIdentifierOperands(exp);
	}

	private static Collection<Identifier> getIdentifierOperands(ValueExpression exp) {
		Collection<Identifier> res = new HashSet<>();

		if (exp == null)
			return res;

		if (exp instanceof Identifier)
			res.add((Identifier) exp);

		if (exp instanceof UnaryExpression)
			res.addAll(getIdentifierOperands((ValueExpression) ((UnaryExpression) exp).getExpression()));

		if (exp instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) exp;
			res.addAll(getIdentifierOperands((ValueExpression) binary.getLeft()));
			res.addAll(getIdentifierOperands((ValueExpression) binary.getRight()));
		}

		if (exp instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) exp;
			res.addAll(getIdentifierOperands((ValueExpression) ternary.getLeft()));
			res.addAll(getIdentifierOperands((ValueExpression) ternary.getMiddle()));
			res.addAll(getIdentifierOperands((ValueExpression) ternary.getRight()));
		}

		return res;
	}

	@Override
	public Collection<AvailableExpressionsSolution> gen(Identifier id, ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressionsSolution> domain) {
		Collection<AvailableExpressionsSolution> res = new HashSet<>();
		AvailableExpressionsSolution ae = new AvailableExpressionsSolution(exp);
		if (!ae.getInvolvedIdentifiers().contains(id) && filter(exp))
			res.add(ae);
		return res;
	}

	@Override
	public Collection<AvailableExpressionsSolution> gen(ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressionsSolution> domain) {
		Collection<AvailableExpressionsSolution> res = new HashSet<>();
		AvailableExpressionsSolution ae = new AvailableExpressionsSolution(exp);
		if (filter(exp))
			res.add(ae);
		return res;
	}

	private static boolean filter(ValueExpression exp) {
		if (exp instanceof Identifier)
			return false;
		if (exp instanceof Constant)
			return false;
		if (exp instanceof Skip)
			return false;
		if (exp instanceof PushAny)
			return false;
		return true;
	}

	@Override
	public Collection<AvailableExpressionsSolution> kill(Identifier id, ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressionsSolution> domain) { 
		Collection<AvailableExpressionsSolution> res = new HashSet<>();

		for (AvailableExpressionsSolution ae : domain.getDataflowElements()) {
			Collection<Identifier> ids = getIdentifierOperands(ae.exp);

			if (ids.contains(id))
				res.add(ae);
		}

		return res;
	}

	@Override
	public Collection<AvailableExpressionsSolution> kill(ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressionsSolution> domain) { 
		return Collections.emptyList();
	}

	@Override
	public int hashCode() {
		final int _prime = 31;
		int res = 1;
		res = _prime * res + ((exp == null) ? 0 : exp.hashCode());
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AvailableExpressionsSolution other = (AvailableExpressionsSolution) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		return true;
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(exp);
	}

	@Override
	public AvailableExpressionsSolution pushScope(ScopeToken _scope) throws SemanticException {
		return new AvailableExpressionsSolution((ValueExpression) exp.pushScope(_scope));
	}

	@Override
	public AvailableExpressionsSolution popScope(ScopeToken _scope) throws SemanticException {
		return new AvailableExpressionsSolution((ValueExpression) exp.popScope(_scope));
	}
}
