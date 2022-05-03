package it.unive.scsr.project;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BricksDomain extends BaseNonRelationalValueDomain<BricksDomain> {

    private static final int boundL = 10;   //bound the size of the list of bricks

    private LinkedList<Brick> bricks;

    public BricksDomain() {
        this.bricks = new LinkedList<>();
    }

    public BricksDomain(LinkedList<Brick> bricks) {
        this.bricks = (LinkedList<Brick>) bricks.clone();
    }

    public BricksDomain(Brick b) {
        this.bricks = new LinkedList<>();
        this.bricks.add(b);
    }

    @Override
    protected BricksDomain lubAux(BricksDomain other) throws SemanticException {
        BricksDomain l1 = new BricksDomain(this.bricks);
        BricksDomain l2 = new BricksDomain(other.bricks);
        int n;

        if (l1.bricksSize() != l2.bricksSize()) {
            if (l1.bricksSize() < l2.bricksSize())
                l1 = l1.padWith(l2);
            else
                l2 = l1.padWith(l2);
        }

        n = l1.bricksSize();
        BricksDomain out = new BricksDomain();

        for (int i = 0; i < n; i++) {
            out.addBrick(l1.getBrick(i).lubAux(l2.getBrick(i)));
        }

        out = out.normalize();
        return out;
    }

    @Override
    protected BricksDomain glbAux(BricksDomain other) throws SemanticException {
        BricksDomain l1 = new BricksDomain(this.bricks);
        BricksDomain l2 = new BricksDomain(other.bricks);
        int n;

        if (l1.bricksSize() != l2.bricksSize()) {
            if (l1.bricksSize() < l2.bricksSize())
                l1 = l1.padWith(l2);
            else
                l2 = l1.padWith(l2);
        }

        n = l1.bricksSize();
        BricksDomain out = new BricksDomain();

        for (int i = 0; i < n; i++) {
           Brick supp = l1.getBrick(i).glbAux(l2.getBrick(i));
           if (supp.isBottom())
               return bottom();
           else
               out.addBrick(supp);
        }

        return out;
    }

    @Override
    protected BricksDomain wideningAux(BricksDomain other) throws SemanticException {
        if ((!this.lessOrEqualAux(other) && !other.lessOrEqualAux(this))
                || this.bricksSize() > boundL
                || other.bricksSize() > boundL)
            return top();
        else {
            BricksDomain l1 = new BricksDomain(this.bricks);
            BricksDomain l2 = new BricksDomain(other.bricks);
            int n;

            if (l1.bricksSize() != l2.bricksSize()) {
                if (l1.bricksSize() < l2.bricksSize())
                    l1 = l1.padWith(l2);
                else
                    l2 = l1.padWith(l2);
            }

            n = l1.bricksSize();
            BricksDomain out = new BricksDomain();

            for (int i = 0; i < n; i++) {
                out.addBrick(l1.getBrick(i).wideningAux(l2.getBrick(i)));
            }
            return out.normalize();
        }
    }

    @Override
    protected boolean lessOrEqualAux(BricksDomain other) throws SemanticException {
        BricksDomain l1 = new BricksDomain(this.bricks);
        BricksDomain l2 = new BricksDomain(other.bricks);
        int n;

        if (l2.isTop() || l1.isBottom()) return true;

        if (l1.bricksSize() != l2.bricksSize()) {
            if (l1.bricksSize() < l2.bricksSize())
                l1 = l1.padWith(l2);
            else
                l2 = l1.padWith(l2);
        }

        n = l1.bricksSize();
        for (int i = 0; i < n; i++) {
            if (!((l1.getBrick(i)).lessOrEqualAux(l2.getBrick(i))))
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BricksDomain other = (BricksDomain) obj;
        if (this.bricksSize() != other.bricksSize())
            return false;
        int n = this.bricksSize();
        for (int i = 0; i < n; i++) {
            if (!(this.getBrick(i).equals(other.getBrick(i))))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public DomainRepresentation representation() {
        String out = "[";
        int n = bricksSize();
        for (int i = 0; i < n; i++) {
            Brick b = getBrick(i);
            if (i == n-1)
                out = out + (b.representation()).toString() + "]";
            else
                out = out + (b.representation()).toString() + ", ";
        }
        return new StringRepresentation(out);
    }

    @Override
    public BricksDomain top() {
        return new BricksDomain(new Brick().top());
    }

    @Override
    public BricksDomain bottom() {
        return new BricksDomain();
    }

    @Override
    public boolean isTop() {
        return this.bricksSize() == 1 && this.getBrick(0).isTop();
    }

    @Override
    public boolean isBottom() {
        return this.bricksSize() == 0;
    }

    @Override
    protected BricksDomain evalNullConstant(ProgramPoint pp) throws SemanticException {
        return super.evalNullConstant(pp);
    }

    @Override
    protected BricksDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        LinkedList<String> str = new LinkedList<>();
        str.add(constant.toString());
        return new BricksDomain(new Brick(1,1, str));
    }

    //Contains
    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, BricksDomain left, BricksDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringContains) {
            //From the paper, we assume that the parameter to look for is a character
            String c = right.getBrick(0).getString(0);
            Pattern pattern = Pattern.compile("(\\D)");
            Matcher matcher = pattern.matcher(c);
            if (matcher.find())
                c = matcher.group(1);

            //Check if the brick are in the valid form, as specified from paper
            for (Brick b: left.bricks) {
                if (b.getMin().getValue() < 1 || !(b.getMin().lessOrEqual(b.getMax()))) {
                    return SemanticDomain.Satisfiability.UNKNOWN;
                }

                //if is valid and the char c is not contained in all string return "false"
                for (String s: b.getStrings()) {
                    if (!s.contains(c)) {
                        return SemanticDomain.Satisfiability.NOT_SATISFIED;
                    }
                }
            }
            return SemanticDomain.Satisfiability.SATISFIED;
        }
        return super.satisfiesBinaryExpression(operator, left, right, pp);
    }

    // Concat
    @Override
    protected BricksDomain evalBinaryExpression(BinaryOperator operator, BricksDomain left, BricksDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringConcat) {
            BricksDomain out = new BricksDomain();
            out.bricks.addAll(left.bricks);
            out.bricks.addAll(right.bricks);
            return out;
        }
        return super.evalBinaryExpression(operator, left, right, pp);
    }

    // SubString
    @Override
    protected BricksDomain evalTernaryExpression(TernaryOperator operator, BricksDomain left, BricksDomain middle, BricksDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof StringSubstring) {

            // Per problema discusso con tutor, nella strsub bisogna passare stringhe come indici,
            // per questo per estrarre gli indici uso una regexp.
            Pattern pattern = Pattern.compile("\"(\\d)\"");
            Matcher matcherI = pattern.matcher(middle.getBrick(0).getString(0));
            Matcher matcherJ = pattern.matcher(right.getBrick(0).getString(0));
            int i = 0;  //Begin
            int j = 0;  //End

            if (matcherI.find() && matcherJ.find()) {
                i = Integer.parseInt(matcherI.group(1));
                j = Integer.parseInt(matcherJ.group(1));
            }
            BricksDomain aux = left.normalize();

            Brick b = aux.getBrick(0);

            if (b.getMin().equals(1) && b.getMax().equals(1)) {
                for (String s : b.getStrings()) {
                    if (s.length() < j)
                        return top();
                }
                LinkedList<String> t = new LinkedList<>();
                for (String s : b.getStrings()) {
                    t.add(s.substring(i, j));
                }

                return new BricksDomain(new Brick(1, 1, t));
            }
        }

        return top();
    }

    public BricksDomain padWith(BricksDomain other) {
        //L1 and L2 be the two lists of bricks and let L1 be the shortest one
        LinkedList<Brick> l1, l2;
        int emptyBricksAdded;
        if (this.bricks.size() <= other.bricks.size()) {
            l1 = (LinkedList<Brick>) this.bricks.clone();
            l2 = (LinkedList<Brick>) other.bricks.clone();
        } else {
            l1 = (LinkedList<Brick>) other.bricks.clone();
            l2 = (LinkedList<Brick>) this.bricks.clone();
        }

        //Let n1 be the number of bricks of L1, n2 be the number
        //of bricks of L2, and n be their difference (n = n2 - n1).
        int n1 = l1.size();
        int n2 = l2.size();
        int n = n2 - n1;

        //for each brick of the shorter list, we check if the same brick appears in the other list
        //If so, we modify the shorter list by adding empty bricks such that the two equal bricks will appear in
        //the same position in the two lists. If no pair of equal bricks is found, the algorithm works in a way
        //that all n empty bricks are added at the beginning of the shorter list.

        LinkedList<Brick> lNew = new LinkedList<>();
        emptyBricksAdded = 0;

        for (int i = 0; i < n2; i++) {
            if (emptyBricksAdded >= n) {
                lNew.add(l1.removeFirst());
            } else if (l1.isEmpty() || !l1.getFirst().equals(l2.get(i))) {
                lNew.add(new Brick());
                emptyBricksAdded++;
            } else {
                lNew.add(l1.removeFirst());
            }
        }
        return new BricksDomain(lNew);
    }

    public BricksDomain normalize() {
        LinkedList<Brick> list = (LinkedList<Brick>) this.bricks.clone();
        LinkedList<Brick> aux = new LinkedList<>();
        boolean isNormalized = false;

        //Rule 1
        for (Brick b: list) {
            if (!b.isEmptyBrick())
                aux.add(b);
        }
        list = (LinkedList<Brick>) aux.clone();

        while (!isNormalized) {
            int i;

            //Rule 2
            boolean rule2 = false;
            if (list.size() >= 2) {
                i = 0;
                while (i < list.size()) {
                    if (i != list.size() - 1) {
                        if (list.get(i).canBeMergedWith(list.get(i + 1))) {
                            Brick m = list.get(i).merge(list.get(i + 1));
                            list.remove(i + 1);
                            list.remove(i);
                            list.add(i, m);
                            rule2 = true;
                            i--;
                        }
                    }
                    i++;
                }
            }

            //Rule 3
            boolean rule3 = false;
            i = 0;
            while (i < list.size()) {
                Brick supp = list.get(i).transformCostantApplication();
                if (!supp.equals(list.get(i)))
                    rule3 = true;
                list.remove(i);
                list.add(i, supp);
                i++;
            }

            //Rule 4
            boolean rule4 = false;
            if (list.size() >= 2) {
                i = 0;
                while (i < list.size()) {
                    if (i != list.size() - 1) {
                        if (list.get(i).hasSameSet(list.get(i + 1))) {
                            Brick m = list.get(i).mergeSameSet(list.get(i + 1));
                            list.remove(i + 1);
                            list.remove(i);
                            list.add(i, m);
                            rule4 = true;
                            i--;
                        }
                    }
                    i++;
                }
            }

            //Rule 5
            boolean rule5 = false;
            i = 0;
            while (i < list.size()) {
                if (list.get(i).canBeBreak()) {
                    LinkedList<Brick> supp = list.get(i).breakBrick();
                    list.remove(i);
                    list.add(i, supp.get(0));
                    list.add(i + 1, supp.get(1));
                    rule5 = true;
                }
                i++;
            }

            if (!rule2 && !rule3 && !rule4 && !rule5) isNormalized = true;
        }
        return new BricksDomain(list);
    }

    public int bricksSize() {
        return bricks.size();
    }

    public boolean addBrick(Brick b) {
        return bricks.add(b);
    }

    public Brick getBrick(int i) {
        return bricks.get(i);
    }
}
