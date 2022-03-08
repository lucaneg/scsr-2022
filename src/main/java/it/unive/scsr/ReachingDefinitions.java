package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.PairRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.OutOfScopeIdentifier;
import it.unive.lisa.symbolic.value.ValueExpression;

public class ReachingDefinitions
		implements DataflowElement<
				PossibleForwardDataflowDomain<ReachingDefinitions>,
				ReachingDefinitions> {

	private final Identifier id;
	private final CodeLocation point;

	public ReachingDefinitions() {
		this(null, null);
	}

	public ReachingDefinitions(Identifier id, CodeLocation point) {
		this.id = id;
		this.point = point;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, point);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReachingDefinitions other = (ReachingDefinitions) obj;
		return Objects.equals(id, other.id) && Objects.equals(point, other.point);
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		result.add(id);
		return result;
	}

	@Override
	public Collection<ReachingDefinitions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain) throws SemanticException {
		Set<ReachingDefinitions> result = new HashSet<>();
		ReachingDefinitions rd = new ReachingDefinitions(id, pp.getLocation());
		result.add(rd);
		return result;
	}

	@Override
	public Collection<ReachingDefinitions> gen(ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain) throws SemanticException {
		return new HashSet<>();
	}

	@Override
	public Collection<ReachingDefinitions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain) throws SemanticException {
		Set<ReachingDefinitions> result = new HashSet<>();

		for (ReachingDefinitions rd : domain.getDataflowElements())
			if (rd.id.equals(id))
				result.add(rd);

		return result;
	}

	@Override
	public Collection<ReachingDefinitions> kill(ValueExpression expression, ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain) throws SemanticException {
		return new HashSet<>();
	}

	// the following code outside of the scope of the course, ignore it!
	
	@Override
	public DomainRepresentation representation() {
		return new PairRepresentation(new StringRepresentation(id), new StringRepresentation(point));
	}

	@Override
	public ReachingDefinitions pushScope(ScopeToken token) throws SemanticException {
		return new ReachingDefinitions((Identifier) id.pushScope(token), point);
	}

	@Override
	public ReachingDefinitions popScope(ScopeToken token) throws SemanticException {
		if (!(id instanceof OutOfScopeIdentifier))
			return null;

		return new ReachingDefinitions((Identifier) id.popScope(token), point);
	}
}
