package it.unive.scsr;

public class AvailableExpressions {


	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to get
	// your code to compile. Beware that the code is written expecting that a field named 
	// "expression" of type ValueExpression exists in this class: if you name it differently,
	// change also the code below to make it work by just using the name of your choice instead
	// of "expression". If you don't have a field of type ValueExpression in your solution,
	// then you should make sure that what you are doing is correct :)
	private ValueExpression construction;
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(construction);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) construction.pushScope(scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) construction.popScope(scope));
	}
}

