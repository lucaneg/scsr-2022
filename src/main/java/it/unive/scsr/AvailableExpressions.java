package it.unive.scsr;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.ExpressionVisitor;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;

import java.util.*;

public class AvailableExpressions
        implements DataflowElement<
        DefiniteForwardDataflowDomain<AvailableExpressions>,
        AvailableExpressions> {

    private final ValueExpression expression;


    public AvailableExpressions() {
        this(null);
    }

    public AvailableExpressions(ValueExpression e) {
        this.expression=e;
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
        return Objects.equals(expression,other.expression);
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        setInvolvedIdentifier(expression,result);
        return result;
    }

    //add to result the identifiers involved in the different expressions type
    private void setInvolvedIdentifier(ValueExpression expression , Collection<Identifier> involvedId){
        //base case
        if(expression instanceof Identifier)
            involvedId.add((Identifier) expression);
        //recursion
        else if(expression instanceof UnaryExpression){
            setInvolvedIdentifier(((ValueExpression)((UnaryExpression) expression).getExpression()),involvedId );
        }
        else if(expression instanceof BinaryExpression){
            setInvolvedIdentifier((ValueExpression) ((BinaryExpression)expression).getLeft(),involvedId);
            setInvolvedIdentifier((ValueExpression) ((BinaryExpression)expression).getRight(),involvedId);
        }
        else if(expression instanceof TernaryExpression){
            setInvolvedIdentifier((ValueExpression) ((TernaryExpression)expression).getLeft(),involvedId);
            setInvolvedIdentifier((ValueExpression) ((TernaryExpression)expression).getMiddle(),involvedId);
            setInvolvedIdentifier((ValueExpression) ((TernaryExpression)expression).getRight(),involvedId);
        }
    }

    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        //valid and also the exp does not contain the id in which it is assigned
        if(isValid(expression) && !isIdInvolved(id,ae.getInvolvedIdentifiers()))
            result.add(ae);
        return result;

    }

    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        //considering also expressions not assigned (like if(a>b) --> a>b is considered as AE)
        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        if(isValid(expression))
            result.add(ae);
        return result;
    }

    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        for(AvailableExpressions ae: domain.getDataflowElements()){
            if(isIdInvolved(id,ae.getInvolvedIdentifiers())){
                result.add(ae);
            }
        }
        return result;
    }
    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        return new HashSet<>();
    }

    //utility functions

    private boolean isIdInvolved(Identifier id , Collection<Identifier> involvedId){
        for(Identifier i : involvedId){
            if(id.equals(i))
                return true;
        }
        return false;
    }

    //expression is an instance of a valid expression(the types that are considered exp in this code - subclasses of ValueExpression)
    private boolean isValid(ValueExpression expression){
        if((expression instanceof UnaryExpression ||
                expression instanceof BinaryExpression || expression instanceof TernaryExpression))
            return true;
        else
            return false;
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

