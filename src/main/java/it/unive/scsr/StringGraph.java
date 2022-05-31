package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.scsr.Exceptions.InvalidCharacterException;
import it.unive.scsr.Exceptions.WrongBuildStringGraphException;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.*;

public class StringGraph {

    enum NodeType {
        CONCAT,
        OR,
        EMPTY, // represent a simple node
        MAX, // represent a simple node
        SIMPLE
    }

    enum CHARACTER {
        a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, Ɛ
    }

    private NodeType label;
    private List<StringGraph> fathers;
    private List<StringGraph> sons;
    private boolean normalized = false;
    private CHARACTER character;
    private Integer bound; // NEEDED FOR evalTernaryExpression() METHOD

    public StringGraph(NodeType label, List<StringGraph> sons, CHARACTER character) {
        this.label = label;
        this.sons = sons;
        this.character = character;

        for (StringGraph son : sons) {
            son.getFathers().add(this);
            this.getSons().add(son);
        }

        // Checking CONCAT and OR node have at least one node
        if ((label == NodeType.CONCAT && !(this.getSons().size() == 0)) ||
            (label == NodeType.OR && !(this.getSons().size() == 0)))
            throw new WrongBuildStringGraphException("OR node and CONCAT node must have at least one son.");

        // We make sure root has no sons
        if (this.label == NodeType.MAX && this.sons.size() > 0)
            throw new WrongBuildStringGraphException("MAX node cannot have son.");

        if (this.label == NodeType.SIMPLE && Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("SIMPLE node must have a character associated.");
        }

        if (this.label != NodeType.SIMPLE && !Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("Only SIMPLE can have a character associated.");
        }

    }

    private static CHARACTER map(char c) {
        return switch (c) {
            case 'a' -> CHARACTER.a;
            case 'b' -> CHARACTER.b;
            case 'c' -> CHARACTER.c;
            case 'd' -> CHARACTER.d;
            case 'e' -> CHARACTER.e;
            case 'f' -> CHARACTER.f;
            case 'g' -> CHARACTER.g;
            case 'h' -> CHARACTER.h;
            case 'i' -> CHARACTER.i;
            case 'j' -> CHARACTER.j;
            case 'k' -> CHARACTER.k;
            case 'l' -> CHARACTER.l;
            case 'm' -> CHARACTER.m;
            case 'n' -> CHARACTER.n;
            case 'o' -> CHARACTER.o;
            case 'p' -> CHARACTER.p;
            case 'q' -> CHARACTER.q;
            case 'r' -> CHARACTER.r;
            case 's' -> CHARACTER.s;
            case 't' -> CHARACTER.t;
            case 'u' -> CHARACTER.u;
            case 'v' -> CHARACTER.v;
            case 'w' -> CHARACTER.w;
            case 'x' -> CHARACTER.x;
            case 'y' -> CHARACTER.y;
            case 'z' -> CHARACTER.z;
            case 'Ɛ' -> CHARACTER.Ɛ;
            default -> throw new InvalidCharacterException(c);
        };
    }

    public StringGraph(String stringToRepresent) {
        this.label = NodeType.CONCAT;
        this.fathers = new ArrayList<>();
        this.sons = new ArrayList<>();
        for (int i = 0; i < stringToRepresent.length(); i++){
            StringGraph son = new StringGraph(NodeType.SIMPLE, new ArrayList<>(), StringGraph.map(stringToRepresent.charAt(i)));
            this.addSon(son);
        }
        this.normalized = true;
    }


    public StringGraph(NodeType label) {
        assert(!(label == NodeType.CONCAT));
        this.label = label;
        this.sons = new ArrayList<>();
        this.fathers = new ArrayList<>();
    }

    public StringGraph(int bound) {
        this.bound = bound;
    }

    public void addSon(StringGraph son){
        this.getSons().add(son);
        son.getFathers().add(this);
    }

    public void removeSon(StringGraph son){
        this.getSons().remove(son);
        son.getFathers().remove(this);
    }

    public void addAllSons(List<StringGraph> sons){
        for(StringGraph s : sons) {
            this.addSon(s);
        }
    }

    public void removeAllSons(){
        for (StringGraph s : this.sons) {
            this.removeSon(s);
        }
    }

    public NodeType getLabel() {
        return label;
    }

    public void setLabel(NodeType label) {
        this.label = label;
    }

    public List<StringGraph> getSons() {
        return sons;
    }

    /*
    public void setSons(List<StringGraph> sons) {
        this.sons = sons;
    }*/
    public List<StringGraph> getFathers() {
        return fathers;
    }

