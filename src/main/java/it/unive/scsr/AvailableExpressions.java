package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

    private final ValueExpression expression;

    public AvailableExpressions() {
        this(null);
    }

    public AvailableExpressions(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o == null) return false;
        else if (this.getClass() != o.getClass()) return false;
        else return Objects.equals(this.expression, ((AvailableExpressions) o).expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.expression);
    }

    @Override
    public String toString() {
        return this.representation().toString();
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return this.getInvolvedIdentifiers(this.expression);
    }

    public Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
        Collection<Identifier> res = new HashSet<Identifier>();
        /*if expression is valid*/
        if (expression != null) {
            /*case single identifier*/
            if (expression instanceof Identifier) {
                res.add((Identifier) expression);
            }
            /*case unary expression*/
            else if (expression instanceof UnaryExpression) {
                Collection<Identifier> ci = getInvolvedIdentifiers((ValueExpression) ((UnaryExpression) expression).getExpression());
                res.addAll(ci);
            }
            /*case binary expression*/
            else if (expression instanceof BinaryExpression) {
                Collection<Identifier> cl = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getLeft());
                res.addAll(cl);
                Collection<Identifier> cr = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getRight());
                res.addAll(cr);
            }
            /*case ternary expression*/
            else if (expression instanceof TernaryExpression) {
                Collection<Identifier> cl = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getLeft());
                res.addAll(cl);
                Collection<Identifier> cm = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getMiddle());
                res.addAll(cm);
                Collection<Identifier> cr = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getRight());
                res.addAll(cr);
            }
        }
        /*return result*/
        return res;
    }

    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Collection<AvailableExpressions> res = new HashSet<AvailableExpressions>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        Collection<Identifier> ci = ae.getInvolvedIdentifiers();
        /*if it is not present the id*/
        if (!ci.contains(id)) {
            /*check types of expressions that we not include in generation result*/
            if (!(expression instanceof Constant)) {
                if (!(expression instanceof Identifier)) {
                    if (!(expression instanceof Skip)) {
                        /*now i can add the result in the set of generation result*/
                        res.add(ae);
                    }
                }
            }
        }
        /*return result*/
        return res;
    }

    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Collection<AvailableExpressions> res = new HashSet<AvailableExpressions>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        /*check types of expressions that we not include in generation result*/
        if (!(expression instanceof Constant)) {
            if (!(expression instanceof Identifier)) {
                if (!(expression instanceof Skip)) {
                    /*now i can add the result in the set of generation result*/
                    res.add(ae);
                }
            }
        }
        /*return result*/
        return res;
    }

    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Collection<AvailableExpressions> res = new HashSet<AvailableExpressions>();
        Set<AvailableExpressions> df = domain.getDataflowElements();
        for (AvailableExpressions ae : df) {
            Collection<Identifier> ci = ae.getInvolvedIdentifiers();
            if (ci.contains(id)) {
                res.add(ae);
            }
        }
        return res;
    }

    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        /*return an empty collection*/
        return new HashSet<>();
    }

    /*IMPLEMENTATION NOTE:
	 the code below is outside of the scope of the course. You can uncomment it to get
	 your code to compile. Beware that the code is written expecting that a field named
	 "expression" of type ValueExpression exists in this class: if you name it differently,
	 change also the code below to make it work by just using the name of your choice instead
	 of "expression". If you don't have a field of type ValueExpression in your solution,
	 then you should make sure that what you are doing is correct :)*/

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

