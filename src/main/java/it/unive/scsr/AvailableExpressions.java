/*
 * Valentino Dalla Valle 874210
 */

package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.*;

import java.util.*;

public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final ValueExpression expression;


	public AvailableExpressions() {
		this(null);
	}

	public AvailableExpressions(ValueExpression expression){
		this.expression = expression;
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
		return extractIdentifiersFromExpression(expression);
	}

	private Collection<Identifier> extractIdentifiersFromExpression(ValueExpression expression) {
		Collection<Identifier> identifiers = new HashSet<>();
		if(expression == null)
			return identifiers;
		//Expression in the form a;
		//This is the base case of the recursion
		if (expression instanceof Identifier) {
			identifiers.add((Identifier) expression);
		}
		//Compound expressions
		//Expression in the form op a;
		else if (expression instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression) expression;
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) unary.getExpression()));
		}
		//Expression in the form a op b; where a is left expression and b right
		else if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) binary.getLeft()));
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) binary.getRight()));
		}
		//Expression in the form a op b op c; where a is left expression, b is middle and c is right
		else if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) ternary.getLeft()));
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) ternary.getRight()));
			identifiers.addAll(extractIdentifiersFromExpression((ValueExpression) ternary.getMiddle()));
		}
		return identifiers;
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		HashSet<AvailableExpressions> gen_set = new HashSet<>();
		AvailableExpressions current_expressions = new AvailableExpressions(expression);
		//We check if the assignment expression does not contain its operands among the assigned variables. If that's true expression is added to the gen set
		//Moreover, we also check if such expression is a Constant or an Identifier. If that's the case than it is not necessary to add such value to the gen set.
		if(!current_expressions.getInvolvedIdentifiers().contains(id)
			&& !(expression instanceof Constant) && !(expression instanceof Identifier))
			gen_set.add(current_expressions);

		return gen_set;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		HashSet<AvailableExpressions> gen_set = new HashSet<>();
		AvailableExpressions current_expressions = new AvailableExpressions(expression);
		//A non assigning expression can affect AvailableExpressions gen set analysis!
		//We just check if such expression is a Constant or an identifier. If not than we add such expression
		if(!(expression instanceof Constant) && !(expression instanceof Identifier))
			gen_set.add(current_expressions);

		return gen_set;
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		HashSet<AvailableExpressions> kill_set = new HashSet<>();
		//We check each expression of the current domain to see if the current expression redefines one of their operands. If that's true such domain expression is added to the kill set
		for(AvailableExpressions domain_expression : domain.getDataflowElements())
			if(extractIdentifiersFromExpression(domain_expression.expression).contains(id))
				kill_set.add(domain_expression);

		return kill_set;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		//A non assigning expression won't affect AvailableExpressions kill set analysis, so the empty set is returned.
		return Collections.emptyList();
	}


	/*-----------------------------------------------------------------------------------*/


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

