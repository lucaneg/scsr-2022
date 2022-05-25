package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.program.cfg.statement.string.Concat;
import it.unive.scsr.Exceptions.InvalidCharacterException;
import it.unive.scsr.Exceptions.WrongBuildStringGraphException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.*;

public class StringGraph {

    enum NodeType {
        CONCAT,
        OR,
        EMPTY,
        MAX,
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
        if (!nonEmptyDenotation()) {
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
                if (son.getLabel() == NodeType.MAX) hasMaxSon = true;
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
            this.setLabel(this.getSons().get(0).getLabel());
            this.removeAllSons();
            this.addAllSons(this.getSons().get(0).getSons());
            this.getSons().get(0).removeAllSons();
            if (this.getSons().get(0).getFathers().size() > 0){
                for (StringGraph father : this.getSons().get(0).getFathers()){
                    father.addSon(this);
                    father.removeSon(this.getSons().get(0));
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


    private Set<NodeType> principalLabels() {
        switch(this.label) {
            case CONCAT: return Set.of(NodeType.CONCAT);
            case MAX: return Set.of(NodeType.MAX);
            case SIMPLE: return Set.of(NodeType.SIMPLE);
            case OR: {
                Set<NodeType> labels = new HashSet<>();
                for(StringGraph son : sons) {
                    labels.addAll(son.principalLabels());
                }
                return labels;
            }
            case EMPTY: return Set.of(NodeType.EMPTY);
            default: throw new WrongBuildStringGraphException("Invalid graph label");
        }
    }

}
