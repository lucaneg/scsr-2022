package it.unive.scsr.final_project;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ExtSignParityDomain extends BaseNonRelationalValueDomain<ExtSignParityDomain> {

    /*
     * NB: in the assume binary function, we used forget identifier and then put state because the assign function in some cases
     * doesn't update the new domain of the variables
     * */

    // Parity EVEN Combinations
    public static final ExtSignParityDomain TOP_EVEN = new ExtSignParityDomain(ExtSignDomain.TOP, ParityDomain.EVEN);
    public static final ExtSignParityDomain BOTTOM_EVEN = new ExtSignParityDomain(ExtSignDomain.BOTTOM, ParityDomain.EVEN);
    public static final ExtSignParityDomain NEG_OR_ZERO_EVEN = new ExtSignParityDomain(ExtSignDomain.NEG_OR_ZERO, ParityDomain.EVEN);
    public static final ExtSignParityDomain POS_OR_ZERO_EVEN = new ExtSignParityDomain(ExtSignDomain.POS_OR_ZERO, ParityDomain.EVEN);
    public static final ExtSignParityDomain ZERO_EVEN = new ExtSignParityDomain(ExtSignDomain.ZERO, ParityDomain.EVEN);
    public static final ExtSignParityDomain NEG_EVEN = new ExtSignParityDomain(ExtSignDomain.NEG, ParityDomain.EVEN);
    public static final ExtSignParityDomain POS_EVEN = new ExtSignParityDomain(ExtSignDomain.POS, ParityDomain.EVEN);

    // Parity ODD Combinations
    public static final ExtSignParityDomain TOP_ODD = new ExtSignParityDomain(ExtSignDomain.TOP, ParityDomain.ODD);
    public static final ExtSignParityDomain BOTTOM_ODD = new ExtSignParityDomain(ExtSignDomain.BOTTOM, ParityDomain.ODD);
    public static final ExtSignParityDomain NEG_OR_ZERO_ODD = new ExtSignParityDomain(ExtSignDomain.NEG_OR_ZERO, ParityDomain.ODD);
    public static final ExtSignParityDomain POS_OR_ZERO_ODD = new ExtSignParityDomain(ExtSignDomain.POS_OR_ZERO, ParityDomain.ODD);
    public static final ExtSignParityDomain ZERO_ODD = new ExtSignParityDomain(ExtSignDomain.ZERO, ParityDomain.ODD);
    public static final ExtSignParityDomain NEG_ODD = new ExtSignParityDomain(ExtSignDomain.NEG, ParityDomain.ODD);
    public static final ExtSignParityDomain POS_ODD = new ExtSignParityDomain(ExtSignDomain.POS, ParityDomain.ODD);

    // Parity BOTTOM Combinations
    public static final ExtSignParityDomain TOP_BOTTOM = new ExtSignParityDomain(ExtSignDomain.TOP, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain BOTTOM_BOTTOM = new ExtSignParityDomain(ExtSignDomain.BOTTOM, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain NEG_OR_ZERO_BOTTOM = new ExtSignParityDomain(ExtSignDomain.NEG_OR_ZERO, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain POS_OR_ZERO_BOTTOM = new ExtSignParityDomain(ExtSignDomain.POS_OR_ZERO, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain ZERO_BOTTOM = new ExtSignParityDomain(ExtSignDomain.ZERO, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain NEG_BOTTOM = new ExtSignParityDomain(ExtSignDomain.NEG, ParityDomain.BOTTOM);
    public static final ExtSignParityDomain POS_BOTTOM = new ExtSignParityDomain(ExtSignDomain.POS, ParityDomain.BOTTOM);

    // Parity TOP Combinations
    public static final ExtSignParityDomain TOP_TOP = new ExtSignParityDomain(ExtSignDomain.TOP, ParityDomain.TOP);
    public static final ExtSignParityDomain BOTTOM_TOP = new ExtSignParityDomain(ExtSignDomain.BOTTOM, ParityDomain.TOP);
    public static final ExtSignParityDomain NEG_OR_ZERO_TOP = new ExtSignParityDomain(ExtSignDomain.NEG_OR_ZERO, ParityDomain.TOP);
    public static final ExtSignParityDomain POS_OR_ZERO_TOP = new ExtSignParityDomain(ExtSignDomain.POS_OR_ZERO, ParityDomain.TOP);
    public static final ExtSignParityDomain ZERO_TOP = new ExtSignParityDomain(ExtSignDomain.ZERO, ParityDomain.TOP);
    public static final ExtSignParityDomain NEG_TOP = new ExtSignParityDomain(ExtSignDomain.NEG, ParityDomain.TOP);
    public static final ExtSignParityDomain POS_TOP = new ExtSignParityDomain(ExtSignDomain.POS, ParityDomain.TOP);

    private final ExtSignDomain extSignDomain;
    private final ParityDomain parityDomain;

    public ExtSignParityDomain(ExtSignDomain extSignDomain, ParityDomain parityDomain) {
        this.extSignDomain = extSignDomain;
        this.parityDomain = parityDomain;
    }

    public ExtSignParityDomain() {
        this(ExtSignDomain.TOP, ParityDomain.TOP);
    }

    public ExtSignParityDomain(Integer val) {
        this(ExtSignDomain.fromInt(val), ParityDomain.fromInt(val));
    }

    private static ExtSignParityDomain reduceProduct(ExtSignParityDomain currentDomain) {
        /*
           *EXT_SIGN*
               *PARITY*

            * -> to handle
            _ -> this
         */

        ParityDomain p = currentDomain.parityDomain;
        switch (currentDomain.extSignDomain.sign) {
            case BOTTOM:
                /*
                    TOP     -> (BOTTOM, BOTTOM)   *
                    EVEN    -> (BOTTOM, BOTTOM)   *
                    ODD     -> (BOTTOM, BOTTOM)   *
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
                return BOTTOM_BOTTOM;
            case TOP:
                /*
                    TOP     -> (TOP, TOP)
                    EVEN    -> (TOP, EVEN)
                    ODD     -> (TOP, ODD)
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
            case POS:
                /*
                    TOP     -> (PLUS, TOP)
                    EVEN    -> (PLUS, EVEN)
                    ODD     -> (PLUS, ODD)
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
            case NEG:
                /*
                    TOP     -> (MINUS, TOP)
                    EVEN    -> (MINUS, EVEN)
                    ODD     -> (MINUS, ODD)
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
                if (p.equals(ParityDomain.BOTTOM))
                    return BOTTOM_BOTTOM;
                else return currentDomain;
            case ZERO:
                /*
                    TOP     -> (ZERO, EVEN)       *
                    EVEN    -> (ZERO, EVEN)
                    ODD     -> (BOTTOM, BOTTOM)   *
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
                if (p.equals(ParityDomain.ODD))
                    return BOTTOM_BOTTOM;
                else if (p.equals(ParityDomain.TOP))
                    return ZERO_EVEN;
                else if (p.equals(ParityDomain.BOTTOM))
                    return BOTTOM_BOTTOM;
                else return currentDomain;
            case POS_OR_ZERO:
                /*
                    TOP     -> (ZERO_PLUS, TOP)
                    EVEN    -> (ZERO_PLUS, EVEN)
                    ODD     -> (PLUS, ODD)        *
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
                if (p.equals(ParityDomain.ODD))
                    return ExtSignParityDomain.POS_ODD;
                else if (p.equals(ParityDomain.BOTTOM))
                    return BOTTOM_BOTTOM;
                else return currentDomain;
            case NEG_OR_ZERO:
                /*
                    TOP     -> (ZERO_MINUS, TOP)
                    EVEN    -> (ZERO_MINUS, EVEN)
                    ODD     -> (MINUS, ODD)       *
                    BOTTOM  -> (BOTTOM, BOTTOM)   *
                 */
                if (p.equals(ParityDomain.ODD))
                    return ExtSignParityDomain.NEG_ODD;
                else if (p.equals(ParityDomain.BOTTOM))
                    return BOTTOM_BOTTOM;
                else return currentDomain;
        }
        return currentDomain;
    }

    // x = 5
    @Override
    protected ExtSignParityDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        ExtSignDomain extSign = new ExtSignDomain().evalNonNullConstant(constant, pp);
        ParityDomain parity = new ParityDomain().evalNonNullConstant(constant, pp);
        ExtSignParityDomain updatedDomain = new ExtSignParityDomain(extSign, parity);
        return ExtSignParityDomain.reduceProduct(updatedDomain);
    }

    // (-x)
    @Override
    protected ExtSignParityDomain evalUnaryExpression(UnaryOperator operator, ExtSignParityDomain arg, ProgramPoint pp) {
        ExtSignDomain extSign = new ExtSignDomain().evalUnaryExpression(operator, arg.extSignDomain, pp);
        ParityDomain parity = new ParityDomain().evalUnaryExpression(operator, arg.parityDomain, pp);
        ExtSignParityDomain updatedDomain = new ExtSignParityDomain(extSign, parity);
        return ExtSignParityDomain.reduceProduct(updatedDomain);
    }

    // x + y
    @Override
    protected ExtSignParityDomain evalBinaryExpression(BinaryOperator operator, ExtSignParityDomain left, ExtSignParityDomain right, ProgramPoint pp) {
        ExtSignDomain extSign = new ExtSignDomain().evalBinaryExpression(operator, left.extSignDomain, right.extSignDomain, pp);
        ParityDomain parity = new ParityDomain().evalBinaryExpression(operator, left.parityDomain, right.parityDomain, pp);
        ExtSignParityDomain updatedDomain = new ExtSignParityDomain(extSign, parity);
        return ExtSignParityDomain.reduceProduct(updatedDomain);
    }

    private SemanticDomain.Satisfiability eq(ExtSignParityDomain other) {
        if (!parityDomain.equals(other.parityDomain) || !extSignDomain.equals(other.extSignDomain))
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
        else if (extSignDomain.equals(ExtSignDomain.ZERO))
            return SemanticDomain.Satisfiability.SATISFIED;
        else
            return SemanticDomain.Satisfiability.UNKNOWN;
    }

    private SemanticDomain.Satisfiability ne(ExtSignParityDomain other) {
        if (!parityDomain.equals(other.parityDomain) || !extSignDomain.equals(other.extSignDomain))
            return SemanticDomain.Satisfiability.SATISFIED;
        else if (extSignDomain.sign == ExtSignDomain.Sign.ZERO)
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
        else
            /*same symbol and same domain we can't say anything*/
            return SemanticDomain.Satisfiability.UNKNOWN;
    }

    /*we use only extendedsign into the conditions because the parity in this case doesn't change anything. Example (0+,odd)>= (0,even) we see
     * immediatly that all numbers into 0+ (both odd or even) are correct*/
    private SemanticDomain.Satisfiability gt(ExtSignParityDomain other) {
        if (extSignDomain.sign.equals(other.extSignDomain.sign))
            return extSignDomain.sign == ExtSignDomain.Sign.ZERO ? SemanticDomain.Satisfiability.NOT_SATISFIED : SemanticDomain.Satisfiability.UNKNOWN;
        else if (extSignDomain.sign == ExtSignDomain.Sign.NEG) {
            if (other.extSignDomain.sign == ExtSignDomain.Sign.ZERO || other.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO || other.extSignDomain.sign == ExtSignDomain.Sign.POS) return SemanticDomain.Satisfiability.NOT_SATISFIED;
            /* - > 0- we can have true if - > 0 but unknown if - > -3*/
            return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) {
            if (other.extSignDomain.sign == ExtSignDomain.Sign.POS) return SemanticDomain.Satisfiability.NOT_SATISFIED;
            return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (extSignDomain.sign == ExtSignDomain.Sign.ZERO) {
            if (other.extSignDomain.sign == ExtSignDomain.Sign.NEG) return SemanticDomain.Satisfiability.SATISFIED;
                /* 0>0 and 0>0+ nd 0>+ */
            else if (other.extSignDomain.sign == ExtSignDomain.Sign.POS || other.extSignDomain.sign == ExtSignDomain.Sign.ZERO)
                return SemanticDomain.Satisfiability.NOT_SATISFIED;
                /* 0>0- we can have 0 > -1 that is true but we can have 0>0 that is false and same thing for 0+*/
            else return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) {
            /*satisfied only with 0+ > -*/
            if (other.extSignDomain.sign == ExtSignDomain.Sign.NEG) return SemanticDomain.Satisfiability.SATISFIED;
            else return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (extSignDomain.sign == ExtSignDomain.Sign.POS) {
            /*always satisfied but not for + > 0+ that is unknown*/
            if (other.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) return SemanticDomain.Satisfiability.UNKNOWN;
            else return SemanticDomain.Satisfiability.SATISFIED;
        } else
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
    }

    @Override
    public boolean isTop() {
        return extSignDomain.isTop() && parityDomain.isTop();
    }

    @Override
    public boolean isBottom() {
        return extSignDomain.isBottom() && parityDomain.isBottom();
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ExtSignParityDomain left, ExtSignParityDomain right, ProgramPoint pp) throws SemanticException {
        if (left.isTop() || right.isTop())
            return SemanticDomain.Satisfiability.UNKNOWN;

        if (operator == ComparisonEq.INSTANCE) // ==
            return left.eq(right);
        else if (operator == ComparisonNe.INSTANCE) // !=
            return left.ne(right);
        else if (operator == ComparisonGe.INSTANCE) // >=
            return left.eq(right).or(left.gt(right));
        else if (operator == ComparisonGt.INSTANCE) // >
            return left.gt(right);
        else if (operator == ComparisonLe.INSTANCE) // <=
            return left.gt(right).negate(); // e1 <= e2 same as !(e1 > e2)
        else if (operator == ComparisonLt.INSTANCE) // <
            return left.gt(right).negate().and(left.eq(right).negate()); // e1 < e2 -> !(e1 > e2) && !(e1 == e2)
        else
            return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesUnaryExpression(UnaryOperator operator, ExtSignParityDomain arg, ProgramPoint pp) throws SemanticException {
        if (arg.isTop())
            return SemanticDomain.Satisfiability.UNKNOWN;
        if (arg.isBottom())
            return SemanticDomain.Satisfiability.NOT_SATISFIED;

        if (operator == NumericNegation.INSTANCE)
            return SemanticDomain.Satisfiability.SATISFIED;

        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected ValueEnvironment<ExtSignParityDomain> assumeBinaryExpression(ValueEnvironment<ExtSignParityDomain> environment, BinaryOperator operator, ValueExpression left, ValueExpression right, ProgramPoint pp) throws SemanticException {
        if (operator == ComparisonEq.INSTANCE) { // x == c
            if (left instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, extSignParityDomain);
            } else if (right instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, extSignParityDomain);
            }
        } else if (operator == ComparisonNe.INSTANCE) { // x != c

            /*In the != case, we always have top_top. We can understand it better in the following example:
             * If we have the element (+, odd) that represents all positive numbers that are odd: 1,3,5,7,9,11,13,.....
             * So we can see that (+,odd) is different from (+,even),(0-,even) and (0-,odd) and so both the first part and the second one
             * can always change and for this it is always top_top
             * */

            if (left instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;

                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSignParityDomain);
            } else if (right instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;

                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSignParityDomain);
            }
        } else if (operator == ComparisonGe.INSTANCE) {
            if (left instanceof Identifier) { // x >= c
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = POS_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = POS_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;

                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSignParityDomain);
            } else if (right instanceof Identifier) { // c >= x
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = NEG_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = NEG_OR_ZERO_TOP;

                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSignParityDomain);
            }
        } else if (operator == ComparisonLe.INSTANCE) { // x <= c
            if (left instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = NEG_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = NEG_OR_ZERO_TOP;
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSignParityDomain);
            } else if (right instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = POS_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = POS_OR_ZERO_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;

                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSignParityDomain);
            }
        } else if (operator == ComparisonLt.INSTANCE) { // x < c
            if (left instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = NEG_TOP;

                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSignParityDomain);
            } else if (right instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;

                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSignParityDomain);
            }
        } else if (operator == ComparisonGt.INSTANCE) { // x > c
            if (left instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(right, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = POS_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSignParityDomain);
            } else if (right instanceof Identifier) {
                ExtSignParityDomain extSignParityDomain = eval(left, environment, pp);
                ExtSignParityDomain newExtSignParityDomain = new ExtSignParityDomain();

                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.ZERO) newExtSignParityDomain = NEG_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.POS_OR_ZERO) newExtSignParityDomain = TOP_TOP;
                if (extSignParityDomain.extSignDomain.sign == ExtSignDomain.Sign.NEG_OR_ZERO) newExtSignParityDomain = NEG_TOP;

                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSignParityDomain);
            }
        }
        return environment;
    }

    @Override
    protected ExtSignParityDomain lubAux(ExtSignParityDomain other) throws SemanticException {
        ExtSignDomain extSignDomainLub = this.extSignDomain.lubAux(other.extSignDomain);
        ParityDomain parityDomainLub = this.parityDomain.lubAux(other.parityDomain);
        ExtSignParityDomain updatedDomain = new ExtSignParityDomain(extSignDomainLub, parityDomainLub);
        return ExtSignParityDomain.reduceProduct(updatedDomain);
    }

    @Override
    protected ExtSignParityDomain glbAux(ExtSignParityDomain other) throws SemanticException {
        ExtSignDomain extSignDomainLub = this.extSignDomain.glbAux(other.extSignDomain);
        ParityDomain parityDomainLub = this.parityDomain.glbAux(other.parityDomain);
        ExtSignParityDomain updatedDomain = new ExtSignParityDomain(extSignDomainLub, parityDomainLub);
        return ExtSignParityDomain.reduceProduct(updatedDomain);
    }

    @Override
    protected ExtSignParityDomain wideningAux(ExtSignParityDomain other) throws SemanticException {
        return this.lubAux(other);
    }

    // <=
    @Override
    protected boolean lessOrEqualAux(ExtSignParityDomain other) throws SemanticException {
        if (this.parityDomain.equals(other.parityDomain)) // only when parity is same for both
            return this.extSignDomain.lessOrEqual(other.extSignDomain);
        else
            return false; // if parity is different, there are no <= relations (a part from base cases handled by lessOrEqual)
    }

    // (+, EVEN)
    @Override
    public DomainRepresentation representation() {
        String representation = String.format("(%s, %s)", extSignDomain.representation(), parityDomain.representation());
        return new StringRepresentation(representation);
    }

    @Override
    public ExtSignParityDomain top() {
        return TOP_TOP;
    }

    @Override
    public ExtSignParityDomain bottom() {
        return BOTTOM_BOTTOM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtSignParityDomain that = (ExtSignParityDomain) o;
        return Objects.equals(extSignDomain, that.extSignDomain) && Objects.equals(parityDomain, that.parityDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extSignDomain, parityDomain);
    }

}