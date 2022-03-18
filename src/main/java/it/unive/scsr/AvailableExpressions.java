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
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;

public class AvailableExpressions 
			 	implements DataflowElement<
						DefiniteForwardDataflowDomain<AvailableExpressions>,
						AvailableExpressions>{

	private final ValueExpression expression;
	private final Identifier id;
	private final CodeLocation point;
	
	public AvailableExpressions(ValueExpression expression, Identifier id, CodeLocation point) {
		this.expression = expression;
		this.id = id;
		this.point = point;
	}
	
	public AvailableExpressions() {
		this(null, null, null);
	}
	
	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		result.add(id);
		return result;
	}
	
	private boolean canBeAdded(SymbolicExpression exp) {
		
		if(exp instanceof Constant) {
			return false;
		}
		
		if(exp instanceof Variable) {
			return true;
		}
		
		if(exp instanceof BinaryExpression) {
			return canBeAdded(((BinaryExpression) exp).getLeft()) || canBeAdded(((BinaryExpression) exp).getRight());
		}
		
		if(exp instanceof TernaryExpression) {
			return canBeAdded(((TernaryExpression) exp).getLeft()) || canBeAdded(((TernaryExpression) exp).getMiddle())|| canBeAdded(((TernaryExpression) exp).getRight());
		}
		
		return false;
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		
		Set<AvailableExpressions> result = new HashSet<>();
		if(canBeAdded(expression)) {
			AvailableExpressions ae = new AvailableExpressions(expression, id, pp.getLocation());
			result.add(ae);
		}
		return result;
	}
	
	
	private boolean isPresent(Identifier id, String exp) {
		String i = "" + id;
		return exp.contains(i);
	}
	
	/*
	private Collection<Identifier> getOperandIds(SymbolicExpression exp){
        Collection<Identifier> ids = new HashSet<>();
        if(exp.getClass().isInstance(UnaryExpression.class)){
            if(!exp.getDynamicType().isNumericType())
                ids.add((Identifier) exp);
        }
        if(exp.getClass().isInstance(BinaryExpression.class)){
            ids.addAll(getOperandIds(((BinaryExpression) exp).getLeft()));
            ids.addAll(getOperandIds(((BinaryExpression) exp).getRight()));
        }
        if(exp.getClass().isInstance(TernaryExpression.class)){
            ids.addAll(getOperandIds(((TernaryExpression) exp).getLeft()));
            ids.addAll(getOperandIds(((TernaryExpression) exp).getMiddle()));
            ids.addAll(getOperandIds(((TernaryExpression) exp).getRight()));
        }
        return ids;
    }
    */

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
			String exp = ae.expression.toString();
			if(isPresent(id, exp));
				result.add(ae);
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
		return Objects.hash(expression, id, point);
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
		return Objects.equals(expression, other.expression) && Objects.equals(id, other.id)
				&& Objects.equals(point, other.point);
	}
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope), id, point);
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope), id, point);
	}
}

