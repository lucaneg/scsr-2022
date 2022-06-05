package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.scsr.Exceptions.WrongBuildStringGraphException;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.*;
import static it.unive.scsr.StringGraph.NodeType.SIMPLE;
import static it.unive.scsr.StringGraph.NodeType.MAX;
import static it.unive.scsr.StringGraph.NodeType.EMPTY;
import static it.unive.scsr.StringGraph.NodeType.CONCAT;
import static it.unive.scsr.StringGraph.NodeType.OR;
/**
 * StringGraph: base unit of abstract domain StringGraphDomain
 */
public class StringGraph {

    enum NodeType {
        CONCAT,
        OR,
        EMPTY,
        MAX,
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

        boolean allSonsSIMPLE = true;
        for (StringGraph son: sons) {
            if (son.getLabel() != NodeType.SIMPLE) {
                allSonsSIMPLE = false;
                break;
            }
        }

        if (allSonsSIMPLE) this.normalized = true;

        // Checking CONCAT and OR node have at least one node
        if ((label == CONCAT && (this.getSons().isEmpty())) ||
            (label == OR && (this.getSons().isEmpty())))
            throw new WrongBuildStringGraphException("OR node and CONCAT node must have at least one son.");

        // We make sure root has no sons
        if (this.label == MAX && this.getSons().size() > 0)
            throw new WrongBuildStringGraphException("MAX node cannot have son.");

        if (this.label == SIMPLE && Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("SIMPLE node must have a character associated.");
        }

        if (this.label != SIMPLE && !Objects.isNull(this.character)) {
            throw new WrongBuildStringGraphException("Only SIMPLE can have a character associated.");
        }

    }

    public StringGraph(String stringToRepresent) {
        this.normalized = true;
        if (isStringInt(stringToRepresent)) {
            this.label = SIMPLE;
            this.setBound(Integer.parseInt(stringToRepresent));
        } else if (stringToRepresent.length() == 0) {
            this.label = EMPTY;
        } else if (stringToRepresent.length() == 1) {
            this.label = SIMPLE;
            try {
                this.character = StringGraph.map(stringToRepresent.charAt(0));
            } catch (WrongBuildStringGraphException e) {
                this.label = MAX;
            }
        } else {
            this.label = CONCAT;
            for (int i = 0; i < stringToRepresent.length(); i++) {
                StringGraph son = new StringGraph(SIMPLE, new ArrayList<>(), StringGraph.map(stringToRepresent.charAt(i)));
                this.addSon(son);
            }
        }

    }

    public StringGraph(NodeType label) {
        assert(!(label == CONCAT));
        this.label = label;
        this.sons = new ArrayList<>();
        this.fathers = new ArrayList<>();
    }

    /**
     * Try to convert a string to integer and return true if the conversion succeed.
     * @param s string to check integer type
     * @return true if string can be represented as an int value, false otherwise.
     */
    private boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Manage mapping of a single character into a CHARACTER enum value.
     * @param c character to map
     * @return character mapped in CHARACTER enum value if possible, otherwise throw WrongBuildStringGraphException exception
     */
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

    /**
     * Add {@code son} to current sons.
     * @param son String graph to add as son.
     */
    public void addSon(StringGraph son) {
        if (!this.sons.contains(son)) this.getSons().add(son);
        if (!son.getFathers().contains(this)) son.getFathers().add(this);
    }

    /**
     * Remove {@code son} from current sons.
     * @param son String graph to be removed from sons.
     */
    public void removeSon(StringGraph son){
        this.getSons().remove(son);
        son.getFathers().remove(this);
    }

    /**
     * Remove {@code sons} from current sons.
     * @param sons list of sons to be removed from all sons.
     */
    public void removeSons(List<StringGraph> sons) {
        this.getSons().removeAll(sons);

        for (StringGraph son : sons) {
            this.removeSon(son);
        }
    }

    /**
     * Add {@code sons} to the current list of sons.
     * @param sons list of sons to be added to all sons.
     */
    public void addAllSons(List<StringGraph> sons){
        for(StringGraph s : sons) {
            this.addSon(s);
        }
    }

    /**
     * Remove all sons.
     */
    public void removeAllSons(){
        for (StringGraph s : this.getSons()) {
            s.getFathers().remove(this);
        }
        this.sons = new ArrayList<>();
    }

