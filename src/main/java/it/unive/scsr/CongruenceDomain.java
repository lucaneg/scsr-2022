package it.unive.scsr;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.analysis.SemanticException;
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


    private static final CongruenceDomain top = new CongruenceDomain(new AValue(1, 0));
    private static final CongruenceDomain bottom = new CongruenceDomain(new AValue(true));

    private final AValue avalue;

    public CongruenceDomain() {
        this.avalue = new AValue();
    }

    private CongruenceDomain(AValue avalue) {
        this.avalue = avalue;
    }

    @Override
    protected CongruenceDomain lubAux(CongruenceDomain other) throws SemanticException {
        int newA = my_gcd(this.avalue.getA(), my_gcd(other.avalue.getA(), abs(this.avalue.getB() - other.avalue.getB())));
        int newB = min(this.avalue.b, other.avalue.b); //TODO min with abs or not??
        return new CongruenceDomain(new AValue(newA,  newB));
    }


    @Override
    protected CongruenceDomain glbAux(CongruenceDomain other) throws SemanticException {
        return null;
    }

    @Override
    protected CongruenceDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer)
            return new CongruenceDomain(new AValue(0, (Integer) constant.getValue()));
        else
            return new CongruenceDomain(new AValue(1, 0));
    }

    private int my_gcd(int a, int b) {
        if (b == 0) return a;
        if (a == 0) return b;
        int abs_a = abs(a);
        int abs_b = abs(b);
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
        int lcm = (abs_a * abs_b) / gcd;
        return lcm;
    }

    @Override
    protected CongruenceDomain evalUnaryExpression(UnaryOperator operator, CongruenceDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return new CongruenceDomain(new AValue(arg.avalue.a, arg.avalue.b * (-1)));
        else
            return arg;
    }

    @Override
    protected CongruenceDomain evalBinaryExpression(BinaryOperator operator, CongruenceDomain left,
                                                    CongruenceDomain right,
                                                    ProgramPoint pp) {

        if (operator instanceof AdditionOperator) {
            int newA = my_gcd(left.avalue.getA(), right.avalue.getA());
            int newB = left.avalue.getB() + right.avalue.getB();
            return new CongruenceDomain(new AValue(newA, newB)); // (a ∧ a')Z + (b + b')
        }

        if (operator instanceof SubtractionOperator) {
            int newA = my_gcd(left.avalue.getA(), right.avalue.getA());
            int newB = left.avalue.getB() - right.avalue.getB();
            return new CongruenceDomain(new AValue(newA, newB)); // (a ∧ a')Z + (b - b')
        }

        if (operator instanceof Multiplication) {
            int newA = my_gcd(my_gcd(left.avalue.a * right.avalue.a, left.avalue.a * right.avalue.b), right.avalue.a * left.avalue.b);
            int newB = left.avalue.b * right.avalue.b;
            return new CongruenceDomain(new AValue(newA, newB)); //(aa′ ∧ ab′ ∧ a′b)Z + bb'
        }
        if (operator instanceof DivisionOperator) {
            // 3 cases
            if (right.avalue.a == 0 && right.avalue.b == 0)
                return new CongruenceDomain(new AValue(true)); //modify this Sh*t //it means return bottom
            else if (right.avalue.a == 0 && right.avalue.b != 0 && left.avalue.a % right.avalue.b == 0 && left.avalue.b % right.avalue.b == 0)
                return new CongruenceDomain(new AValue(left.avalue.a / (abs(right.avalue.b)), left.avalue.b / right.avalue.b));
            else return new CongruenceDomain(new AValue(1, 0)); //top
        }
        return top();
    }

    @Override
    protected CongruenceDomain wideningAux(CongruenceDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(CongruenceDomain other) throws SemanticException {
        int bs_abs = abs(avalue.b - other.avalue.b);
        if (other.avalue.getA() == 0) {
            return false;
        }
        if ((avalue.a % other.avalue.a == 0) && (bs_abs % other.avalue.a == 0))
            return true;
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
        return top;
    }

    @Override
    public CongruenceDomain bottom() {
        return bottom;
    }

    private static class AValue {

        public int a = -1;  // is a natural number N {0,1...}
        public int b = 0;  // Z

        public boolean aBottom = false;

        public AValue() {
            this.a = 1;
            this.b = 0; /*TOP*/
        }

        public AValue(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public AValue(boolean bot) {
            this.aBottom = bot;
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
            if (!this.aBottom && !other.aBottom)
                return (this.a == other.a) && (this.b == other.b);
            else
                return this.aBottom == other.aBottom;
        }

        @Override
        public String toString() {
            if (aBottom)
                return "BOTTOM";
            if (b >= 0)
                return a + "Z" + "+" + b;
            else
                return a + "Z" + b;
        }

    }
}
