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

/* Notes
 * Available Expression is a Definite Data flow domain
 * Constants and Identifiers, are not Available Expressions
 */

public class AvailableExpressions  implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>,AvailableExpressions > {

    private final ValueExpression expression;

    public AvailableExpressions() {
        this(null);
    }

    public AvailableExpressions(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getInvolvedIdentifiers(this.expression);
    }

    // Recursively extract Identifiers from expressions
    // Base case: Single Identifier
    private Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
        Set<Identifier> result = new HashSet<>();
        if (expression != null) {
            // below casts are safe (check made during runtime which assures the type)
            if (expression instanceof Identifier) { // Id
                Identifier id = (Identifier) expression;
                result.add(id);
            }
            else if (expression instanceof UnaryExpression) { // Expression1++
                UnaryExpression unary = (UnaryExpression) expression;
                getInvolvedIdentifiers((ValueExpression) unary.getExpression()).forEach((id) -> { // Expression1
                    result.add(id);
                });
            }
            else if (expression instanceof BinaryExpression) { // i.e. Expression1 + Expression2
                BinaryExpression binary = (BinaryExpression) expression;
                getInvolvedIdentifiers((ValueExpression) binary.getLeft()).forEach((id) -> { // Expression1
                    result.add(id);
                });
                getInvolvedIdentifiers((ValueExpression) binary.getRight()).forEach((id) -> { // Expression2
                    result.add(id);
                });
            }
            else if (expression instanceof TernaryExpression) { // i.e. Expression1 ? Expression2 : Expression3
                TernaryExpression ternary = (TernaryExpression) expression;
                getInvolvedIdentifiers((ValueExpression) ternary.getLeft()).forEach((id) -> { // Expression1
                    result.add(id);
                });
                getInvolvedIdentifiers((ValueExpression) ternary.getMiddle()).forEach((id) -> { // Expression2
                    result.add(id);
                });
                getInvolvedIdentifiers((ValueExpression) ternary.getRight()).forEach((id) -> { // Expression3
                    result.add(id);
                });
            }
        }
        return result;
    }

    // gen set for -> ID = EXP
    @Override
    public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        // check if any potential identifier of an expression is being redefined
        // i.e. x = x + y
        boolean present = ae.getInvolvedIdentifiers().contains(id);
        // if this doesn't happen, it's safe to add to the gen set
        if (!present &&
            !isIdentifier(ae.expression) &&
            !isConstant(ae.expression) &&
            !doesNothing(ae.expression))
                result.add(ae);
        return result;
    }

    // gen set for -> EXP
    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae = new AvailableExpressions(expression);
        // i.e. handle expression cases:
        //      y (y is not an AE)
        //      5 (5 is not an AE)
        if (!isIdentifier(ae.expression) &&
            !isConstant(ae.expression) &&
            !doesNothing(ae.expression))
                result.add(ae);
        return result;
    }

    // kill set for -> ID = EXP
    @Override
    public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        // checks if any identifier (present in all the AE found till now) is being redefined
        // i.e. x = x + 4 (all expressions which use identifier x from the AE found till now, should be killed)
        domain.getDataflowElements().forEach((ae) -> {
           if(ae.getInvolvedIdentifiers().contains(id))
               result.add(ae);
        });
        return result;
    }

    // kill set for -> EXP
    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        Set<AvailableExpressions> result = new HashSet<>();
        return result;
    }

    // wrapper for identifier check
    private static boolean isIdentifier (ValueExpression exp) {
        return (exp instanceof Identifier);
    }

    // wrapper for constant check
    private static boolean isConstant (ValueExpression exp) {
        return (exp instanceof Constant);
    }

    // wrapper for an expression that does nothing
    private static boolean doesNothing(ValueExpression exp) { return (exp instanceof Skip); }

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
}

