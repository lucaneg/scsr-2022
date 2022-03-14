package it.unive.scsr;
import java.util.*;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;

public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

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
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getIdentifiers(expression);
    }

    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions temp = new AvailableExpressions(expression);
        boolean value = !(expression instanceof Constant || expression instanceof Identifier || expression instanceof Skip || expression instanceof PushAny);
        if (!temp.getInvolvedIdentifiers().contains(id) && value)
            result.add(temp);
        return result;
    }

    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> result = new HashSet<>();
        if ( !(expression instanceof Constant || expression instanceof Identifier || expression instanceof Skip || expression instanceof PushAny) )
            result.add(new AvailableExpressions(expression));
        return result;
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
        if (expression == null) {
            return other.expression == null;
        } else return expression.equals(other.expression);
    }

    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> result = new HashSet<>();
        for(AvailableExpressions temp : domain.getDataflowElements()){
            Collection<Identifier> tempId =  getIdentifiers(temp.expression);
            if (tempId.contains(id))
                result.add(temp);
        }
        return result;
    }

    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        return Collections.emptyList();
    }

    private Collection<Identifier> getIdentifiers(ValueExpression expression){
        Collection<Identifier> result = new HashSet<>();

        if (expression == null){
            return result;
        }
        else if (expression instanceof Identifier){
            Identifier value = (Identifier) expression;
            result.add(value);
        }
        else if (expression instanceof UnaryExpression){
            Collection<Identifier> value = getIdentifiers((ValueExpression) ((UnaryExpression) expression).getExpression());
            result.addAll(value);
        }
        else if (expression instanceof BinaryExpression){
            Collection<Identifier> valueL = getIdentifiers((ValueExpression) ((BinaryExpression) expression).getLeft());
            result.addAll(valueL);
            Collection<Identifier> valueR = getIdentifiers((ValueExpression) ((BinaryExpression) expression).getRight());
            result.addAll(valueR);
        }
        else if (expression instanceof TernaryExpression){
            Collection<Identifier> valueL = getIdentifiers((ValueExpression) ((TernaryExpression) expression).getLeft());
            result.addAll(valueL);
            Collection<Identifier> valueM = getIdentifiers((ValueExpression) ((TernaryExpression) expression).getMiddle());
            result.addAll(valueM);
            Collection<Identifier> valueR = getIdentifiers((ValueExpression) ((TernaryExpression) expression).getRight());
            result.addAll(valueR);
        }
        return result;
    }


	// IMPLEMENTATION NOTE:
	// the code below is outside the scope of the course. You can uncomment it to get
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
        assert expression != null;
        return new AvailableExpressions((ValueExpression) expression.pushScope(scope));
    }

    @Override
    public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
        assert expression != null;
        return new AvailableExpressions((ValueExpression) expression.popScope(scope));
    }
}