    /**
     * Remove all fathers.
     */
    public void removeAllFathers(){
        for (StringGraph f : this.getFathers()) {
            f.getSons().remove(this);
        }
        this.fathers = new ArrayList<>();
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

    public List<StringGraph> getFathers() {
        return fathers;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public CHARACTER getCharacter() {
        return character;
    }

    public Integer getBound() {
        return bound;
    }

    public void setBound(Integer bound) {
        this.bound = bound;
    }

    /**
     * Compact the string graph by applying the 8 rule defined in the article
     * <i>"Deriving Descriptions of Possible Values of Program Variables by Means of Abstract Interpretation"</i>
     */
    protected void compact() {
        for (StringGraph s : this.getSons()){
            if (!(this.getFathers().contains(s))) {
                s.compact();
            }
        }

        // RULE 1
        if (this.getLabel() == CONCAT && !nonEmptyDenotation()) {
            this.setLabel(EMPTY);
            this.removeAllSons();
        }

        // RULE 2
        if (this.getLabel() == OR){
            List<StringGraph> emptyNodes = new ArrayList<>();
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == EMPTY) {
                    emptyNodes.add(son);
                }
            }
            this.removeSons(emptyNodes);
        }

        // RULE 3
        if (this.getLabel() == OR && this.getSons().contains(this)){
            this.removeSon(this);
        }

        // RULE 4
        if (this.getLabel() == OR && this.getSons().isEmpty()){
            this.setLabel(EMPTY);
            this.removeAllSons();
        }

        // RULE 5
        if (this.getLabel() == OR){
            boolean hasMaxSon = false;
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == MAX) {
                    hasMaxSon = true;
                    break;
                }
            }
            if (hasMaxSon){
                this.setLabel(MAX);
                this.removeAllSons();
            }
        }

        // RULE 6
        if (this.getLabel() == OR){
            ArrayList<StringGraph> sonsToRemove = new ArrayList<>();
            for(StringGraph son: this.getSons()) {
                if (son.getLabel() == OR && son.getFathers().size() == 1) {
                    sonsToRemove.add(son);
                    this.addAllSons(son.getSons());
                    son.removeAllSons();
                }
            }
            this.removeSons(sonsToRemove);
        }

        // RULE 7
        if (this.getLabel() == OR && this.getSons().size() == 1){
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
        if (this.getLabel() == OR){
            for(StringGraph son: this.getSons()){
                if (son.getLabel() == OR && son.getFathers().size() > 1) {
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

    /**
     * Normalize the string graph by applying the 4 rules defined in the article
     * <i>"A Suite of Abstract Domains for Static Analysis of String Values"</i>
     */
    protected void normalize() {

        if (!this.isNormalized()) {

            for (StringGraph s : this.getSons()) {
                if (!(this.getFathers().contains(s)))
                    s.normalize();
            }

            // RULE 1
            if (this.getLabel() == CONCAT && this.getSons().size() == 1) {
                StringGraph s = this.getSons().get(0);
                this.setLabel(s.getLabel());
                this.removeAllSons();
                this.addAllSons(s.getSons());
                for (StringGraph father : s.getFathers()) {
                    father.addSon(this);
                    father.removeSon(s);
                }
            }

            // RULE 2
            if (this.getLabel() == CONCAT) {
                boolean allMaxSons = true;
                for (StringGraph son : this.getSons()) {
                    if (son.getLabel() != MAX) {
                        allMaxSons = false;
                        break;
                    }
                }
                if (allMaxSons) {
                    this.setLabel(MAX);
                    this.removeAllSons();
                }
            }

            // RULE 3
            if (this.getLabel() == CONCAT) {
                if (this.getSons().size() >= 2) {
                    AtomicReference<StringGraph> previousSibling;
                    AtomicReference<StringGraph> currentSibling;

                    int pos = 0;
                    do {
                        previousSibling = new AtomicReference<>(this.getSons().get(pos));
                        currentSibling = new AtomicReference<>(this.getSons().get(++pos));

                        if (previousSibling.get().getLabel() == CONCAT && currentSibling.get().getLabel() == CONCAT &&
                                (previousSibling.get().getFathers().size() == 1) && (currentSibling.get().getFathers().size() == 1)) {

                            previousSibling.get().addAllSons(currentSibling.get().getSons()); // Adding currentSibling sons to previous sibling sons
                            currentSibling.get().removeAllSons();


                            for (StringGraph father : currentSibling.get().getFathers()) {
                                father.addSon(previousSibling.get());
                            }
                            currentSibling.get().removeAllFathers();
                            //appliedRule = true;
                        }
                        ++pos;
                    } while (pos < this.getSons().size() - 1);

                }
            }

            // RULE 4
            if (this.label == CONCAT) {
                List<StringGraph> concatSonsToAdd = new ArrayList<>();
                List<StringGraph> sonsToRemove = new ArrayList<>();
                for (StringGraph son : this.getSons()) {
                    if (son.getLabel() == CONCAT && son.getFathers().size() == 1) {
                        concatSonsToAdd.addAll(son.getSons());
                        sonsToRemove.add(son);
                    }
                }
                this.addAllSons(concatSonsToAdd);
                for (StringGraph son : sonsToRemove) {
                    son.removeAllFathers(); // don't need for bc son has only one father
                    son.removeAllSons(); // need to remove the sons' fathers lists
                }
            }

            // At the end we know for sure that our StringGraph is normalized
            this.setNormalized(true);
        }
    }

    /**
     * Check if inside a string graph we have a character {@code c} without an OR node presence.
     * @param c character to check presence
     */
    private boolean containsCharWithoutOR(CHARACTER c) {
        boolean result = false;
        if (this.label == SIMPLE) return this.character == c;
        else if (this.label == MAX) return true;
        else if (this.label == EMPTY) return false;
        else if (this.label == OR) return false;
        else if (this.label == CONCAT) {

            for (StringGraph s : this.getSons()) {
                result = result || s.containsCharWithoutOR(c);
            }

        }
        return result;
    }

    /**
     * Check if inside a string graph we have a character {@code c} or a MAX node.
     * @param c character to check presence
     */
    private boolean containsCharOrMax(CHARACTER c) {
        boolean result = false;
        if (this.label == SIMPLE) return this.character == c;
        else if (this.label == MAX) return true;
        else {
            for (StringGraph s : this.getSons()) {
                result = result || s.containsCharOrMax(c);
            }
        }
        return result;
    }

    /**
     * Check if inside a string graph we have a character {@code c} presence.
     * @param c character to check presence
     */
    public SemanticDomain.Satisfiability contains(CHARACTER c) {
        if (!containsCharOrMax(c)) return NOT_SATISFIED;
        else if (containsCharWithoutOR(c)) return SATISFIED;
        else return UNKNOWN;
    }

    /**
     * Check if string graph denotation is not empty (i.e. the set of finite strings represented by a node n in the string graph T is said to be the
     * denotation of the node n)
     */
    private boolean nonEmptyDenotation() {
        boolean result;
        if(this.label == CONCAT && this.getSons().size() > 0) {
            result = true;
            for (StringGraph son : this.getSons()) {
                result = result && son.nonEmptyDenotation();
            }
            return result;
        } else if(this.label == OR) {
            result = false;
            for (StringGraph son : this.getSons()) {
                result = result || son.nonEmptyDenotation();
            }
            return result;
        }
        return this.label == SIMPLE || (this.label == CONCAT && this.getSons().isEmpty());
    }

    /**
     * Return a string graph representing the string between string {@code leftBound} and {@code rightBound}.
     * @param leftBound index of the first character of the substring to be taken
     * @param rightBound index (excluded) of the last character of the substring to be taken
     * @return substring if bounds are correctly specified, TOP otherwise
     */
    public StringGraph substring(int leftBound, int rightBound) {

        if (this.getLabel() == CONCAT &&
            this.getSons().size() >= rightBound && 
            leftBound >= 0) {

            for(int i = 0; i < rightBound; ++i) {
                if (this.getSons().get(i).getLabel() != SIMPLE)
                    return StringGraph.buildMAX();
            }

            List<StringGraph> substringSons = new ArrayList<>();
            for(int i = leftBound; i < rightBound; ++i) {
                substringSons.add(new StringGraph(this.getSons().get(i).character.toString())); // father adding will be u
            }
            return new StringGraph(CONCAT, substringSons, null);
        }
        return StringGraph.buildMAX();
    }

    /**
     * Concatenate two string graphs in a single string graph.
     * @param left left string graph to be concatenated
     * @param right left string graph to be concatenated
     * @return a concatenated string graph
     */
    public static StringGraph buildCONCAT(StringGraph left, StringGraph right) {
        return new StringGraph(CONCAT, new ArrayList<>(List.of(left, right)), null);
    }

    /**
     * TOP is an element of S that is larger than every other element of S (the greatest element).
     * where S a subset of a partially ordered set (poset)
     * @return string graph representing TOP in our String Graph domain.
     */
    public static StringGraph buildMAX() {
        return new StringGraph(MAX);
    }

    /**
     * BOTTOM is an element of S that is smaller than every other element of S (the least element).
     * where S a subset of a partially ordered set (poset)
     * @return string graph representing BOTTOM in our String Graph domain.
     */
    public static StringGraph buildEMPTY() {
        return new StringGraph(EMPTY); //
    }

    /**
     * Get principal labels of our string graph. Principal labels are based on the definition of principal nodes.
     * Set of principal label are SIMPLE, CONCAT, MAX, OR, and EMPTY.
     * @return collection returning string graph's principal labels
     */
    private Collection<NodeType> getPrincipalLabels() {

        // Check for nodes in principal nodes and extract their labels
        return getPrincipalNodes().stream().map(StringGraph::getLabel).collect(Collectors.toSet());
    }

    /**
     * Get principal nodes of our string graph.
     * @return collection returning string graph's principal nodes
     */
    Collection<StringGraph> getPrincipalNodes() {

        if (this.label != OR)
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

    /**
     * Auxiliary function to check the partial order between two string graphs.
     * @param first string graph to be compared
     * @param second string graph to be compared
     * @param edges set of pairs of already visited string graphs
     * @return true if {@code first} is contained or equal to {@code second}, false otherwise.
     */
    public static boolean checkPartialOrder (StringGraph first, StringGraph second, List<Pair<StringGraph, StringGraph>> edges) {
        Pair<StringGraph, StringGraph> currentEdge = new Pair<>(first, second);
        if (edges.contains(currentEdge)) return true;
        else if (second.getLabel() == MAX) return true;
        else if (first.getLabel() == CONCAT &&
                second.getLabel() == CONCAT &&
                first.getSons().size() == second.getSons().size() &&
                first.getSons().size() > 0) {
            boolean result = true;
            edges.add(new Pair<>(first, second));
            for(int i = 0; i < first.getSons().size(); i++){
                result = result && checkPartialOrder(first.getSons().get(i), second.getSons().get(i), edges);
            }
            return result;
        }
        else if (first.getLabel() == OR &&
                second.getLabel() == OR){
            boolean result = true;
            edges.add(new Pair<>(first, second));
            for(StringGraph son : first.getSons()){
                result = result && checkPartialOrder(son, second, edges);
            }
            return result;
        }
        else if (second.getLabel() == OR) {
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

    /**
     *
     * @param stringGraphList collection of string graph to consider
     * @param stringGraph string graph to compare1
     * @return the list of string graphs belonging to {@code stringGraphList} which have the same label as {@code stringGraph}
     */
    public static List<StringGraph> labelEqualitySet(Collection<StringGraph> stringGraphList, StringGraph stringGraph) {
        List<StringGraph> result = new ArrayList<>();
        for(StringGraph s : stringGraphList) {
            if (s.getLabel().equals(stringGraph.getLabel())) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * The correspondence set between two type graphs g1 and g2, denoted
     * by c(g1, g2), is the smallest relation R closed by the following two rules:
     * <ul>
     *     <li>(root(gl), root(g2)) &#8712 R.</li>
     *     <li>(v1 ,v2) &#8712 R & same-depth(v1, v2) & same-pf(v1,v2) =>
     *          (succ(v1,i), succ(v2,i)) &#8712 R(1 &#8804 i &#8804 arity(v1)).</li>
     * </ul>
     * @param other string graph to compare
     * @return true if correspondence set match, false otherwise
     */
    private boolean correspondenceSet(StringGraph other) {
        // this g1
        // other g2

        return !(this.depth(new HashSet<>()) == other.depth(new HashSet<>()) && this.getPrincipalLabels().equals(other.getPrincipalLabels()));
    }

    /**
     * Introduce a cycle in the graph by replacing edges to a vertex with edges to one of its ancestors.
     * @param root string graph where we check for sons matching condition (see condition on method <i>cycleInductionRuleAux</i>)
     * @param other string graph where we check for son-ancestor matching condition (see method <i>cycleInductionRuleAux</i>)
     * @return string graph with edges replaced
     */
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
            other.replaceEdge(nodesToCheck.a, nodesToCheck.b);  // vertex from other is replaced with ancestor from other
            return other;
        }
        return null;
    }

    /**
     * Return a pair of string graph matching the condition:
     * <ul>
     *     <li>Cl(go, gn) ={((v, Vn)(V, va) | (vo, Vn) &#8712 WTC(go, gn) & Va &#8712 ancestor(vn) & (va &#8805 Vn) &
     *      depth(vo) > depth(va) & v = ca(vo, Vn) ↓ 2}.</li>
     * </ul>
     * @param other string graph representing vn vertex
     * @return pair of string graph if condition above match
     */
    private Pair<StringGraph, StringGraph> cycleInductionRuleAux(StringGraph other) {
        Collection<StringGraph> ancestors = other.getAncestors();
        Pair<StringGraph, StringGraph> pair = null;

        // ancestor --> va
        // other    --> vn
        // son      --> vo
        for (StringGraph son : this.getSons()) {
            for (StringGraph ancestor : ancestors) {
                if (checkPartialOrder(other, ancestor, new ArrayList<>()) &&
                    son.depth(new HashSet<>()) >= ancestor.depth(new HashSet<>()) &&
                    son.depth(new HashSet<>()) - other.depth(new HashSet<>()) < 2) {
                    pair = new Pair<>(other, ancestor);
                    break;
                }
            }
        }

        return pair; // No ancestor for root node
    }

    /**
     * Replaces the ancestor by an upper bound of the vertices.
     * Applies when a cycle cannot be introduced because the denotation of the ancestor is not greater than the vertices in the clash.
     * <br>
     * CR(go,gn) = {(Vn,Va) | (Vo,Vn) &#8712 WTC(go,gn) &
     * Va &#8712 ancestor (Vn) & &#8989(Va &#8805 Vn) &
     * depth(Vo) &#8805 depth(va) & (pf(Vn) &#8838 pf(Va) V depth(Vo) < depth(Vn))}
     *
     * @param root string graph where we check for sons matching condition (see condition on method <i>replacementRuleAux</i>)
     * @param other string graph where we check for son-ancestor matching condition (see method <i>replacementRuleAux</i>)
     * @return
     */
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
            other.replaceVertex(nodesToCheck.a, nodesToCheck.b); // ancestor is replaced
            return other;
        }
        return null;
    }

    /**
     * Check for ancestor, other, and son triple matching <i>replacementRule</i> condition.
     * @param other vertex where we check for ancestor matching condition
     * @return pair of vertex-ancestor to be replaced
     */
    private Pair<StringGraph, StringGraph> replacementRuleAux(StringGraph other) {

        Collection<StringGraph> ancestors = other.getAncestors();
        Pair<StringGraph, StringGraph> pair = null;

        // ancestor --> va
        // other    --> vn
        // son      --> vo
        for (StringGraph son : this.getSons()) {
            for (StringGraph ancestor : ancestors) {
                if (!checkPartialOrder(other, ancestor, new ArrayList<>()) &&
                    son.depth(new HashSet<>()) >= ancestor.depth(new HashSet<>()) &&
                    (ancestor.getPrincipalLabels().contains(other.getPrincipalLabels()) ||
                    son.depth(new HashSet<>()) < other.depth(new HashSet<>()))) {
                    pair = new Pair<>(ancestor, other);
                    break;
                }
            }
        }

        return pair; // No ancestor for root node

    }

    /**
     * The set of widening clashes, defined as follows.
     * <br>
     * WTC(g1, g2) = {(v1,v2) | (v1,v2) &#8712 TC(g1,g2) & pf(v2) &#8800 &#8709 &
     * ( (pf(v1) &#8800 pf(v2) & same-depth(v1, v2)) &#8744
     * depth(v1) < depth(v2) )}
     * @param other string graph to check for condition matching
     * @return true if there exist two vertexes in g1 and g2 matching the above condition, false otherwise
     */
    private boolean wideningTopologicalClash(StringGraph other) {

        if (this.topologicalClash(other)) {
           return this.wideningTopologicalClashAux(other);
        }
        return false;
    }


    /**
     * Checks for other sons and current string graph sons matching the condition. (check <i>wideningTopologicalClash</i> method)
     * @param other string graph to recursively compare vertexes
     * @return true if condition is matched, false otherwise
     */
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
                         (!this.getPrincipalLabels().equals(other.getPrincipalLabels()) && this.depth(new HashSet<>()) == other.depth(new HashSet<>())) ||
                           this.depth(new HashSet<>()) < other.depth(new HashSet<>()));
    }

    /**
     * Topological clash occurs when two vertices in correspondence have different
     * pf-sets or different depths.
     *
     * TC(g1, g2) = {(v1, v2) | (v1, v2) &#8712 C(g1, g2) &
     * &#8989(same-depth(v1,v2) & same-pf(v1,v2)}.
     * @param other string graph matching the condition above
     * @return true if condition is matched, false otherwise
     */
    private boolean topologicalClash(StringGraph other) {

        if (checkPartialOrder(this, other, new ArrayList<>())) {
            return this.topologicalClashAux(other);
        }
        return false;
    }

    /**
     * Check for other and current string graph matching the condition.
     * @param other vertex to check for condition matching (check method <i>topologicalClash</i>)
     * @return true if condition is matched, false otherwise
     */
    private boolean topologicalClashAux(StringGraph other) {

        boolean result = false;

        // Checking all left sub nodes with all right sub nodes
        for (StringGraph son : this.getSons()) {
            for (StringGraph otherSon : other.getSons()) {
                result = result || son.topologicalClashAux(otherSon);
                if (result)
                    break;
            }
        }

        return result || (this.correspondenceSet(other) &&
                !(this.depth(new HashSet<>()) == other.depth(new HashSet<>()) && this.getPrincipalLabels().equals(other.getPrincipalLabels())));
    }

    /**
     * Get ancestors of current string graph.
     * @return collection containing the ancestor of the current string graph
     */
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

    /**
     * @return depth of a string graph
     */
    private int depth(Set<StringGraph> alreadyVisitedNodes) {
        if(alreadyVisitedNodes.contains(this)){ // already visited node case
            return - 1;
        }

        alreadyVisitedNodes.add(this);

        if(this.getFathers().isEmpty()) { // root case
            return 0;
        }
        else {
            int graphDepth = 0;

            for(StringGraph father : this.getFathers()){
                graphDepth += father.depth(alreadyVisitedNodes);
            }

            return graphDepth + 1; // Considering root of a complete graph
        }
    }

    /**
     * In `this` string graph, replace nodeToBeReplaced with replacingNode
     * @param nodeToBeReplaced node to be replaced
     * @param replacingNode replacing node
     */
    private void replaceVertex(StringGraph nodeToBeReplaced, StringGraph replacingNode) {
        if (this.searchForNode(nodeToBeReplaced, new HashSet<>()) && this.searchForNode(replacingNode, new HashSet<>())) {
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

    /**
     * In current string graph replace {@code edgeToBeRemoved} with {@code edgeToBeAdded}.
     * @param edgeToBeRemoved edge to be removed
     * @param edgeToBeAdded edge to be replaced
     */
    private void replaceEdge(StringGraph edgeToBeRemoved, StringGraph edgeToBeAdded) {
        if (this.searchForNode(edgeToBeRemoved, new HashSet<>()) && this.searchForNode(edgeToBeAdded, new HashSet<>())) {
            this.removeSon(edgeToBeRemoved);
            this.addSon(edgeToBeAdded);
        }
    }

    /**
     * Search for {@code node} in the string graph.
     * @param node node to search in the string graph
     * @return true if node is contained, false otherwise
     */
    private boolean searchForNode(StringGraph node, Set<StringGraph> alreadyVisited) {
        boolean result;

        alreadyVisited.add(this);
        if(this.equals(node)) {
            return true;
        }
        else {
            for (StringGraph son : this.getSons()) {
                if(!alreadyVisited.contains(son)) {
                    result = son.searchForNode(node, alreadyVisited);
                    if (result)
                        return true;
                }
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
                if (Objects.isNull(this.character))
                    return this.bound.toString();
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
