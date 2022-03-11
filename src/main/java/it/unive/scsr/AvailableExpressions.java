package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.ExpressionVisitor;
import it.unive.lisa.symbolic.heap.AccessChild;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.symbolic.heap.HeapDereference;
import it.unive.lisa.symbolic.heap.HeapReference;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailableExpressions
		implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final ValueExpression expression;

	public AvailableExpressions() {
		this.expression = null;
	}

	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		if (this.expression != null) {
			try {
				this.expression.accept(new ExpressionVisitor<Collection<Identifier>>() {

					@Override
					public Collection<Identifier> visit(AccessChild expression, Collection<Identifier> receiver,
							Collection<Identifier> child, Object... params) throws SemanticException {

						System.out.println("AccessChild: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(HeapAllocation expression, Object... params)
							throws SemanticException {
						System.out.println("HeapAlloc: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(HeapReference expression, Collection<Identifier> arg,
							Object... params)
							throws SemanticException {
						System.out.println("HeapRef: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(HeapDereference expression, Collection<Identifier> arg,
							Object... params) throws SemanticException {
						System.out.println("HeapDeref: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(UnaryExpression expression, Collection<Identifier> arg,
							Object... params) throws SemanticException {
						System.out.println("Unary: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(BinaryExpression expression, Collection<Identifier> left,
							Collection<Identifier> right, Object... params) throws SemanticException {
						// System.out.println("Binary: " + expression);
						for (Identifier id : left) {
							System.out.println("Left: " + id);
							result.add(id);
						}

						for (Identifier id : right) {
							System.out.println("Right: " + id);
							result.add(id);
						}
						return result;
					}

					@Override
					public Collection<Identifier> visit(TernaryExpression expression, Collection<Identifier> left,
							Collection<Identifier> middle, Collection<Identifier> right, Object... params)
							throws SemanticException {
						System.out.println("Ternary: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(Skip expression, Object... params) throws SemanticException {
						System.out.println("Skip: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(PushAny expression, Object... params) throws SemanticException {
						System.out.println("PushAny: " + expression);
						return result;
					}

					@Override
					public Collection<Identifier> visit(Constant expression, Object... params)
							throws SemanticException {
						return result;
					}

					@Override
					public Collection<Identifier> visit(Identifier expression, Object... params)
							throws SemanticException {
						result.add(expression);
						return result;
					}

				}, result);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		Set<AvailableExpressions> result = new HashSet<>();
		AvailableExpressions ae = new AvailableExpressions(expression);
		result.add(ae);

		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		Set<AvailableExpressions> result = new HashSet<>();

		for (AvailableExpressions ae : domain.getDataflowElements()) {

			ae.getInvolvedIdentifiers();

			if (ae.tracksIdentifiers(id)) {
				result.add(ae);
			}
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
		return new HashSet<>();
	}

	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to
	// get
	// your code to compile. Beware that the code is written expecting that a field
	// named
	// "expression" of type ValueExpression exists in this class: if you name it
	// differently,
	// change also the code below to make it work by just using the name of your
	// choice instead
	// of "expression". If you don't have a field of type ValueExpression in your
	// solution,
	// then you should make sure that what you are doing is correct :)

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		System.out.println("PUSH SCOPE: " + scope.toString());
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		System.out.println("POP SCOPE: " + scope.toString());
		return new AvailableExpressions((ValueExpression) expression.popScope(scope));
	}

}
