package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.operator.StringOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class StringGraphDomain extends BaseNonRelationalValueDomain<StringGraphDomain> {

    private StringGraph stringGraph;

    public StringGraphDomain(StringGraph stringGraph) {
        this.stringGraph = stringGraph;
    }


    @Override
    protected StringGraphDomain evalTernaryExpression(TernaryOperator operator, StringGraphDomain left, StringGraphDomain middle, StringGraphDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringSubstring){
            // return left.stringGraph.substring(middle, right);
        }
        return new StringGraphDomain(StringGraph.buildMAX());
    }

    @Override
    protected StringGraphDomain evalBinaryExpression(BinaryOperator operator, StringGraphDomain left,
                                                         StringGraphDomain right,
                                                         ProgramPoint pp) {

        if (operator instanceof StringConcat) {
            StringGraph.buildCONCAT(left.stringGraph, right.stringGraph).normalize();
        } else if (operator instanceof StringContains) {

        }
        return new StringGraphDomain(StringGraph.buildMAX());
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
