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
		private final ValueExpression exp;
		
		public AvailableExpressions(ValueExpression exp){
			this.exp = exp;
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
			return Objects.equals(exp, other.exp);

		}

		@Override
		public Collection<Identifier> getInvolvedIdentifiers() {
			return getInvolvedIdentifiers(this.exp);
		}

		public AvailableExpressions(){
			this(null);
		}

		@Override
		public int hashCode(){
			return Objects.hash(exp);
		}

		//let's do some dataflow analysis

		public Collection<Identifier> getInvolvedIdentifiers(ValueExpression exp) {
			Set<Identifier> _myhash = new HashSet<>();

			if (exp instanceof Identifier) {
				_myhash.add((Identifier) exp);
			} 
		
			if (exp.getClass().isInstance(UnaryExpression.class)) {
				UnaryExpression ue = (UnaryExpression) exp;
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) ue.getExpression()));
			} 

			if (exp.getClass().isInstance(BinaryExpression.class)) {
				BinaryExpression be = (BinaryExpression) exp;
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) be.getLeft()));
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) be.getRight()));
			} 

			if (exp.getClass().isInstance(TernaryExpression.class)) {
				TernaryExpression te = (TernaryExpression) exp; 
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getLeft()));
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getRight()));
				_myhash.addAll(getInvolvedIdentifiers((ValueExpression) te.getMiddle()));
			}

			return _myhash;
		}

		@Override
		public Collection<AvailableExpressions> gen(Identifier id, ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException { 
			Set<AvailableExpressions> _myhash = new HashSet<>();
			AvailableExpressions _xexp = new AvailableExpressions(exp);
			if (!_xexp.getInvolvedIdentifiers().contains(id))
				_myhash.add(_xexp);
			return _myhash;
		}

		@Override
		public Collection<AvailableExpressions> gen(ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			return new HashSet<>();
		}

		@Override
		public Collection<AvailableExpressions> kill(Identifier id, ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			Set<AvailableExpressions> _myhash = new HashSet<>();

			for (AvailableExpressions _xexp : domain.getDataflowElements()) {

				if (_xexp.getInvolvedIdentifiers().contains(id))
					_myhash.add(_xexp);
			
			}

			return _myhash;
		}	

		@Override
		public Collection<AvailableExpressions> kill(ValueExpression exp, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
			return new HashSet<>();
		}
		
	


	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment it to get
	// your code to compile. Beware that the code is written expecting that a field named 
	// "exp" of type ValueExpression exists in this class: if you name it differently,
	// change also the code below to make it work by just using the name of your choice instead
	// of "exp". If you don't have a field of type ValueExpression in your solution,
	// then you should make sure that what you are doing is correct :)
	
	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(exp);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken _scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) exp.pushScope(_scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken _scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) exp.popScope(_scope));
	}
}


