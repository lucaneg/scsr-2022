package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import org.antlr.v4.runtime.misc.Pair;

import static it.unive.scsr.StringGraph.checkPartialOrder;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * StringGraphDomain: abstract domain used to represent strings
 */
public class StringGraphDomain extends BaseNonRelationalValueDomain<StringGraphDomain> {

    private final StringGraph stringGraph;

    public StringGraphDomain(StringGraph stringGraph) {
        this.stringGraph = stringGraph;
    }

    public StringGraphDomain() {
        this(null);
    }

    @Override
    protected StringGraphDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof String) {
            String constantWithoutApex = ((String)constant.getValue()).replace("\"","");
            return new StringGraphDomain(new StringGraph(constantWithoutApex));
        }
        return super.evalNonNullConstant(constant, pp);
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

        // other --> gn
        // this --> go
        if (StringGraph.checkPartialOrder(this.stringGraph, other.stringGraph, new ArrayList<>())) {
            return this;
        } else {
            return this.widen(this.lubAux(other));
        }
    }

    public StringGraphDomain widen(StringGraphDomain other) {
        // other --> gn
        // this --> go
        StringGraph gResCI = StringGraph.cycleInductionRule(this.stringGraph, other.stringGraph);
        StringGraph gResCR = StringGraph.replacementRule(this.stringGraph, other.stringGraph);

        if (!Objects.isNull(gResCI)) return this.widen(this.lubAux(new StringGraphDomain(gResCI)));
        else if (!Objects.isNull(gResCR)) return this.widen(this.lubAux(new StringGraphDomain(gResCR)));
        else return other;
    }

    @Override
    protected boolean lessOrEqualAux(StringGraphDomain other) {
        List<Pair<StringGraph, StringGraph>> edges = new ArrayList<>();
        return checkPartialOrder(this.stringGraph, other.stringGraph, edges);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof StringGraphDomain) return this.stringGraph.equals(((StringGraphDomain) obj).stringGraph);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringGraph);
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

    @Override
    public boolean isTop() {
        if (!Objects.isNull(this.stringGraph))
            return this.stringGraph.getLabel() == StringGraph.NodeType.MAX;
        return true;
    }

    @Override
    public boolean isBottom() {
        if (!Objects.isNull(this.stringGraph))
            return this.stringGraph.getLabel() == StringGraph.NodeType.EMPTY;
        return true;
    }
}
