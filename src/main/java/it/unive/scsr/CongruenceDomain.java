package it.unive.scsr;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.symbolic.value.operator.Module;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;

public class CongruenceDomain extends BaseNonRelationalValueDomain<CongruenceDomain> {

    private static final CongruenceDomain TOP = new CongruenceDomain(new AValue(1, 0));
    private static final CongruenceDomain BOTTOM = new CongruenceDomain(new AValue(-1, 0));

    private final AValue avalue;

    public CongruenceDomain() {
        this.avalue = new AValue();
    }

    private CongruenceDomain(AValue avalue) {
        this.avalue = avalue;
    }


    /*
     * The lub is represented by:
     *   (aZ + b) ⊔ (a′Z + b′) = gcd{a, a′, | b − b′ |}Z + min{b, b′}
     */
    @Override
    protected CongruenceDomain lubAux(CongruenceDomain other) throws SemanticException {
        int newA = my_gcd(this.avalue.getA(), my_gcd(other.avalue.getA(), abs(this.avalue.getB() - other.avalue.getB())));
        int newB = min(this.avalue.b, other.avalue.b);
        return computeDomainElement(newA, newB);
    }


    /*
     * The glb is represented by:
     *   (aZ + b) ± (a′Z + b′) = lcm{a, a′}Z + b′′ if b ≡ b′ mod gcd{a, a′}
     *   (aZ + b) ± (a′Z + b′) = ⊥ otherwise
     */
    @Override
    protected CongruenceDomain glbAux(CongruenceDomain other) throws SemanticException {
        if (this.avalue.getA() == 0 || other.avalue.getA() == 0)
            return new CongruenceDomain(new AValue(-1, 0));  // bottom

        if (checkCongruence(this.avalue.getB(), other.avalue.getB(), my_gcd(this.avalue.getA(), other.avalue.getA()))) {
            int newA = my_lcm(this.avalue.getA(), other.avalue.getA());
            int newB = findNewBAux(other, newA);
            return computeDomainElement(newA, newB);
        } else
            return new CongruenceDomain(new AValue(-1, 0));  // bottom
    }


    /*
     * This method finds b''
     */
    private int findNewBAux(CongruenceDomain other, int newA) {
        int newB = 0;

        while (newB < newA || (checkCongruence(newB, this.avalue.getB(), this.avalue.getA()) && checkCongruence(newB, other.avalue.getB(), other.avalue.getA()))) {
            newB++;
        }
        return newB;
    }


    /*
     * Returns a mod n
     */
    private int modulus(int a, int n) {
        return ((a % n) + n) % n;
    }


    /*
     * Yields true if a ≡ b mod n false otherwise
     */
    private boolean checkCongruence(int a, int b, int n) {
        return modulus(a, n) == modulus(b, n);
    }

