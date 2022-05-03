package it.unive.scsr.project;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Brick extends BaseNonRelationalValueDomain<Brick> {

    static public class BrickIndicesValue {

        public int value;   //Value of the index
        public Types type;  //Type of the index, that determines if is valid index

        public BrickIndicesValue(int value, Types type) {
            this.value = value;
            this.type = type;
        }

        public BrickIndicesValue(int value) {
            this(value, Types.INDEX);
        }

        public int getValue() {return value;}

        public boolean isIndex() {
            return this.type == Types.INDEX;
        }

        public boolean isInfinity() {
            return this.type == Types.INFINITY;
        }

        public boolean equals(int value) {return this.value == value && this.type == Types.INDEX;}

        public boolean equals(BrickIndicesValue other) {
            return this.value == other.value && this.type == other.type;
        }

        public boolean less(BrickIndicesValue other) {
            if ((this.isInfinity() && other.isIndex()) || (this.isInfinity() && other.isInfinity()))
                return false;
            else if (this.isIndex() && other.isInfinity())
                return true;
            else
                return this.getValue() < other.getValue();
        }

        public boolean lessOrEqual(BrickIndicesValue other) {
            if ((this.isIndex() && other.isInfinity()) || (this.isInfinity() && other.isInfinity()))
                return true;
            else if (this.isInfinity() && other.isIndex())
                return false;
            else
                return this.getValue() <= other.getValue();
        }

        public boolean greater(BrickIndicesValue other) {
            if ((this.isIndex() && other.isInfinity()) || (this.isInfinity() && other.isInfinity()))
                return false;
            else if (this.isInfinity() && other.isIndex())
                return true;
            else
                return this.getValue() > other.getValue();
        }

        public boolean greaterOrEqual(BrickIndicesValue other) {
            if ((this.isInfinity() && other.isIndex()) || (this.isInfinity() && other.isInfinity()))
                return true;
            else if (this.isIndex() && other.isInfinity())
                return false;
            else
                return this.getValue() >= other.getValue();
        }

        public BrickIndicesValue sum(BrickIndicesValue other) {
            if ((this.isIndex() && other.isInfinity()) || (this.isInfinity() && other.isIndex()) || (this.isInfinity() && other.isInfinity()))
                return new BrickIndicesValue(0, Types.INFINITY);
            else
                return new BrickIndicesValue(this.getValue() + other.getValue());
        }

        public BrickIndicesValue diff(BrickIndicesValue other) throws ArithmeticException {
            if (this.less(other))
                throw new ArithmeticException("'this' must be greater than 'other'");
            else if (this.isInfinity())
                return new BrickIndicesValue(0, Types.INFINITY);
            else
                return new BrickIndicesValue(this.getValue() - other.getValue());
        }

        public static BrickIndicesValue max(BrickIndicesValue b1, BrickIndicesValue b2) {
            if (b1.isInfinity() || b2.isInfinity())
                return new BrickIndicesValue(0, Types.INFINITY);
            else
                return b1.greaterOrEqual(b2) ? b1 : b2;
        }

        public static BrickIndicesValue min(BrickIndicesValue b1, BrickIndicesValue b2){
            if (b1.isInfinity() && b2.isIndex()) return b2;
            else if (b1.isIndex() && b2.isInfinity()) return b1;
            else if (b1.isInfinity() && b2.isInfinity()) return b1;
            else return b1.lessOrEqual(b2) ? b1 : b2;
        }

        public String toString() {
            if (this.isIndex())
                return String.format("%s", this.getValue());
            else
                return String.format("+inf");
        }

    }

    //Define type for brick's indices
    enum Types {
        INDEX,      //Type for integer index
        INFINITY    //Type for Infinity index
    }

    private BrickIndicesValue min;
    private BrickIndicesValue max;
    private LinkedList<String> strings;

    private static final int boundS = 10;    //bound the size of the string set
    private static final int boundI = 10;    //bound the size of the range of a brick

    //create empty string "ε", Brick []^(0,0)
    public Brick() {
        this(new BrickIndicesValue(0),
                new BrickIndicesValue(0),
                new LinkedList<String>());
    }

    //create Brick [{strings}]^(1,1)
    public Brick(LinkedList<String> strings) {
        this(new BrickIndicesValue(1), new BrickIndicesValue(1), strings);
    }

    public Brick(int min, int max, LinkedList<String> strings) {
        this(new BrickIndicesValue(min), new BrickIndicesValue(max), strings);
    }

    //create Brick [{strings}]^(min,max)
    public Brick(BrickIndicesValue min, BrickIndicesValue max, LinkedList<String> strings) {
        this.min = min;
        this.max = max;
        this.strings = strings;
    }

    @Override
    protected Brick lubAux(Brick other) throws SemanticException {
        BrickIndicesValue m = BrickIndicesValue.min(this.min, other.min);
        BrickIndicesValue M = BrickIndicesValue.max(this.max, other.max);

        LinkedList<String> strs1 = this.strings;
        LinkedList<String> strs2 = other.strings;

        if (strs1.isEmpty() && !strs2.isEmpty()) return new Brick(m, M, strs2);
        if (!strs1.isEmpty() && strs2.isEmpty()) return new Brick(m, M, strs1);

        LinkedList<String> out = new LinkedList<>(strs1);

        for (String s: strs2) {
            if (!strs1.contains(s))
                out.add(s);
        }

        return new Brick(m, M, out);
    }

    @Override
    protected Brick glbAux(Brick other) throws SemanticException {
        BrickIndicesValue m = BrickIndicesValue.max(this.min, other.min);
        BrickIndicesValue M = BrickIndicesValue.min(this.max, other.max);

        LinkedList<String> strs1 = this.strings;
        LinkedList<String> strs2 = other.strings;

        if (strs1.isEmpty() || strs2.isEmpty()) return new Brick(m, M, new LinkedList<String>());

        LinkedList<String> out = new LinkedList<>();

        for (String s: strs1) {
            if (strs2.contains(s))
                out.add(s);
        }

        return new Brick(m, M, out);
    }

    @Override
    protected Brick wideningAux(Brick other) throws SemanticException {
        BrickIndicesValue m = BrickIndicesValue.min(this.min, other.min);
        BrickIndicesValue M = BrickIndicesValue.max(this.max, other.max);

        LinkedList<String> strs1 = this.strings;
        LinkedList<String> strs2 = other.strings;
        LinkedList<String> out = new LinkedList<>();

        if (this.strings.isEmpty() && !other.strings.isEmpty()) out = (LinkedList<String>) other.strings.clone();
        else if (!this.strings.isEmpty() && other.strings.isEmpty()) out = (LinkedList<String>) this.strings.clone();
        else {
            out.addAll(strs1);
            for (String s : strs2) {
                if (!strs1.contains(s))
                    out.add(s);
            }
        }

        if (out.size() > boundS || this.isTop() || other.isTop())
            return top();
        else if ((M.diff(m)).getValue() > boundI)
            return new Brick(new BrickIndicesValue(0), new BrickIndicesValue(0, Types.INFINITY), out);
        else
            return new Brick(m, M, out);

    }

    @Override
    protected boolean lessOrEqualAux(Brick other) throws SemanticException {
        if ((isSubSet(this.strings, other.strings)
                && this.min.lessOrEqual(other.min)
                && this.max.lessOrEqual(other.max))
        || (other.isTop())
        || (this.isBottom()))
            return true;
        else
            return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Brick other = (Brick) obj;
        if (this.strings.size() != other.strings.size()
                || !this.strings.containsAll(other.strings)
                || !this.min.equals(other.min)
                || !this.max.equals(other.max))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public DomainRepresentation representation() {
        String str = String.format("%s(%s,%s)",
                strings.toString(),
                this.min.toString(),
                this.max.toString());
        return new StringRepresentation(str);
    }

    @Override
    public Brick top() {
        return new Brick(new BrickIndicesValue(0),
                new BrickIndicesValue(0, Types.INFINITY),
                new LinkedList<>());
    }

    @Override
    public Brick bottom() {
        return new Brick(new BrickIndicesValue(2),
                new BrickIndicesValue(1),
                new LinkedList<String>());
    }

    @Override
    public boolean isTop() {
        return this.min.equals(0) && this.max.isInfinity();
    }

    @Override
    public boolean isBottom() {
        if ((this.strings.isEmpty() && !(this.min.equals(0) && this.max.equals(0)))
                || (!this.strings.isEmpty() && this.min.equals(0) && this.max.equals(0))
                || (this.max.less(this.min)))
            return true;
        else
            return false;
    }

    public boolean add(String s) {
        return this.strings.add(s);
    }

    /**  For norm rule 1 **/
    public boolean isEmptyBrick() {
        return this.strings.isEmpty()
                && this.min.equals(0)
                && this.max.equals(0);
    }


    /** For norm rule 2 **/
    public boolean canBeMergedWith(Brick other) {
        return this.min.equals(1) && this.max.equals(1) && other.min.equals(1) && other.max.equals(1);
    }

    public Brick merge(Brick other) {
        //The invalid bricks are emptyset, thus the merge return the set that
        if (this.isBottom() && !other.isBottom())
            return other;
        else if ((other.isBottom() && !this.isBottom()) || (this.isBottom() && other.isBottom()))
            return this;
        else {
            LinkedList<String> thisTemp = this.strings;
            LinkedList<String> otherTemp = other.strings;
            LinkedList<String> res = new LinkedList<>();

            for (String sThis: thisTemp) {
                for (String sOther: otherTemp) {
                    res.add(sThis.concat(sOther));
                }
            }
            return new Brick(res);
        }
    }

    /** For norm rule 3 **/
    public Brick transformCostantApplication() {
        if (this.max.equals(this.min)) {

            if (this.strings.isEmpty()) return new Brick(new LinkedList<String>());

            int m = this.min.getValue() - 1;
            Brick temp = new Brick(this.strings);
            Brick aux = new Brick(this.strings);

            for (; m > 0; m--) {
                aux = aux.merge(temp);
            }
            return aux;
        } else
            return this;
    }

    /** For norm rule 4 **/
    public boolean hasSameSet(Brick other) {
        return this.strings.size() == other.strings.size() && this.strings.containsAll(other.strings);
    }

    public Brick mergeSameSet(Brick other) {
        return new Brick(this.min.sum(other.min), this.max.sum(other.max), this.strings);
    }

    /** For norm rule 5 **/
    public boolean canBeBreak() {
        return this.min.greaterOrEqual(new BrickIndicesValue(1)) && !this.max.equals(this.min);
    }

    public LinkedList<Brick> breakBrick(){
        LinkedList<String> strs1 = this.strings;
        int min = this.min.getValue() - 1;

        Brick b2 = new Brick(new BrickIndicesValue(0), this.max.diff(this.min), this.strings);

        LinkedList<String> strs2 = this.strings;
        LinkedList<String> aux = new LinkedList<>();

        for (; min > 0; min--) {
            for (String s2 : strs2) {
                for (String s1 : strs1) {
                    aux.add(s2.concat(s1));
                }
            }
            strs2 = (LinkedList<String>) aux.clone();
            aux.clear();
        }

        Brick b1 = new Brick(1,1, strs2);

        LinkedList<Brick> bricks = new LinkedList<>();
        bricks.add(b1);
        bricks.add(b2);

        return bricks;
    }

    static private boolean isSubSet (LinkedList<String> l1, LinkedList<String> l2) {
        return l1.size() <= l2.size() && l2.containsAll(l1) ;
    }

    public String getString(int i) {
        return this.strings.get(i);
    }

    public LinkedList<String> getStrings() {
        return strings;
    }

    public BrickIndicesValue getMin() {
        return this.min;
    }

    public BrickIndicesValue getMax() {
        return this.max;
    }
}