    /*
    public void setFathers(List<StringGraph> fathers) {
        this.fathers = fathers;
    }*/
    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public CHARACTER getCharacter() {
        return character;
    }

    public void setCharacter(CHARACTER character) {
        this.character = character;
    }

    public Integer getBound() {
        return bound;
    }

    public void setBound(Integer bound) {
        this.bound = bound;
    }

    protected void compact() {
        for (StringGraph s : this.getSons()){
            if (!(this.getFathers().contains(s))) {
                s.compact();
            }
        }

        // RULE 1
        if (this.getLabel() == NodeType.CONCAT && !nonEmptyDenotation()) {
            this.setLabel(NodeType.EMPTY);
            this.removeAllSons();
        }

        // RULE 2
        if (this.getLabel() == NodeType.OR){
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == NodeType.EMPTY) {
                    this.removeSon(son);
                }
            }
        }

        // RULE 3
        if (this.getLabel() == NodeType.OR && this.getSons().contains(this)){
            this.removeSon(this);
        }

        // RULE 4
        if (this.getLabel() == NodeType.OR && this.getSons().size() == 0){
            this.setLabel(NodeType.EMPTY);
            this.removeAllSons();
        }

        // RULE 5
        if (this.getLabel() == NodeType.OR){
            boolean hasMaxSon = false;
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == NodeType.MAX) {
                    hasMaxSon = true;
                    break;
                }
            }
            if (hasMaxSon){
                this.setLabel(NodeType.MAX);
                this.removeAllSons();
            }
        }

        // RULE 6
        if (this.getLabel() == NodeType.OR){
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == NodeType.OR && son.getFathers().size() == 1) {
                    this.removeSon(son);
                    this.addAllSons(son.getSons());
                    son.removeAllSons();
                }
            }
        }

        // RULE 7
        if (this.getLabel() == NodeType.OR && this.getSons().size() == 1){
            StringGraph son = this.getSons().get(0);
            this.setLabel(son.getLabel());
            this.removeAllSons();
            this.addAllSons(son.getSons());
            son.removeAllSons();
            if (son.getFathers().size() > 0){
                for (StringGraph father : son.getFathers()){
                    father.addSon(this);
                    father.removeSon(son);
                }
            }
        }

        // RULE 8
        if (this.getLabel() == NodeType.OR){
            for(StringGraph son: this.getSons()){
                if (son.getLabel() == NodeType.OR && son.getFathers().size() > 1) {
                    for(StringGraph father : son.getFathers()){
                        if(!father.equals(this)) {
                            father.removeSon(son);
                            father.addSon(this);
                        }
                    }
                }
            }
        }
    }

    protected void normalize() {
        for (StringGraph s : this.getSons()){
            if (!(this.getFathers().contains(s)))
                s.normalize();
        }

        // RULE 1
        if (this.getLabel() == NodeType.CONCAT && this.getSons().size() == 1) {
            StringGraph s = this.getSons().get(0);
            this.setLabel(s.getLabel());
            this.removeAllSons();
            this.addAllSons(s.getSons());
            for (StringGraph father : s.getFathers()){
                father.addSon(this);
                father.removeSon(s);
            }
        }

        // RULE 2
        if (this.getLabel() == NodeType.CONCAT) {
            boolean allMaxSons = true;
            for(StringGraph son: this.getSons()){
                if(son.getLabel() != NodeType.MAX)
                    allMaxSons = false;
            }
            if(allMaxSons){
                this.setLabel(NodeType.MAX);
                this.removeAllSons();
            }
        }

        // RULE 3
        if (this.getLabel() == NodeType.CONCAT) {
            if (this.getSons().size() >= 2) {
                AtomicReference<StringGraph> previousSibling;
                AtomicReference<StringGraph> currentSibling;

                int pos = 0;
                //boolean appliedRule = false;
                do {
                    previousSibling = new AtomicReference<>(this.getSons().get(pos));
                    currentSibling = new AtomicReference<>(this.getSons().get(++pos));

                    if (previousSibling.get().getLabel() == NodeType.CONCAT && currentSibling.get().getLabel() == NodeType.CONCAT &&
                            (previousSibling.get().getFathers().size() == 1) && (currentSibling.get().getFathers().size() == 1)) {

                        previousSibling.get().addAllSons(currentSibling.get().getSons()); // Adding currentSibling sons to previous sibling sons
                        currentSibling.get().removeAllSons();

                        for(StringGraph father : currentSibling.get().getFathers()){
                            father.addSon(previousSibling.get());
                            father.removeSon(currentSibling.get());
                        }
                        //appliedRule = true;
                    }
                    ++pos;
                } while(pos < this.getSons().size() /*&& !appliedRule*/);

                /* fatto con il for dentro all'if precedente
                if (appliedRule){
                    this.removeSon(currentSibling.get());
                }*/
            }
        }

        // RULE 4
        if (this.label == NodeType.CONCAT){
            for(StringGraph son : this.getSons()){
                if(son.getLabel() == NodeType.CONCAT && son.getFathers().size() == 1){
                    this.addAllSons(son.getSons());
                    this.removeSon(son); // don't need for bc son has only one father
                    son.removeAllSons(); // need to remove the sons' fathers lists
                }
            }
        }

        // At the end we know for sure that our StringGraph is normalized
        this.setNormalized(true);
    }

    private boolean containsCharWithoutOR(CHARACTER c) {
        boolean result = false;
        if (this.label == NodeType.SIMPLE) return this.character == c;
        else if (this.label == NodeType.MAX) return true;
        else if (this.label == NodeType.EMPTY) return false;
        else if (this.label == NodeType.OR) return false;
        else if (this.label == NodeType.CONCAT) {

            for (StringGraph s : this.getSons()) {
                result = result || s.containsCharWithoutOR(c);
            }

        }
        return result;
    }

    private boolean containsCharOrMax(CHARACTER c) {
        boolean result = false;
        if (this.label == NodeType.SIMPLE) return this.character == c;
        else if (this.label == NodeType.MAX) return true;
        else {
            for (StringGraph s : this.getSons()) {
                result = result || s.containsCharOrMax(c);
            }
        }
        return result;
    }

    public SemanticDomain.Satisfiability contains(CHARACTER c) {
        if (!containsCharOrMax(c)) return NOT_SATISFIED;
        else if (containsCharWithoutOR(c)) return SATISFIED;
        else return UNKNOWN;
    }

    private boolean nonEmptyDenotation() {
        boolean result;
        if(this.label == NodeType.CONCAT && this.getSons().size() > 0) {
            result = true;
            for (StringGraph son : this.getSons()) {
                result = result && son.nonEmptyDenotation();
            }
            return result;
        } else if(this.label == NodeType.OR) {
            result = false;
            for (StringGraph son : this.getSons()) {
                result = result || son.nonEmptyDenotation();
            }
            return result;
        }
        return this.label == NodeType.SIMPLE || (this.label == NodeType.CONCAT && this.getSons().size() == 0);
    }

    @Override
    public boolean equals(Object o){
        assert o.getClass() != StringGraph.class;
        StringGraph other = (StringGraph)o;
        if (this.getSons().equals(other.getSons()) && this.getFathers().equals(other.getFathers())) {
            int i = 0;
            for (StringGraph nodeToCompare : this.getSons()) {
                if (!nodeToCompare.equals(other.getSons().get(i)))
                    return false;
                ++i;
            }

            return true;
                /*
                if (!(nodeToCompare.equals(other.getSons().get(i)) &&
                        nodeToCompare.equals(other.getFathers().get(i))))
                    return false;
                else
                    if(!nodeToCompare.equals(other.getSons().get(i)))
                        return false;


                    for (StringGraph otherGraphToCompare : other.getSons())
                        nodeToCompare.equals(otherGraphToCompare);*/

        }
        else return false;
    }

    public StringGraph substring(int leftBound, int rightBound) {

        if (this.getLabel() == NodeType.CONCAT &&
            this.getSons().size() >= rightBound - 1) {

            for(int i = 0; i < rightBound - 1; ++i) {
                if (this.getSons().get(i).getLabel() != NodeType.SIMPLE)
                    return StringGraph.buildMAX();
            }

            List<StringGraph> substringSons = new ArrayList<>();
            for(int i = leftBound; i < rightBound - 1; ++i) {
                substringSons.add(this.getSons().get(i));
            }
            return new StringGraph(NodeType.CONCAT, substringSons, null);
        }
        return StringGraph.buildMAX();
    }

    public static StringGraph buildCONCAT(StringGraph left, StringGraph right) {
        return new StringGraph(NodeType.CONCAT, new ArrayList<>(List.of(left, right)), null);
    }

    public static StringGraph buildMAX() {
        return new StringGraph(NodeType.MAX); // Represent TOP in the domain
    }

    public static StringGraph buildEMPTY() {
        return new StringGraph(NodeType.EMPTY); //
    }