    @Override
    protected CongruenceDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer)
            return new CongruenceDomain(new AValue(0, (Integer) constant.getValue()));
        else
            return new CongruenceDomain(new AValue(1, 0));
    }


    @Override
    protected CongruenceDomain evalUnaryExpression(UnaryOperator operator, CongruenceDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation) {
            int newA = arg.avalue.a;
            int newB = arg.avalue.b * (-1);
            return computeDomainElement(newA, newB);
        } else
            return arg;
    }

    @Override
    protected CongruenceDomain evalBinaryExpression(BinaryOperator operator, CongruenceDomain left,
                                                    CongruenceDomain right,
                                                    ProgramPoint pp) {

        // (aZ+b)+(a'Z+b') = gcd(a, a′)Z + min(b, b′)
        if (operator instanceof AdditionOperator) {
            int newA = my_gcd(left.avalue.getA(), right.avalue.getA());
            int newB = left.avalue.getB() + right.avalue.getB();
            return computeDomainElement(newA, newB);
        }

        // (aZ+b)-(a'Z+b') = gcd(a, a′)Z - min(b, b′)
        if (operator instanceof SubtractionOperator) {
            int newA = my_gcd(left.avalue.getA(), right.avalue.getA());
            int newB = left.avalue.getB() - right.avalue.getB();
            return computeDomainElement(newA, newB);
        }

        // (aZ + b)(a′Z + b′) = gcd{aa′, ab′, a′b}Z + bb
        if (operator instanceof Multiplication) {
            int newA = my_gcd(my_gcd(left.avalue.a * right.avalue.a, left.avalue.a * right.avalue.b), right.avalue.a * left.avalue.b);
            int newB = left.avalue.b * right.avalue.b;
            return computeDomainElement(newA, newB);
        }

        // (aZ + b)/(a′Z + b′) = ⊥ if a′=0 and b′=0
        //                       (a/|b′|)Z + (b/b′) if a′=0, b′!=0, b′|a, and b′|b
        //                       1Z + 0 otherwise
        if (operator instanceof DivisionOperator) {
            // 3 cases
            if (right.avalue.a == 0 && right.avalue.b == 0)
                return new CongruenceDomain(new AValue(-1, 0));  // It means return bottom

            else if (right.avalue.a == 0 && right.avalue.b != 0 && left.avalue.a % right.avalue.b == 0 && left.avalue.b % right.avalue.b == 0) {
                int newA = left.avalue.a / (abs(right.avalue.b));
                int newB = left.avalue.b / right.avalue.b;
                return computeDomainElement(newA, newB);
            } else
                return new CongruenceDomain(new AValue(1, 0));  // Top
        }

        // (aZ + b)mod(a′Z + b′) = gcd{a,a′,b′}Z + b
        if (operator instanceof Module) {
            int newA = my_gcd(left.avalue.getA(), my_gcd(right.avalue.getA(), right.avalue.getB()));
            int newB = left.avalue.getB();
            return computeDomainElement(newA, newB);
        }
        return top();
    }


    @Override
    protected CongruenceDomain wideningAux(CongruenceDomain other) throws SemanticException {
        return lubAux(other);
    }


    /*
     * (aZ + b) ⊑ (a′Z + b′) ⇐⇒ a′|a and b≡b′ mod a′
     */
    @Override
    protected boolean lessOrEqualAux(CongruenceDomain other) throws SemanticException {
        if (other.avalue.getA() == 0) {
            return false;
        }
        boolean firstCondition = (avalue.a % other.avalue.a == 0);
        boolean secondCondition = checkCongruence(this.avalue.getB(), other.avalue.getB(), other.avalue.getA());
        return firstCondition && secondCondition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CongruenceDomain other = (CongruenceDomain) obj;
        return avalue == other.avalue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((avalue == null) ? 0 : avalue.hashCode());
        return result;
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(avalue);
    }

    @Override
    public CongruenceDomain top() {
        return TOP;
    }

    @Override
    public CongruenceDomain bottom() {
        return BOTTOM;
    }

    private int my_gcd(int a, int b) {
        int abs_a = abs(a);
        int abs_b = abs(b);

        if (b == 0) return abs_a;
        if (a == 0) return abs_b;

        return my_gcd(abs_b, abs_a % abs_b);
    }

    private int my_lcm(int a, int b) {
        if (b == 0 || a == 0) return 0;

        int abs_a = abs(a);
        int abs_b = abs(b);
        int gcd = my_gcd(abs_a, abs_b);

        for (int i = 1; i <= abs_a && i <= abs_b; ++i) {
            if (abs_a % i == 0 && abs_b % i == 0)
                gcd = i;
        }
        return (abs_a * abs_b) / gcd;
    }


    /*
     * Returns the representative of the equivalent class to which element belongs
     */
    private CongruenceDomain equivalenceClassRep(CongruenceDomain element) {
        int newB = ((element.avalue.getB() % element.avalue.getA()) + element.avalue.getA()) % element.avalue.getA();
        return new CongruenceDomain(new AValue(element.avalue.getA(), newB));
    }


    /*
     * Returns element if it is on standard form otherwise computes its representative via equivalenceClassRep
     */
    private CongruenceDomain computeDomainElement(int newA, int newB) {
        CongruenceDomain element = new CongruenceDomain(new AValue(newA, newB));
        if ((element.avalue.getB() >= element.avalue.getA() || element.avalue.getB() < 0) && element.avalue.getA() > 0)
            return equivalenceClassRep(element);
        else
            return element;
    }


    /*
     * An instance of this class represents an abstract value
     */
    private static class AValue {
        public int a;  // Is a natural number N {0,1...}
        public int b;  // Z

        //Constructor
        public AValue() {
            this.a = 1;
            this.b = 0;  // TOP
        }

        public AValue(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AValue other = (AValue) obj;
            return (this.a == other.a) && (this.b == other.b);
        }

        @Override
        public String toString() {
            if (a == -1 && b == 0)
                return "BOTTOM";
            if (b >= 0)
                return a + "Z" + "+" + b;
            else
                return a + "Z" + b;
        }
    }
}
