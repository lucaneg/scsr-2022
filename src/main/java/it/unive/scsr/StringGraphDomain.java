package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StringGraphDomain extends BaseNonRelationalValueDomain<StringGraphDomain> {

    private StringGraph stringGraph;

    public StringGraphDomain(StringGraph stringGraph) {
        this.stringGraph = stringGraph;
    }


    @Override
    protected StringGraphDomain evalTernaryExpression(TernaryOperator operator, StringGraphDomain left, StringGraphDomain middle, StringGraphDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringSubstring){
            return new StringGraphDomain(left.stringGraph.substring(middle.stringGraph.getBound(), right.stringGraph.getBound()));
        }
        return new StringGraphDomain(StringGraph.buildMAX());
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, StringGraphDomain left, StringGraphDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringContains) {
            return left.stringGraph.contains(right.stringGraph.getCharacter());
        }
        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected StringGraphDomain evalBinaryExpression(BinaryOperator operator, StringGraphDomain left,
                                                         StringGraphDomain right,
                                                         ProgramPoint pp) {

        if (operator instanceof StringConcat) {
            StringGraph stringGraph = StringGraph.buildCONCAT(left.stringGraph, right.stringGraph);
            stringGraph.compact();
            stringGraph.normalize();
            return new StringGraphDomain(stringGraph);
        }
        return new StringGraphDomain(StringGraph.buildMAX());
    }

    @Override
    protected StringGraphDomain lubAux(StringGraphDomain other) {
        StringGraph lubGraph = new StringGraph(StringGraph.NodeType.OR, new ArrayList<>(List.of(this.stringGraph, other.stringGraph)), null);
        lubGraph.compact();
        lubGraph.normalize();
        return new StringGraphDomain(lubGraph);
    }

    @Override
    protected StringGraphDomain wideningAux(StringGraphDomain other) {
        return null;
    }

    @Override
    protected boolean lessOrEqualAux(StringGraphDomain other) {
        List<Pair<StringGraph, StringGraph>> edges = new ArrayList<>();
        return checkPartialOrder(this.stringGraph, other.stringGraph, edges);
    }

    private boolean checkPartialOrder (StringGraph first, StringGraph second, List<Pair<StringGraph, StringGraph>> edges) {
        Pair<StringGraph, StringGraph> currentEdge = new Pair<>(first, second);
        if (edges.contains(currentEdge)) return true;
        else if (second.getLabel() == StringGraph.NodeType.MAX) return true;
        else if (first.getLabel() == StringGraph.NodeType.CONCAT &&
                second.getLabel() == StringGraph.NodeType.CONCAT &&
                first.getSons().size() == second.getSons().size() &&
                first.getSons().size() > 0) {
            boolean result = false;
            edges.add(new Pair<>(first, second));
            for(int i = 0; i < first.getSons().size(); i++){
                result = result || checkPartialOrder(first.getSons().get(i), second.getSons().get(i), edges);
            }
            return result;
        }
        else if (first.getLabel() == StringGraph.NodeType.OR &&
                second.getLabel() == StringGraph.NodeType.OR){
            boolean result = false;
            edges.add(new Pair<>(first, second));
            for(StringGraph son : first.getSons()){
                result = result ||checkPartialOrder(son, second, edges);
            }
            return result;
        }
        else if (second.getLabel() == StringGraph.NodeType.OR && !Objects.isNull(checkLabelEquality(getPrincipalNodes(second), first))) {
            edges.add(new Pair<>(first, second));
            return checkPartialOrder(first, checkLabelEquality(getPrincipalNodes(second), first), edges);
        } else {
            return first.getLabel().equals(second.getLabel());
        }
    }

    private List<StringGraph> getPrincipalNodes(StringGraph stringGraph) {
        return new ArrayList<>(); // TODO: da fare
    }

    private StringGraph checkLabelEquality(List<StringGraph> stringGraphList, StringGraph stringGraph) {
        boolean result;
        for(StringGraph s : stringGraphList) {
            result = s.getLabel().equals(stringGraph.getLabel());
            if (result) {
                return s;
            }
        }
        return null;
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
        return new StringRepresentation(stringGraph);
    }

    @Override
    public StringGraphDomain top() {
        return new StringGraphDomain(StringGraph.buildMAX());
    }

    @Override
    public StringGraphDomain bottom() {
        return new StringGraphDomain(StringGraph.buildEMPTY());
    }
}
