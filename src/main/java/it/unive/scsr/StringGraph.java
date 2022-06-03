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
        a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
    }

    private NodeType label;
    private List<StringGraph> fathers = new ArrayList<>();;
    private List<StringGraph> sons = new ArrayList<>();
    private boolean normalized = false;
    private CHARACTER character;
    private Integer bound; // NEEDED FOR evalTernaryExpression() METHOD

    public StringGraph(NodeType label, List<StringGraph> sons, CHARACTER character) {
        this.label = label;
        this.character = character;

        for (StringGraph son : sons) {
            this.addSon(son);
        }

        // Checking CONCAT and OR node have at least one node
        if ((label == NodeType.CONCAT && (this.getSons().size() == 0)) ||
            (label == NodeType.OR && (this.getSons().size() == 0)))
            throw new WrongBuildStringGraphException("OR node and CONCAT node must have at least one son.");

        // We make sure root has no sons
        if (this.label == NodeType.MAX && this.getSons().size() > 0)
            throw new WrongBuildStringGraphException("MAX node cannot have son.");

        if (this.label == NodeType.SIMPLE && Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("SIMPLE node must have a character associated.");
        }

        if (this.label != NodeType.SIMPLE && !Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("Only SIMPLE can have a character associated.");
        }

    }

    public StringGraph(String stringToRepresent) {
        this.normalized = true;
        if (isStringInt(stringToRepresent)) {
            this.label = NodeType.SIMPLE;
            this.bound = Integer.parseInt(stringToRepresent);
        } else if (stringToRepresent.length() == 0) {
            this.label = NodeType.EMPTY;
        } else if (stringToRepresent.length() == 1) {
            this.label = NodeType.SIMPLE;
            this.character = StringGraph.map(stringToRepresent.charAt(0));
        } else {
            this.label = NodeType.CONCAT;
            for (int i = 0; i < stringToRepresent.length(); i++) {
                StringGraph son = new StringGraph(NodeType.SIMPLE, new ArrayList<>(), StringGraph.map(stringToRepresent.charAt(i)));
                this.addSon(son);
            }
        }

    }



    private boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static CHARACTER map(char c) {
        switch (c) {
            case 'a': return CHARACTER.a;
            case 'b': return CHARACTER.b;
            case 'c': return CHARACTER.c;
            case 'd': return CHARACTER.d;
            case 'e': return CHARACTER.e;
            case 'f': return CHARACTER.f;
            case 'g': return CHARACTER.g;
            case 'h': return CHARACTER.h;
            case 'i': return CHARACTER.i;
            case 'j': return CHARACTER.j;
            case 'k': return CHARACTER.k;
            case 'l': return CHARACTER.l;
            case 'm': return CHARACTER.m;
            case 'n': return CHARACTER.n;
            case 'o': return CHARACTER.o;
            case 'p': return CHARACTER.p;
            case 'q': return CHARACTER.q;
            case 'r': return CHARACTER.r;
            case 's': return CHARACTER.s;
            case 't': return CHARACTER.t;
            case 'u': return CHARACTER.u;
            case 'v': return CHARACTER.v;
            case 'w': return CHARACTER.w;
            case 'x': return CHARACTER.x;
            case 'y': return CHARACTER.y;
            case 'z': return CHARACTER.z;
            default: throw new WrongBuildStringGraphException(c + " is not a valid character.");
        }
    }

    public StringGraph(NodeType label) {
        assert(!(label == NodeType.CONCAT));
        this.label = label;
        this.sons = new ArrayList<>();
        this.fathers = new ArrayList<>();
    }

    public void addSon(StringGraph son) {
        if (!this.sons.contains(son)) this.getSons().add(son);
        if (!son.getFathers().contains(this)) son.getFathers().add(this);
    }

    public void removeSon(StringGraph son){
        this.getSons().remove(son);
        son.getFathers().remove(this);
    }

    public void removeSons(List<StringGraph> sons) {
        this.getSons().removeAll(sons);

        for (StringGraph son : sons) {
            this.removeSon(son);
        }
    }

    public void addAllSons(List<StringGraph> sons){
        for(StringGraph s : sons) {
            this.addSon(s);
        }
    }

    public void removeAllSons(){
        for (StringGraph s : this.getSons()) {
            s.getFathers().remove(this);
        }
        this.sons = new ArrayList<>();
    }

    public void removeAllFathers(){
        for (StringGraph f : this.getFathers()) {
            f.getSons().remove(this);
        }
        this.fathers = new ArrayList<>();
    }

    public void removeFathers(List<StringGraph> fathers) {
        this.getFathers().removeAll(fathers);

        for (StringGraph father : fathers) {
            father.removeSon(this);
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
            List<StringGraph> emptyNodes = new ArrayList<>();
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == NodeType.EMPTY) {
                    emptyNodes.add(son);
                }
            }

            this.removeSons(emptyNodes);

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
            ArrayList<StringGraph> sonsToRemove = new ArrayList<>();
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == NodeType.OR && son.getFathers().size() == 1) {
                    sonsToRemove.add(son);
                    this.addAllSons(son.getSons());
                    son.removeAllSons();
                }
            }
            this.removeSons(sonsToRemove);
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
                }
                son.removeAllFathers();
            }
        }

        // RULE 8
        if (this.getLabel() == NodeType.OR){
            for(StringGraph son: this.getSons()){
                if (son.getLabel() == NodeType.OR && son.getFathers().size() > 1) {

//                    List<StringGraph> sonsToRemove = new ArrayList<>();
//                    for(StringGraph father : son.getFathers()){
//                        if(!father.equals(this)) {
//                            sonsToRemove.add(son);
//                            father.addSon(this);
//                        }
//                    }

                    for(StringGraph father : son.getFathers()) {
                        if (!father.equals(this)) {
                            father.addSon(this);
                        }
                    }
                    son.removeAllFathers();
                    this.addSon(son);
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
                if (son.getLabel() != NodeType.MAX) {
                    allMaxSons = false;
                    break;
                }
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
                        }
                        currentSibling.get().removeAllFathers();
                        //appliedRule = true;
                    }
                    ++pos;
                } while(pos < this.getSons().size() - 1/*&& !appliedRule*/);

                /* fatto con il for dentro all'if precedente
                if (appliedRule){
                    this.removeSon(currentSibling.get());
                }*/
            }
        }

        // RULE 4
        if (this.label == NodeType.CONCAT){
            List<StringGraph> concatSonsToAdd = new ArrayList<>();
            List<StringGraph> sonsToRemove = new ArrayList<>();
            for(StringGraph son : this.getSons()){
                if(son.getLabel() == NodeType.CONCAT && son.getFathers().size() == 1){
                    concatSonsToAdd.addAll(son.getSons());
                    sonsToRemove.add(son);
                }
            }
            this.addAllSons(concatSonsToAdd);
            for(StringGraph son : sonsToRemove){
                son.removeAllFathers(); // don't need for bc son has only one father
                son.removeAllSons(); // need to remove the sons' fathers lists
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

//    @Override
//    public boolean equals(Object o){
//        assert o.getClass() == StringGraph.class;
//        StringGraph other = (StringGraph)o;
//
//        if (this.getLabel() == ((StringGraph) o).getLabel())
//
//
//        if (this.getSons().equals(other.getSons()) && this.getFathers().equals(other.getFathers())) {
//            int i = 0;
//            for (StringGraph nodeToCompare : this.getSons()) {
//                if (!nodeToCompare.equals(other.getSons().get(i)))
//                    return false;
//                ++i;
//            }
//
//            return true;
//                /*
//                if (!(nodeToCompare.equals(other.getSons().get(i)) &&
//                        nodeToCompare.equals(other.getFathers().get(i))))
//                    return false;
//                else
//                    if(!nodeToCompare.equals(other.getSons().get(i)))
//                        return false;
//
//
//                    for (StringGraph otherGraphToCompare : other.getSons())
//                        nodeToCompare.equals(otherGraphToCompare);*/
//
//        }
//        else return false;
//    }

    public StringGraph substring(int leftBound, int rightBound) {

        if (this.getLabel() == NodeType.CONCAT &&
            this.getSons().size() >= rightBound - 1) {

            for(int i = 0; i < rightBound - 1; ++i) {
                if (this.getSons().get(i).getLabel() != NodeType.SIMPLE)
                    return StringGraph.buildMAX();
            }

            List<StringGraph> substringSons = new ArrayList<>();
            for(int i = leftBound; i < rightBound; ++i) {
                substringSons.add(new StringGraph(this.getSons().get(i).character.toString())); // father adding will be u
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

    public static boolean checkPartialOrder (StringGraph first, StringGraph second, List<Pair<StringGraph, StringGraph>> edges) {
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


    public static StringGraph cycleInductionRule(StringGraph root, StringGraph other) {
        Pair<StringGraph, StringGraph> nodesToCheck = null;
        if (root.wideningTopologicalClash(other)) {
            for (StringGraph son : other.getSons()) {
                nodesToCheck = root.cycleInductionRuleAux(son);
                if (!Objects.isNull(nodesToCheck))
                    break;
            }
        }

        if (!Objects.isNull(nodesToCheck)) {
            other.replaceEdge(nodesToCheck.a, nodesToCheck.b);
            return other;
        }
        return null;
    }

    private Pair<StringGraph, StringGraph> cycleInductionRuleAux(StringGraph other) {
        Collection<StringGraph> ancestors = other.getAncestors();
        Pair<StringGraph, StringGraph> pair = null;

        // ancestor --> va
        // other    --> vn
        // son      --> vo
        for (StringGraph son : this.getSons()) {
            for (StringGraph ancestor : ancestors) {
                if (checkPartialOrder(other, ancestor, new ArrayList<>()) &&
                    son.depth() >= ancestor.depth() &&
                    son.depth() - other.depth() < 2) {
                    pair = new Pair<>(ancestor, other);
                    break;
                }
            }
        }

        return pair; // No ancestor for root node
    }

    public static StringGraph replacementRule(StringGraph root, StringGraph other) {

        Pair<StringGraph, StringGraph> nodesToCheck = null;
        if (root.wideningTopologicalClash(other)) {
            for (StringGraph son : other.getSons()) {
                nodesToCheck = root.replacementRuleAux(son);
                if (!Objects.isNull(nodesToCheck))
                    break;
            }
        }

        if (!Objects.isNull(nodesToCheck)) {
            other.replaceVertex(nodesToCheck.a, nodesToCheck.b);
            return other;
        }
        return null;
    }

    private Pair<StringGraph, StringGraph> replacementRuleAux(StringGraph other) {

        Collection<StringGraph> ancestors = other.getAncestors();
        Pair<StringGraph, StringGraph> pair = null;

        // ancestor --> va
        // other    --> vn
        // son      --> vo
        for (StringGraph son : this.getSons()) {
            for (StringGraph ancestor : ancestors) {
                if (!checkPartialOrder(other, ancestor, new ArrayList<>()) &&
                    son.depth() >= ancestor.depth() &&
                    (ancestor.getPrincipalLabels().contains(other.getPrincipalLabels()) || son.depth() < other.depth())) {
                    pair = new Pair<>(ancestor, other);
                    break;
                }
            }
        }

        return pair; // No ancestor for root node

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

    @Override
    public String toString() {
        StringBuilder stringGraphToRepresent = new StringBuilder();

        switch (this.label) {
            case MAX:
                return "MAX";
            case EMPTY:
                return "EMPTY";
            case SIMPLE:
                return this.character.toString();
            case OR:
                stringGraphToRepresent.append("OR[");
                for (StringGraph son : this.getSons()) {
                    stringGraphToRepresent.append(son.toString());
                }
                stringGraphToRepresent.append("]");
                return stringGraphToRepresent.toString();
            case CONCAT:
                stringGraphToRepresent.append("CONCAT[");
                for (StringGraph son : this.getSons()) {
                    stringGraphToRepresent.append(son.toString());
                }
                stringGraphToRepresent.append("]");
                return stringGraphToRepresent.toString();
            default:
                throw new WrongBuildStringGraphException(String.format("Type %s does not exists.", this.label));
        }

    }
}
