package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.NonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Sign;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);
    private static final ExtSignDomain PLUS = new ExtSignDomain(Sign.PLUS);
    private static final ExtSignDomain MINUS = new ExtSignDomain(Sign.MINUS);
    private static final ExtSignDomain ZEROMINUS = new ExtSignDomain(Sign.ZEROMINUS);
    private static final ExtSignDomain ZEROPLUS = new ExtSignDomain(Sign.ZEROPLUS);

    enum Sign {
        BOTTOM, MINUS, ZERO, ZEROPLUS, ZEROMINUS, PLUS, TOP;
    }

    private final ExtSignDomain.Sign sign;

    public ExtSignDomain() {
        this(ExtSignDomain.Sign.TOP);
    }

    public ExtSignDomain(ExtSignDomain.Sign sign) {
        this.sign = sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtSignDomain other = (ExtSignDomain) obj;
        return sign == other.sign;
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if((this.sign == Sign.PLUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.PLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.MINUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.MINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        if((this.sign == Sign.ZEROPLUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.ZEROPLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.ZEROPLUS &&  other.sign == Sign.PLUS) || (this.sign == Sign.PLUS &&  other.sign == Sign.ZEROPLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.ZEROMINUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.ZEROMINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        if((this.sign == Sign.ZEROMINUS &&  other.sign == Sign.MINUS) || (this.sign == Sign.MINUS &&  other.sign == Sign.ZEROMINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        return TOP;

    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        if((this.sign == Sign.PLUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.PLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.MINUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.MINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        if((this.sign == Sign.ZEROPLUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.ZEROPLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.ZEROPLUS &&  other.sign == Sign.PLUS) || (this.sign == Sign.PLUS &&  other.sign == Sign.ZEROPLUS))
            return new ExtSignDomain(Sign.ZEROPLUS);

        if((this.sign == Sign.ZEROMINUS &&  other.sign == Sign.ZERO) || (this.sign == Sign.ZERO &&  other.sign == Sign.ZEROMINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        if((this.sign == Sign.ZEROMINUS &&  other.sign == Sign.MINUS) || (this.sign == Sign.MINUS &&  other.sign == Sign.ZEROMINUS))
            return new ExtSignDomain(Sign.ZEROMINUS);

        return TOP;
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if((this.sign == Sign.MINUS && other.sign == Sign.ZEROMINUS) || (this.sign == Sign.ZERO && other.sign == Sign.ZEROMINUS) || (this.sign == Sign.ZERO && other.sign == Sign.ZEROPLUS) || (this.sign == Sign.PLUS && other.sign == Sign.ZEROPLUS)){
            return  true;
        }
        else {
            return false;
        }
    }


    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(Sign.ZEROPLUS);
            else if (v == 0)
                return new ExtSignDomain(Sign.ZERO);
            else
                return new ExtSignDomain(Sign.ZEROMINUS);
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (sign == Sign.MINUS)
            return new ExtSignDomain(Sign.PLUS);
        else if (sign == Sign.PLUS)
            return new ExtSignDomain(Sign.MINUS);
        else if (sign == Sign.ZEROMINUS)
            return new ExtSignDomain(Sign. ZEROPLUS);
        else if (sign == Sign.ZEROPLUS)
            return new ExtSignDomain(Sign.ZEROMINUS);

        return this;
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();

        return top();
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp)
            throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case ZEROMINUS:
                        case MINUS:
                            return left;
                        case PLUS:
                        case ZEROPLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case PLUS:
                        case ZERO:
                        case ZEROPLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZERO:
                    return right;
                case ZEROMINUS:
                    switch (right.sign){
                        case ZEROMINUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return right;
                        case TOP:
                        case ZEROPLUS:
                        case PLUS:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign){
                        case ZEROPLUS:
                        case ZERO:
                            return left;
                        case PLUS:
                            return right;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case ZERO:
                        case PLUS:
                        case ZEROPLUS:
                            return left;
                        case MINUS:
                        case TOP:
                        case ZEROMINUS:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO:
                        case ZEROMINUS:
                            return left;
                        case PLUS:
                        case TOP:
                        case ZEROPLUS:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                            return PLUS;
                        case ZERO:
                            return left;
                        case ZEROMINUS:
                            return ZEROPLUS;
                        case PLUS:
                            return MINUS;
                        case ZEROPLUS:
                            return ZEROMINUS;
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROMINUS:
                    switch (right.sign) {

                        case ZERO:
                            return left;
                        case PLUS:
                            return MINUS;
                        case ZEROPLUS:
                            return ZEROMINUS;
                        case ZEROMINUS:
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign) {
                        case ZEROMINUS:
                        case ZERO:
                            return left;
                        case MINUS:
                            return PLUS;
                        case ZEROPLUS:
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                default:
                    return TOP;
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return TOP;
                case ZEROMINUS:
                    switch (right.sign) {
                        case ZEROMINUS:
                            return ZEROPLUS;
                        case ZERO:
                            return right;
                        case MINUS:
                            return ZEROPLUS;
                        case ZEROPLUS:
                            return ZEROMINUS;
                        case PLUS:
                            return ZEROMINUS;
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROPLUS:
                    switch (right.sign) {
                        case ZEROMINUS:
                            return ZEROMINUS;
                        case ZERO:
                            return right;
                        case MINUS:
                            return ZEROMINUS;
                        case ZEROPLUS:
                            return ZEROPLUS;
                        case PLUS:
                            return ZEROPLUS;
                        case TOP:
                        default:
                            return TOP;
                    }
            }
        } else if (operator instanceof DivisionOperator) {
            if (right.sign == ExtSignDomain.Sign.ZERO || right.sign == ExtSignDomain.Sign.ZEROPLUS || right.sign == ExtSignDomain.Sign.ZEROMINUS )
                return BOTTOM;

            switch (left.sign) {
                case MINUS:
                    return right.negate();
                case PLUS:
                    return right;
                case TOP:
                    return TOP;
                case ZERO:
                    return new ExtSignDomain(ExtSignDomain.Sign.ZERO);
                case ZEROPLUS:
                    switch (right.sign) {
                        case MINUS:
                            return ZEROMINUS;
                        case PLUS:
                            return ZEROPLUS;
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZEROMINUS:
                    switch (right.sign) {
                        case MINUS:
                            return ZEROPLUS;
                        case PLUS:
                            return ZEROMINUS;
                        case TOP:
                        default: return TOP;
                    }
                default:
                    return TOP;
            }
        }
        return top();
    }



    // IMPLEMENTATION NOTE:
    // the code below is outside of the scope of the course. You can uncomment it to get
    // your code to compile. Beware that the code is written expecting that a field named
    // "sign" containing an enumeration (similar to the one saw during class) exists in
    // this class: if you name it differently, change also the code below to make it work
    // by just using the name of your choice instead of "sign"
    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(sign);
    }
}