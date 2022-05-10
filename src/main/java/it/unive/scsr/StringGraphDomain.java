package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;

import java.util.ArrayList;
import java.util.List;

public class StringGraphDomain extends BaseNonRelationalValueDomain<StringGraphDomain> {

    private StringGraph stringGraph;

    public StringGraphDomain(StringGraph stringGraph) {
        this.stringGraph = stringGraph;
    }

    @Override
    protected StringGraphDomain lubAux(StringGraphDomain other) {
        StringGraph lubGraph = new StringGraph(StringGraph.NodeType.OR, new ArrayList<>(List.of(this.stringGraph, other.stringGraph)));
        lubGraph.normalize();
        return new StringGraphDomain(lubGraph);
    }

    @Override
    protected StringGraphDomain wideningAux(StringGraphDomain other) throws SemanticException {
        return null;
    }

    @Override
    protected boolean lessOrEqualAux(StringGraphDomain other) throws SemanticException {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this.stringGraph.equals(obj);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }

    @Override
    public StringGraphDomain top() {
        return null;
    }

    @Override
    public StringGraphDomain bottom() {
        return null;
    }
}
