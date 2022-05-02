package it.unive.scsr;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StringGraph {

    enum NodeType {
        CONCAT, OR, MAX,
    }


    private NodeType root;
    private List<StringGraph> sons;
    private boolean normalized = false;

    public StringGraph(NodeType root, List<StringGraph> sons) {
        // We make sure root has no sons
        this.root = root;
        this.sons = sons;

        // TODO: capire se serve, ma non penso
//        if (root == NodeType.OR || root == NodeType.MAX){
//            sons.forEach(son -> {
//                if (son.getSons().contains(this)) {
//                    throw new WrongBuildStringGraphException();
//                }
//            });
//        }
    }

    public NodeType getRoot() {
        return root;
    }

    public void setRoot(NodeType root) {
        this.root = root;
    }

    public List<StringGraph> getSons() {
        return sons;
    }

    public void setSons(List<StringGraph> sons) {
        this.sons = sons;
    }

    protected void normalize(){

        // RULE 1
        if (this.root == NodeType.CONCAT && this.sons.size() == 1) {
            this.setRoot(this.sons.get(0).getRoot());
            this.setSons(this.sons.get(0).getSons());
        }

        // RULE 2
        if (this.root == NodeType.CONCAT) {
            AtomicBoolean applyRule = new AtomicBoolean(true);
            this.getSons().forEach(son -> {
                if (son.root != NodeType.MAX) {
                    applyRule.set(false);
                }
            });
            if (applyRule.get()) {
                this.setRoot(NodeType.MAX);
                this.setSons(null);
            }

        }

        // RULE 3
        if (this.root == NodeType.CONCAT) {
            if (this.getSons().size() >= 2) {
                AtomicReference<StringGraph> previousSibling;
                AtomicReference<StringGraph> currentSibling;

                int pos = 0;
                boolean appliedRule = false;
                do {
                    previousSibling = new AtomicReference<>(this.getSons().get(pos));
                    currentSibling = new AtomicReference<>(this.getSons().get(++pos));

                    if (previousSibling.get().root == NodeType.CONCAT &&
                        currentSibling.get().root == NodeType.CONCAT) { // TODO: need to have indegree == 1
                        previousSibling.get().getSons().addAll(currentSibling.get().getSons()); // Adding currentSibling sons to previous sibling sons
                        previousSibling.get().setSons(previousSibling.get().getSons());
                        appliedRule = true;
                    }
                    ++pos;
                } while(pos < this.getSons().size() && !appliedRule);

                if (appliedRule){
                    this.getSons().remove(currentSibling.get());
                }
            }

        }

        // At the end we know for sure that our StringGraph is normalized
        this.normalized = true;
    }

    protected boolean isNormalized() {
        return this.normalized;
    }


}
