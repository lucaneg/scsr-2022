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

public class AvailableExpressions
        implements DataflowElement<
                DefiniteForwardDataflowDomain<AvailableExpressions>,
                AvailableExpressions> {

    private Collection<Identifier> involvedIds; // possible multiple available expression identifiers
    private ValueExpression expression;
    private final CodeLocation point;

    /**
     * Empty constructor
     */
    public AvailableExpressions() {
        this(null, null, null);
    }

    /**
     *
     * @param expression expression that is evaluated
     * @param ids involved expressions ids
     * @param point code location of the evaluated expression
     */
    public AvailableExpressions(ValueExpression expression, Collection<Identifier> ids, CodeLocation point) {
        this.expression = expression;
        this.involvedIds = ids;
        this.point = point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailableExpressions)) return false;
        AvailableExpressions that = (AvailableExpressions) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    /**
     * Analyze the expression and if and only if the expression is not null,
     * choices are taken depending on the type of the expression
     * @param symbolicExpression expression that needs to be evaluated
     * @return collection of all the involved identifiers in the expression
     */
    public Collection<Identifier> analyzingExpression(SymbolicExpression symbolicExpression) {
        // Evaluating symbolic expression which is the smallest element

        Set<Identifier> involvedIdentifiers = new HashSet<>();
        this.involvedIds = involvedIdentifiers;

        if (symbolicExpression != null){
            // Base case of our expression
            if (symbolicExpression instanceof Variable) {
                this.involvedIds.add((Identifier) symbolicExpression);
                involvedIdentifiers.add((Identifier) symbolicExpression);
            }
            else if (symbolicExpression instanceof UnaryExpression)
                involvedIdentifiers.addAll(analyzingExpression(((UnaryExpression)symbolicExpression).getExpression()));
            else if (symbolicExpression instanceof BinaryExpression) {
                involvedIdentifiers.addAll(analyzingExpression(((BinaryExpression) symbolicExpression).getLeft()));
                involvedIdentifiers.addAll(analyzingExpression(((BinaryExpression) symbolicExpression).getRight()));
            }
            else if (symbolicExpression instanceof TernaryExpression) {
                involvedIdentifiers.addAll(analyzingExpression(((TernaryExpression) symbolicExpression).getLeft()));
                involvedIdentifiers.addAll(analyzingExpression(((TernaryExpression) symbolicExpression).getMiddle()));
                involvedIdentifiers.addAll(analyzingExpression(((TernaryExpression) symbolicExpression).getRight()));
            }
        }

        return involvedIdentifiers;
    }

    /**
     * getInvolvedIdentifiers
     * @return all the previously involved identifiers in the available expression
     */
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return this.analyzingExpression(expression);
    }

    /**
     * Gen set defined when identifier id is present
     * @param expression expression that is evaluated
     * @param pp code location of the evaluated expression
     * @param domain the domain that contain all the previous instances of the available expression
     * @return collection representing the expressions evaluated without subsequently
     * redefining its operands
     * @throws SemanticException
     */
    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

        Set<AvailableExpressions> availableExpressionsSet = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression, new HashSet<>(), pp.getLocation());

        // If id of variable is already contained we make the intersection
        if (!ae.involvedIds.contains(id)) {
            if (expression instanceof UnaryExpression ||
                    expression instanceof BinaryExpression ||
                    expression instanceof TernaryExpression) {

                // We can add the value expression in the program available expression set list
                availableExpressionsSet.add(ae);
            }
        }
        return availableExpressionsSet;
    }

    /**
     * Gen set defined when no identifier id is present
     * @param expression expression that is evaluated
     * @param pp code location of the evaluated expression
     * @param domain the domain that contain all the previous instances of the available expression
     * @return collection representing the expressions evaluated without subsequently
     * redefining its operands
     * @throws SemanticException
     */
    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
       return this.gen(null, expression, pp, domain);
    }

    /**
     * Kill set defined when identifier id is present
     * @param expression expression that is evaluated
     * @param pp code location of the evaluated expression
     * @param domain the domain that contain all the previous instances of the available expression
     * @return collection representing the expressions whose operands are redefined in B
     * without reevaluating the expression afterwards
     * @throws SemanticException
     */
    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

        Set<AvailableExpressions> removeAvailableExpressionsSet = new HashSet<>();

        for (AvailableExpressions ae: domain.getDataflowElements())
            // If available expression id is contained in the previous flow we can remove it
            if(ae.getInvolvedIdentifiers().contains(id))
                removeAvailableExpressionsSet.add(ae);

        return removeAvailableExpressionsSet;
    }

    /**
     * Kill set defined when no identifier id is present
     * @param expression expression that is evaluated
     * @param pp code location of the evaluated expression
     * @param domain the domain that contain all the previous instances of the available expression
     * @return collection representing the set of all killed expressions
     * @throws SemanticException
     */
    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        // If we don't assign the expression we are just evaluating and
        // not killing any expression previously defined
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
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope), involvedIds, point);
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope), involvedIds, point);
	}
}