//    private int checkIndegree(StringGraph fixedNode) {
//        if (stringGraph.getSons().size() == 0) {
//            return 0;
//        } else if ((!Objects.isNull(stringGraph.father)) && (stringGraph.getSons().size() == 0)){
//            return 1;
//        } else if (stringGraph.father) {
//
//        }
//        else {
//            int indegreeNumber = 0;
//            for(StringGraph s: stringGraph.getSons())
//                indegreeNumber += checkIndegree(s);
//
//            return Objects.isNull(father) ? indegreeNumber: ++indegreeNumber;
//        }


//        int indegree = 0;
//        for(StringGraph s: fixedNode.getSons()){
//            if (!s.equals(fixedNode.father)) // non tiene in considerazione gli indegree verso il basso
//                indegree += checkIndegreeAUX(fixedNode, s);
//            else if (fixedNode.father.equals(s)) {
//
//            }
//        }
//        // checks if I have a higher node that points to me
//        if ((!Objects.isNull(fixedNode.father)))
//            ++indegree;
//
//        return indegree;
//
//    }
//
//    // return the number of nodes in the tree stringGraph that point to original
//    private int checkIndegreeAUX(StringGraph fixedNode, StringGraph son){
//        //prblema del caso base perchè una foglia può puntare verso su
//        int result = 0; // leaf case
//
//        // if a son does have an inner edge to myself, and it is my direct son
//        if (fixedNode.getSons().contains(son) && fixedNode.father.equals(son))
//            return 1;
//
//        if (son.getSons().contains(fixedNode))
//            result = 1;
//
//        for(StringGraph s: son.getSons()) {
//            if (!s.equals(son.father))
//                result += checkIndegreeAUX(fixedNode, s);
//            else if (son.father.equals(fixedNode))
//                ++result;
//        }
//        return result;
//    }


    private Collection<NodeType> getPrincipalLabels() {

        // Check for nodes in principal nodes and extract their labels
        return getPrincipalNodes().stream().map(StringGraph::getLabel).collect(Collectors.toSet());
    }

    Collection<StringGraph> getPrincipalNodes() {

        if (this.label != NodeType.OR)
            return Set.of(this);
        else {
            Collection<StringGraph> principalSubNodes = new HashSet<>();

            for (StringGraph son : this.getSons()) {
                principalSubNodes.addAll(son.getPrincipalNodes());
            }

            principalSubNodes.add(this);
            return principalSubNodes;
        }

    }

    private boolean checkPartialOrder (StringGraph first, StringGraph second, List<Pair<StringGraph, StringGraph>> edges) {
        Pair<StringGraph, StringGraph> currentEdge = new Pair<>(first, second);
        if (edges.contains(currentEdge)) return true;
        else if (second.getLabel() == StringGraph.NodeType.MAX) return true;
        else if (first.getLabel() == StringGraph.NodeType.CONCAT &&
                second.getLabel() == StringGraph.NodeType.CONCAT &&
                first.getSons().size() == second.getSons().size() &&
                first.getSons().size() > 0) {
            boolean result = true;
            edges.add(new Pair<>(first, second));
            for(int i = 0; i < first.getSons().size(); i++){
                result = result && checkPartialOrder(first.getSons().get(i), second.getSons().get(i), edges);
            }
            return result;
        }
        else if (first.getLabel() == StringGraph.NodeType.OR &&
                second.getLabel() == StringGraph.NodeType.OR){
            boolean result = true;
            edges.add(new Pair<>(first, second));
            for(StringGraph son : first.getSons()){
                result = result && checkPartialOrder(son, second, edges);
            }
            return result;
        }
        else if (second.getLabel() == StringGraph.NodeType.OR) {
            boolean result = false;
            List<StringGraph> labelEqualitySons = labelEqualitySet(second.getPrincipalNodes(), first);
            if (labelEqualitySons.size() > 0) {
                edges.add(new Pair<>(first, second));
                for (StringGraph s : labelEqualitySons) {
                    result = result || checkPartialOrder(first, s, edges);
                }
            }
            return result;
        } else {
            return first.getLabel().equals(second.getLabel());
        }
    }

    public static List<StringGraph> labelEqualitySet(Collection<StringGraph> stringGraphList, StringGraph stringGraph) {
        List<StringGraph> result = new ArrayList<>();
        for(StringGraph s : stringGraphList) {
            if (s.getLabel().equals(stringGraph.getLabel())) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean correspondenceSet(StringGraph other) {
        return !(this.depth() == other.depth() && this.getPrincipalLabels().equals(other.getPrincipalLabels()));
    }


    private boolean cycleInductionRule(StringGraph other) {
        return false;
    }

    private boolean replacementRule(StringGraph other) {

        boolean result = false;

        if (this.wideningTopologicalClash(other)) {
            for (StringGraph son : other.getSons()) {
                result = this.replacementRuleAux(son);
                if (result)
                    break;
            }
        }

        return result;
    }

    private boolean replacementRuleAux(StringGraph other) {

        Collection<StringGraph> ancertors = other.getAncestors();

        boolean result = false;

        // ancestor --> va
        // other    --> vn
        // son      --> vo
        for (StringGraph son : this.getSons()) {
            for (StringGraph ancestor : ancertors) {
                if (!checkPartialOrder(other, ancestor, new ArrayList<>()) &&
                    son.depth() >= ancestor.depth() && (ancestor.getPrincipalLabels().contains(other.getPrincipalLabels()) || son.depth() < other.depth())) {
                    result = true;
                    break;
                }
            }
        }

        return result; // No ancestor for root node

    }

    private boolean wideningTopologicalClash(StringGraph other) {

        if (this.topologicalClash(other)) {
           return this.wideningTopologicalClashAux(other);
        }
        return false;
    }

    private boolean wideningTopologicalClashAux(StringGraph other) {

        boolean result = false;

        // Checking all left sub nodes with all right sub nodes
        for (StringGraph son : this.getSons()) {
            for (StringGraph otherSon : other.getSons()) {
                result = result || son.wideningTopologicalClash(otherSon);
                if (result)
                    break;
            }
        }

        return result || (!other.getPrincipalLabels().isEmpty() &&
                         (!this.getPrincipalLabels().equals(other.getPrincipalLabels()) && this.depth() == other.depth()) ||
                           this.depth() < other.depth());
    }

    private boolean topologicalClash(StringGraph other) {

        if (checkPartialOrder(this, other, new ArrayList<>())) {
            return this.topologicalClashAux(other);
        }
        return false;
    }

    private boolean topologicalClashAux(StringGraph other) {

        boolean result = false;

        // Checking all left sub nodes with all right sub nodes
        for (StringGraph son : this.getSons()) {
            for (StringGraph otherSon : other.getSons()) {
                result = result || son.topologicalClash(otherSon);
                if (result)
                    break;
            }
        }

        return result || (this.correspondenceSet(other) &&
                !(this.depth() == other.depth() && this.getPrincipalLabels().equals(other.getPrincipalLabels())));
    }

    private Collection<StringGraph> getAncestors() {

        Collection<StringGraph> ancestors = new HashSet<>();

        if (this.getFathers().isEmpty())
            return new HashSet<>();


        for (StringGraph father : this.getFathers()) {
            ancestors.addAll(this.getFathers());
            ancestors.addAll(father.getAncestors());
        }

        return ancestors;
    }

    private int depth() {
        if(this.getSons().size() == 0) {
            return 1;
        }
        else {
            int graphDepth = 0;
            for(StringGraph son : this.getSons())
                graphDepth += son.depth();
            return ++graphDepth; // Considering root of a complete graph
        }
    }

    /**
     * In `this` string graph, replace nodeToBeReplaced with nodeToReplace
     * @param nodeToBeReplaced
     * @param replacingNode
     */
    private void replaceVertex(StringGraph nodeToBeReplaced, StringGraph replacingNode) {
        if (this.searchForNode(nodeToBeReplaced) && this.searchForNode(replacingNode)) {
            List<StringGraph> fathers = nodeToBeReplaced.getFathers();
            List<StringGraph> sons = nodeToBeReplaced.getSons();

            for(StringGraph father : fathers) {
                father.removeSon(nodeToBeReplaced);
                father.addSon(replacingNode);
            }

            nodeToBeReplaced.removeAllSons();
            replacingNode.addAllSons(sons);

        }
    }

    private void replaceEdge(StringGraph edgeToBeRemoved, StringGraph edgeToBeAdded) {
        if (this.searchForNode(edgeToBeRemoved) && this.searchForNode(edgeToBeAdded)) {
            this.removeSon(edgeToBeRemoved);
            this.addSon(edgeToBeAdded);
        }
    }

    private boolean searchForNode(StringGraph node) {
        boolean result;

        if(this.equals(node)) {
            return true;
        }
        else {
            for (StringGraph son : this.getSons()) {
                result = son.searchForNode(node);
                if (result)
                    return true;
            }
            return false;
        }
    }
}
