package it.unive.scsr;

import it.unive.scsr.Exceptions.WrongBuildStringGraphException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StringGraph {

    enum NodeType {
        CONCAT,
        OR,
        EMPTY,
        MAX;
        enum CHARACTER {
            a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
        }
    }


    private NodeType label;
    private List<StringGraph> fathers;
    private List<StringGraph> sons;
    private boolean normalized = false;

    public StringGraph(NodeType label, List<StringGraph> sons) {
        this.label = label;
        this.sons = sons;

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
    }

    public StringGraph(NodeType label) {
        assert(!(label == NodeType.CONCAT));
        this.label = label;
        this.sons = new ArrayList<>();
        this.fathers = new ArrayList<>();
    }

    public void addSon(StringGraph son){
        this.getSons().add(son);
        son.getFathers().add(this);
    }

    public void removeSon(StringGraph son){
        this.getSons().remove(son);
        son.getFathers().remove(this);
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

    public void setSons(List<StringGraph> sons) {
        this.sons = sons;
    }

    public List<StringGraph> getFathers() {
        return fathers;
    }

    public void setFathers(List<StringGraph> fathers) {
        this.fathers = fathers;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public boolean isNormalized() {
        return normalized;
    }


    protected void normalize() {
        for (StringGraph s : this.getSons()){
            if (this.getFathers().contains(s))
                return;
            else
                s.normalize();
        }

        // RULE 1
        if (this.label == NodeType.CONCAT && this.sons.size() == 1) {
            this.setLabel(this.sons.get(0).getLabel());
            this.setSons(this.sons.get(0).getSons());
        }

        // RULE 2
        if (this.label == NodeType.CONCAT) {
            AtomicBoolean applyRule = new AtomicBoolean(true);
            for(StringGraph son: this.getSons()) {
                if (son.label != NodeType.MAX) {
                    applyRule.set(false);
                }
            }
            if (applyRule.get()) {
                this.setLabel(NodeType.MAX);
                this.setSons(null);
            }

        }

        // RULE 3
        if (this.label == NodeType.CONCAT) {
            if (this.getSons().size() >= 2) {
                AtomicReference<StringGraph> previousSibling;
                AtomicReference<StringGraph> currentSibling;

                int pos = 0;
                boolean appliedRule = false;
                do {
                    previousSibling = new AtomicReference<>(this.getSons().get(pos));
                    currentSibling = new AtomicReference<>(this.getSons().get(++pos));

                    if (previousSibling.get().label == NodeType.CONCAT && currentSibling.get().label == NodeType.CONCAT &&
                            (previousSibling.get().getFathers().size() == 1) && (currentSibling.get().getFathers().size() == 1)) {

                        previousSibling.get().getSons().addAll(currentSibling.get().getSons()); // Adding currentSibling sons to previous sibling sons
                        previousSibling.get().setSons(previousSibling.get().getSons());

                        for (StringGraph s : currentSibling.get().getSons()) {
                            if (currentSibling.get().getFathers().contains(s)) {
                                s.removeSon(currentSibling.get()); // assuming just one edge pointing up
                                s.addSon(previousSibling.get());
                            }
                            s.getFathers().remove(currentSibling.get());
                            s.getFathers().add(previousSibling.get());
                        }
                        appliedRule = true;
                    }
                    ++pos;
                } while(pos < this.getSons().size() && !appliedRule);

                if (appliedRule){
                    this.getSons().remove(currentSibling.get());
                }

            }
        }

        // RULE 4
        if (this.label == NodeType.CONCAT &&
            this.getSons().get(0).getFathers().size() == 1 &&
            this.getSons().get(0).getLabel() == NodeType.CONCAT) {

            for (StringGraph s : this.getSons().get(0).getSons()) {
                this.getSons().add(s); // adding son to first concat
                s.getFathers().remove(this.getSons().get(0)); // removing intermediate concat to fathers' list
                s.getFathers().add(this); // adding first concat to fathers' list
            }
        }

        // At the end we know for sure that our StringGraph is normalized
        this.setNormalized(true);
    }

    public boolean contains(NodeType.CHARACTER c) {
        // boolean contains = false;

        if (this.label == NodeType.OR) return false;

        else if (this.label == NodeType.CONCAT) {
            for (StringGraph s : this.getSons()) {
                //if (s.getLabel() == c) return true;
            }
        }

        return false;
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

    public StringGraph substring(int leftbound, int rightbound) {

        if (this.label == NodeType.CONCAT &&
            this.getSons().size() >= rightbound - 1) {

            for(int i = 0; i < rightbound - 1; ++i) {
                if (this.getSons().get(i).label != NodeType.CHARACTER)
                    return StringGraph.buildMAX();
            }

            List<StringGraph> substringSons = new ArrayList<>();
            for(int i = leftbound; i < rightbound - 1; ++i) {
                substringSons.add(this.getSons().get(i));
            }
            return new StringGraph(NodeType.CONCAT, substringSons);
        }
        return StringGraph.buildMAX();
    }

    public static StringGraph buildCONCAT(StringGraph left, StringGraph right) {
        return new StringGraph(NodeType.CONCAT, new ArrayList<>(List.of(left, right)));
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


}
