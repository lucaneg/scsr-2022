package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.analysis.ScopeToken;


import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AvailableExpressions implements DataflowElement<PossibleForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

    private final ValueExpression expression;
    private final Identifier id;
    private final CodeLocation cl;

    public AvailableExpressions(){
        this(null,null,null);
    }

    public AvailableExpressions(ValueExpression expression, Identifier id, CodeLocation cl){
        this.expression = expression;
        this.id = id;
        this.cl = cl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailableExpressions that = (AvailableExpressions) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        result.add(id);
        return result;
    }

    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
                                               PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression, id, pp.getLocation());
        result.add(ae);
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

        for (AvailableExpressions ae : domain.getDataflowElements())
            if (ae.id.equals(id))
                result.add(ae);

        return result;
    }

    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
                                                PossibleForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        return new HashSet<>();
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
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope), id, cl);
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope), id, cl);
	}

}

