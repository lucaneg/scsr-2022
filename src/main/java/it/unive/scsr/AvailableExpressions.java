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
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressions 
			 	implements DataflowElement<
						DefiniteForwardDataflowDomain<AvailableExpressions>,
						AvailableExpressions>{

	private final ValueExpression expression;
	private final Identifier ident;
	private final CodeLocation point;
	
	public AvailableExpressions(ValueExpression expression, Identifier ident, CodeLocation point) {
		this.expression = expression;
		this.ident = ident;
		this.point = point;
	}
	
	public AvailableExpressions() {
		this(null, null, null);
	}
	
	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		result.add(ident);
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<>();
		if(!expression.getStaticType().isNumericType() && !expression.getStaticType().isBooleanType() && !expression.getStaticType().isStringType() && !expression.getStaticType().isVoidType()) {
			AvailableExpressions ae = new AvailableExpressions(expression, id, pp.getLocation());
			result.add(ae);
		}
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}
	
	private boolean isPresent(Identifier id, String exp) {
		String i = "" + id;
		return exp.contains(i);
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<>();

		for (AvailableExpressions ae : domain.getDataflowElements()) {
			String exp = ae.expression.toString();;
			if (isPresent(id, exp)) {
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
	
	@Override
	public int hashCode() {
		return Objects.hash(expression, ident, point);
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
		return Objects.equals(expression, other.expression) && Objects.equals(ident, other.ident)
				&& Objects.equals(point, other.point);
	}
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope), ident, point);
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope), ident, point);
	}
}

