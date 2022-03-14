package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;


public class AvailableExpressions implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions>{
		private final ValueExpression expression;
		
		public AvailableExpressions(ValueExpression expression){
			this.expression=expression;
		}

		@Override
		public boolean equals(Object obj){
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass()!=obj.getClass())
				return false;
			
			AvailableExpressions other = (AvailableExpressions) obj;
			return Objects.equals(expression, other.expression);

		}

		@Override
		public Collection<Identifier> getInvolvedIdentifiers() {
			return getInvolvedIdentifiers(this.expression);
		}

		public AvailableExpressions(){
			this(null);
		}

		@Override
		public int hashCode(){
			return Objects.hash(expression);
		}

		//let's do some dataflow analysis

		public Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
			Set<Identifier> myhash = new HashSet<>();

			if (expression instanceof Identifier) {
				myhash.add((Identifier) expression);
			} 
		
			if (expression.getClass().isInstance(UnaryExpression.class)) {
				UnaryExpression ue = (UnaryExpression) expression;
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) ue.getExpression()));
			} 

			if (expression.getClass().isInstance(BinaryExpression.class)) {
				BinaryExpression be = (BinaryExpression) expression;
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) be.getLeft()));
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) be.getRight()));
			} 

			if (expression.getClass().isInstance(TernaryExpression.class)) {
				TernaryExpression te = (TernaryExpression) expression;
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getLeft()));
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getRight()));
				myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getMiddle()));
			}

			return myhash;
		}

		@Override
		public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			Set<AvailableExpressions> myhash = new HashSet<>();
			AvailableExpressions xexpression = new AvailableExpressions(expression);
			if (!xexpression.getInvolvedIdentifiers().contains(id))
				myhash.add(xexpression);
			return myhash;
		}

		@Override
		public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			return new HashSet<>();
		}

		@Override
		public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			Set<AvailableExpressions> myhash = new HashSet<>();

			for (AvailableExpressions xexpression : domain.getDataflowElements()) {

				if (xexpression.getInvolvedIdentifiers().contains(id))
					myhash.add(xexpression);
			
			}

			return myhash;
		}	

		@Override
		public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
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
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope));
	}
}


