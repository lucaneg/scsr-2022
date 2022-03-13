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
import it.unive.lisa.program.cfg.ProgramPoint;;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressions
        implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

    private final ValueExpression expr;


    private AvailableExpressions(ValueExpression expr) {
        this.expr = expr;
    }

    public AvailableExpressions() {
        this(null);
    }

    @Override
    public String toString() {
        return representation().toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int res = 1;
        res = prime * res + ((expr == null) ? 0 : expr.hashCode());
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
        AvailableExpressions supp = (AvailableExpressions) obj;
        if (expr == null) {
            if (supp.expr != null)
                return false;
        } else if (!expr.equals(supp.expr))
            return false;
        return true;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getIdentifierOperands(expr);
    }

    private static Collection<Identifier> getIdentifierOperands(ValueExpression expr) {
        Collection<Identifier> out = new HashSet<>();
        if (expr == null)
            return out;
        if (expr instanceof Identifier)
            out.add((Identifier) expr);
        if (expr instanceof TernaryExpression) {
            TernaryExpression ternary = (TernaryExpression) expr;
            out.addAll(getIdentifierOperands((ValueExpression) ternary.getLeft()));
            out.addAll(getIdentifierOperands((ValueExpression) ternary.getMiddle()));
            out.addAll(getIdentifierOperands((ValueExpression) ternary.getRight()));
        }
        if (expr instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expr;
            out.addAll(getIdentifierOperands((ValueExpression) binary.getLeft()));
            out.addAll(getIdentifierOperands((ValueExpression) binary.getRight()));
        }
        if (expr instanceof UnaryExpression)
            out.addAll(getIdentifierOperands((ValueExpression) ((UnaryExpression) expr).getExpression()));
        return out;
    }

    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expr, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> out = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expr);
        if (filter(expr))
            out.add(ae);
        return out;
    }

    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expr, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> out = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expr);
        if (!ae.getInvolvedIdentifiers().contains(id) && filter(expr))
            out.add(ae);
        return out;
    }


    private static boolean filter(ValueExpression expr) {
        if (expr instanceof Identifier)
            return false;
        if (expr instanceof Constant)
            return false;
        if (expr instanceof Skip)
            return false;
        if (expr instanceof PushAny)
            return false;
        return true;
    }

    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expr, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        return Collections.emptyList();
    }

    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expr, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) {
        Collection<AvailableExpressions> out = new HashSet<>();
        for (AvailableExpressions ae : domain.getDataflowElements()) {
            Collection<Identifier> ids = getIdentifierOperands(ae.expr);
            if (ids.contains(id))
                out.add(ae);
        }
        return out;
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
        return new StringRepresentation(expr);
    }

    @Override
    public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
        return new AvailableExpressions((ValueExpression) expr.pushScope(scope));
    }

    @Override
    public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
        return new AvailableExpressions((ValueExpression) expr.popScope(scope));
    }
}


