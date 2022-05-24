package it.unive.scsr.final_project;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Module;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

/*

 ASCII art of the lattice for reference

                 (TOP)
                 /   \
                0-    0+
               / \   / \
              -    0    +
               \   |   /
                 (BOT)

 */

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    public static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
    public static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);
    public static final ExtSignDomain NEG_OR_ZERO = new ExtSignDomain(Sign.NEG_OR_ZERO);
    public static final ExtSignDomain POS_OR_ZERO = new ExtSignDomain(Sign.POS_OR_ZERO);
    public static final ExtSignDomain ZERO = new ExtSignDomain(Sign.ZERO);
    public static final ExtSignDomain NEG = new ExtSignDomain(Sign.NEG);
    public static final ExtSignDomain POS = new ExtSignDomain(Sign.POS);

    public final Sign sign;

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    public static ExtSignDomain fromInt(Integer c) {
        return (c > 0) ? POS : ((c < 0) ? NEG : ZERO);
    }

    enum Sign {

        BOTTOM {
            @Override
            Sign minus() {
                return this;
            }

            @Override
            Sign add(Sign other) {
                return this;
            }

            @Override
            Sign mul(Sign other) {
                return this;
            }

            @Override
            Sign div(Sign other) {
                return this;
            }

            @Override
            public String toString() {
                return Lattice.BOTTOM_STRING;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(bot, bot) = bot
                 * mod(bot, 0) = bot
                 * mod(bot, +) = bot
                 * mod(bot, -) = bot
                 * mod(bot, 0+) = bot
                 * mod(bot, 0-) = bot
                 * mod(bot, top) = bot
                 */
                return BOTTOM;
            }
        },

        TOP {
            @Override
            Sign minus() {
                return this;
            }

            @Override
            Sign add(Sign other) {
                // add(top, bottom) = bottom
                // add(top, top) = top;
                // add(top, +) = top
                // add(top, 0) = top
                // add(top, -) = top
                // add(top, 0+) = top
                // add(top, 0-) = top
                return other == BOTTOM ? other : this;
            }

            @Override
            Sign mul(Sign other) {
                // mul(top, bottom) = bottom
                // mul(top, top) = top;
                // mul(top, +) = top
                // mul(top, 0) = 0
                // mul(top, -) = top
                // mul(top, 0+) = top
                // mul(top, 0-) = top
                return other == BOTTOM ? other : other == ZERO ? ZERO : TOP;
            }

            @Override
            Sign div(Sign other) {
                // div(top, bottom) = bottom
                // div(top, top) = top;
                // div(top, +) = top
                // div(top, 0) = bottom
                // div(top, -) = top
                // div(top, 0+) = top
                // div(top, 0-) = top
                return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
            }

            @Override
            public String toString() {
                return Lattice.TOP_STRING;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(top, bot) = bot
                 * mod(top, 0) = bot
                 * mod(top, +) = top
                 * mod(top, -) = top
                 * mod(top, 0+) = top
                 * mod(top, 0-) = top
                 * mod(top, top) = top
                 */
                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }
                return TOP;
            }
        },

        POS {
            @Override
            Sign minus() {
                return NEG;
            }

            @Override
            Sign add(Sign other) {
                // add(+, bottom) = bottom
                // add(+, top) = top;
                // add(+, +) = +
                // add(+, 0) = +
                // add(+, -) = top
                // add(+, 0+) = +
                // add(+, 0-) = top
                if (other == TOP || other == BOTTOM)
                    return other;
                if (other == POS || other == POS_OR_ZERO || other == ZERO)
                    return this;

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                // mul(+, bottom) = bottom
                // mul(+, top) = top;
                // mul(+, +) = +
                // mul(+, 0) = 0
                // mul(+, -) = -
                // mul(+, 0+) = 0+
                // mul(+, 0-) = 0-
                return other;
            }

            @Override
            Sign div(Sign other) {
                // div(+, bottom) = bottom
                // div(+, top) = top;
                // div(+, +) = +
                // div(+, 0) = bottom
                // div(+, -) = -
                // div(+, 0+) = +
                // div(+, 0-) = -
                if (other == TOP || other == BOTTOM)
                    return other;
                if (other == POS || other == POS_OR_ZERO)
                    return POS;
                if (other == NEG || other == NEG_OR_ZERO)
                    return NEG;
                return BOTTOM;
            }

            @Override
            public String toString() {
                return "+";
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(+, bot) = bot
                 * mod(+, 0) = bot
                 * mod(+, +) = 0+
                 * mod(+, -) = 0+
                 * mod(+, 0+) = 0+
                 * mod(+, 0-) = 0+
                 * mod(+, top) = 0+
                 */
                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }
                return POS_OR_ZERO;
            }
        },

        NEG {
            @Override
            Sign minus() {
                return POS;
            }

            @Override
            Sign add(Sign other) {
                // add(-, bottom) = bottom
                // add(-, top) = top;
                // add(-, +) = top
                // add(-, 0) = -
                // add(-, -) = -
                // add(-, 0+) = top
                // add(-, 0-) = -
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == NEG || other == ZERO || other == NEG_OR_ZERO)
                    return NEG;

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                // mul(-, bottom) = bottom
                // mul(-, top) = top;
                // mul(-, +) = -
                // mul(-, 0) = 0
                // mul(-, -) = +
                // mul(-, 0+) = 0-
                // mul(-, 0-) = 0+
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS)
                    return this;

                if (other == ZERO)
                    return other;

                if (other == NEG)
                    return POS;

                if (other == POS_OR_ZERO)
                    return NEG_OR_ZERO;
                return POS_OR_ZERO;
            }

            @Override
            Sign div(Sign other) {
                // div(-, bottom) = bottom
                // div(-, top) = top;
                // div(-, +) = -
                // div(-, 0) = bottom
                // div(-, -) = +
                // div(-, 0+) = -
                // div(-, 0-) = +
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return NEG;

                if (other == NEG || other == NEG_OR_ZERO)
                    return POS;

                return BOTTOM;
            }

            @Override
            public String toString() {
                return "-";
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(-, bot) = bot
                 * mod(-, 0) = bot
                 * mod(-, +) = 0-
                 * mod(-, -) = 0-
                 * mod(-, 0+) = 0-
                 * mod(-, 0-) = 0-
                 * mod(-, top) = 0-
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return NEG_OR_ZERO;
            }
        },

        ZERO {
            @Override
            Sign minus() {
                return ZERO;
            }

            @Override
            Sign add(Sign other) {
                // add(0, bottom) = bottom
                // add(0, top) = top;
                // add(0, +) = +
                // add(0, 0) = 0
                // add(0, -) = -
                // add(0, 0+) = 0+
                // add(0, 0-) = 0-
                return other;
            }

            @Override
            Sign mul(Sign other) {
                // mul(0, bottom) = bottom
                // mul(0, top) = 0;
                // mul(0, +) = 0
                // mul(0, 0) = 0
                // mul(0, -) = 0
                // mul(0, 0+) = 0
                // mul(0, 0-) = 0
                return other == BOTTOM ? other : ZERO;
            }

            @Override
            Sign div(Sign other) {
                // div(0, bottom) = bottom
                // div(0, top) = 0;
                // div(0, +) = 0
                // div(0, 0) = bottom
                // div(0, -) = 0
                // div(0, 0+) = 0
                // div(0, 0-) = 0
                return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
            }

            @Override
            public String toString() {
                return "0";
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0, bot) = bot
                 * mod(0, 0) = bot
                 * mod(0, +) = 0
                 * mod(0, -) = 0
                 * mod(0, 0+) = 0
                 * mod(0, 0-) = 0
                 * mod(0, top) = 0
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO;
            }
        },

        POS_OR_ZERO {
            @Override
            Sign minus() {
                return NEG_OR_ZERO;
            }

            @Override
            Sign add(Sign other) {
                // add(0+, bottom) = bottom
                // add(0+, top) = top;
                // add(0+, +) = +
                // add(0+, 0) = 0+
                // add(0+, -) = top
                // add(0+, 0+) = 0+
                // add(0+, 0-) = top
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return other;

                if (other == ZERO)
                    return POS_OR_ZERO;

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                // mul(0+, bottom) = bottom
                // mul(0+, top) = top;
                // mul(0+, +) = 0+
                // mul(0+, 0) = 0
                // mul(0+, -) = 0-
                // mul(0+, 0+) = 0+
                // mul(0+, 0-) = 0-
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return POS_OR_ZERO;

                if (other == NEG || other == NEG_OR_ZERO)
                    return NEG_OR_ZERO;

                return ZERO;
            }

            @Override
            Sign div(Sign other) {
                // div(0+, bottom) = bottom
                // div(0+, top) = top;
                // div(0+, +) = 0+
                // div(0+, 0) = bottom
                // div(0+, -) = 0-
                // div(0+, 0+) = 0+
                // div(0+, 0-) = 0-
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return POS_OR_ZERO;

                if (other == NEG || other == NEG_OR_ZERO)
                    return NEG_OR_ZERO;

                return BOTTOM;
            }

            @Override
            public String toString() {
                return "0+";
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0+, bot) = bot
                 * mod(0+, 0) = bot
                 * mod(0+, +) = 0+
                 * mod(0+, -) = 0+
                 * mod(0+, 0+) = 0+
                 * mod(0+, 0-) = 0+
                 * mod(0+, top) = 0+
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return POS_OR_ZERO;
            }
        },

        NEG_OR_ZERO {
            @Override
            Sign minus() {
                return POS_OR_ZERO;
            }

            @Override
            Sign add(Sign other) {
                // add(0-, bottom) = bottom
                // add(0-, top) = top;
                // add(0-, +) = top
                // add(0-, 0) = 0-
                // add(0-, -) = -
                // add(0-, 0+) = top
                // add(0-, 0-) = 0-
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == ZERO || other == NEG_OR_ZERO)
                    return NEG_OR_ZERO;

                if (other == NEG)
                    return other;

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                // mul(0-, bottom) = bottom
                // mul(0-, top) = top;
                // mul(0-, +) = 0-
                // mul(0-, 0) = 0
                // mul(0-, -) = 0+
                // mul(0-, 0+) = 0-
                // mul(0-, 0-) = 0+
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return NEG_OR_ZERO;

                if (other == ZERO)
                    return other;

                return POS_OR_ZERO;
            }

            @Override
            Sign div(Sign other) {
                // div(0-, bottom) = bottom
                // div(0-, top) = top;
                // div(0-, +) = 0-
                // div(0-, 0) = bottom
                // div(0-, -) = 0+
                // div(0-, 0+) = 0-
                // div(0-, 0-) = 0+
                if (other == TOP || other == BOTTOM)
                    return other;

                if (other == POS || other == POS_OR_ZERO)
                    return NEG_OR_ZERO;

                if (other == ZERO)
                    return BOTTOM;
                return POS_OR_ZERO;
            }

            @Override
            public String toString() {
                return "0-";
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0-, bot) = bot
                 * mod(0-, 0) = bot
                 * mod(0-, +) = 0-
                 * mod(0-, -) = 0-
                 * mod(0-, 0+) = 0-
                 * mod(0-, 0-) = 0-
                 * mod(0-, top) = 0-
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return NEG_OR_ZERO;
            }

        };

        abstract Sign minus();

        abstract Sign add(Sign other);

        abstract Sign mul(Sign other);

        abstract Sign div(Sign other);

        abstract Sign mod(Sign other);

        @Override
        public abstract String toString();
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
    public boolean isTop() {
        return this.sign == Sign.TOP;
    }

    @Override
    public boolean isBottom() {
        return this.sign == Sign.BOTTOM;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            int c = (int) constant.getValue();
            if (c == 0)
                return new ExtSignDomain(Sign.ZERO);
            else if (c > 0)
                return new ExtSignDomain(Sign.POS);
            else
                return new ExtSignDomain(Sign.NEG);
        }
        return top();
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg,
                                             ProgramPoint pp) {
        if (operator instanceof NumericNegation)
            return new ExtSignDomain(arg.sign.minus());
        return top();
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left,
                                              ExtSignDomain right,
                                              ProgramPoint pp) {
        if (operator instanceof AdditionOperator)
            return new ExtSignDomain(left.sign.add(right.sign));
        if (operator instanceof DivisionOperator)
            return new ExtSignDomain(left.sign.div(right.sign));
        if (operator instanceof Multiplication)
            return new ExtSignDomain(left.sign.mul(right.sign));
        if (operator instanceof SubtractionOperator)
            return new ExtSignDomain(left.sign.add(right.sign.minus()));
        if (operator instanceof Module)
            return new ExtSignDomain(left.sign.mod(right.sign));
        return top();
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (lessOrEqual(other))
            return other;
        if (other.lessOrEqual(this))
            return this;

        if (sign == Sign.ZERO) {
            if (other.sign == Sign.POS)
                return POS_OR_ZERO;
            else if (other.sign == Sign.NEG)
                return NEG_OR_ZERO;
        }

        if (other.sign == Sign.ZERO) {
            if (sign == Sign.POS)
                return POS_OR_ZERO;
            else if (sign == Sign.NEG)
                return NEG_OR_ZERO;
        }

        return top();
    }

    @Override
    protected ExtSignDomain glbAux(ExtSignDomain other) throws SemanticException {
        if (lessOrEqual(other))
            return this;
        if (other.lessOrEqual(this))
            return other;

        if ((sign == Sign.POS_OR_ZERO && other.sign == Sign.NEG_OR_ZERO) || (sign == Sign.NEG_OR_ZERO && other.sign == Sign.POS_OR_ZERO))
            return ZERO;

        /*we can see that to do better we can return simply bottom but to make the function more clear we also write this conditions*/
        if (sign == Sign.ZERO || sign == Sign.POS || sign == Sign.NEG)
            return BOTTOM;
        if (other.sign == Sign.ZERO || other.sign == Sign.POS || other.sign == Sign.NEG)
            return BOTTOM;

        return bottom();
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) {
        switch (sign) {
            case NEG:
            case POS:
            case ZERO:
                return true;
            case NEG_OR_ZERO:
            case POS_OR_ZERO:
                if (other.sign == Sign.NEG_OR_ZERO || other.sign == Sign.POS_OR_ZERO)
                    return true;
            default:
                return false;
        }
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
        if (arg.isTop())
            return SemanticDomain.Satisfiability.UNKNOWN;
        if (arg.isBottom())
            return SemanticDomain.Satisfiability.NOT_SATISFIED;

        if (operator == NumericNegation.INSTANCE)
            return SemanticDomain.Satisfiability.SATISFIED;

        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    private SemanticDomain.Satisfiability eq(ExtSignDomain other) {
        if (sign != other.sign)
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
            /*only the case zero we know that 0 = 0 , in fact + = + is unknown because we can have 8 = 6 that is false but  8 = 8 is true*/
        else if (sign == Sign.ZERO)
            return SemanticDomain.Satisfiability.SATISFIED;
        else
            return SemanticDomain.Satisfiability.UNKNOWN;
    }

    private SemanticDomain.Satisfiability ne(ExtSignDomain other) {
        if (sign != other.sign)
            return SemanticDomain.Satisfiability.SATISFIED;
        else if (sign == Sign.ZERO)
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
        else
            /*same symbol we can't say anything*/
            return SemanticDomain.Satisfiability.UNKNOWN;
    }

    private SemanticDomain.Satisfiability gt(ExtSignDomain other) {
        if (sign.equals(other.sign))
            return sign == Sign.ZERO ? SemanticDomain.Satisfiability.NOT_SATISFIED : SemanticDomain.Satisfiability.UNKNOWN;
        else if (sign == Sign.NEG) {
            if (other.sign == Sign.ZERO || other.sign == Sign.POS_OR_ZERO || other.sign == Sign.POS) return SemanticDomain.Satisfiability.NOT_SATISFIED;
            /* - > 0- we can have true if - > 0 but unknown if - > -3*/
            return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (sign == Sign.NEG_OR_ZERO) {
            if (other.sign == Sign.POS) return SemanticDomain.Satisfiability.NOT_SATISFIED;
            return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (sign == Sign.ZERO) {
            if (other.sign == Sign.NEG) return SemanticDomain.Satisfiability.SATISFIED;
                /* 0>0 and 0>0+ nd 0>+ */
            else if (other.sign == Sign.POS || other.sign == Sign.ZERO) return SemanticDomain.Satisfiability.NOT_SATISFIED;
                /* 0>0- we can have 0 > -1 that is true but we can have 0>0 that is false and same thing for 0+*/
            else return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (sign == Sign.POS_OR_ZERO) {
            /*satisfied only with 0+ > -*/
            if (other.sign == Sign.NEG) return SemanticDomain.Satisfiability.SATISFIED;
            else return SemanticDomain.Satisfiability.UNKNOWN;
        } else if (sign == Sign.POS) {
            /*always satisfied but not for + > 0+ that is unknown*/
            if (other.sign == Sign.POS_OR_ZERO) return SemanticDomain.Satisfiability.UNKNOWN;
            else return SemanticDomain.Satisfiability.SATISFIED;
        } else
            return SemanticDomain.Satisfiability.NOT_SATISFIED;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
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
    protected ValueEnvironment<ExtSignDomain> assumeBinaryExpression(ValueEnvironment<ExtSignDomain> environment, BinaryOperator operator, ValueExpression left, ValueExpression right, ProgramPoint pp) throws SemanticException {
        if (operator == ComparisonEq.INSTANCE) { // x == c
            if (left instanceof Identifier) {
                ExtSignDomain extSignDomain = eval(right, environment, pp);
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, extSignDomain);
            } else if (right instanceof Identifier) {
                ExtSignDomain extSignDomain = eval(left, environment, pp);
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, extSignDomain);
            }
        } else if (operator == ComparisonNe.INSTANCE) { // x != c
            if (left instanceof Identifier) {
                ExtSignDomain extSign = eval(right, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP; //  - != + true but also - != - true because -7 != -9
                if (extSign.sign == Sign.POS) newExtSign = TOP;
                if (extSign.sign == Sign.ZERO) newExtSign = TOP;
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP;
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP;
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSign);
            } else if (right instanceof Identifier) {
                ExtSignDomain extSign = eval(left, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP;
                if (extSign.sign == Sign.POS) newExtSign = TOP;
                if (extSign.sign == Sign.ZERO) newExtSign = TOP;
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP;
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP;
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSign);
            }
        } else if (operator == ComparisonGe.INSTANCE) {
            if (left instanceof Identifier) { // x >= c
                ExtSignDomain extSign = eval(right, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP; // x >= - we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.POS) newExtSign = POS; // x >= +
                if (extSign.sign == Sign.ZERO) newExtSign = POS_OR_ZERO; // x >= 0
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = POS_OR_ZERO; // x >= 0+
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP; // x >= 0- we know that x can be -, 0, 0-, 0+ and +
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSign);
            } else if (right instanceof Identifier) { // c >= x
                ExtSignDomain extSign = eval(left, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = NEG; // - >= x
                if (extSign.sign == Sign.POS) newExtSign = TOP; // + >= x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.ZERO) newExtSign = NEG_OR_ZERO; // 0 >= x
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP; // 0+ >= x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = NEG_OR_ZERO; // 0- >= x
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSign);
            }
        } else if (operator == ComparisonLe.INSTANCE) { // x <= c
            if (left instanceof Identifier) {
                ExtSignDomain extSign = eval(right, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = NEG; // x <= -
                if (extSign.sign == Sign.POS) newExtSign = TOP; // x <= + we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.ZERO) newExtSign = NEG_OR_ZERO; // x <= 0
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP; // x <= 0+ we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = NEG_OR_ZERO; // x <= 0-
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSign);
            } else if (right instanceof Identifier) {
                ExtSignDomain extSign = eval(left, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP; // - <= x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.POS) newExtSign = POS; // + <= x
                if (extSign.sign == Sign.ZERO) newExtSign = POS_OR_ZERO; // 0 <= x
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = POS_OR_ZERO; // 0+ <= x
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP; // 0- <= x we know that x can be -, 0, 0-, 0+ and +
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSign);
            }
        } else if (operator == ComparisonLt.INSTANCE) { // x < c
            if (left instanceof Identifier) {
                ExtSignDomain extSign = eval(right, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = NEG; // x < -
                if (extSign.sign == Sign.POS) newExtSign = TOP; // x < + we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.ZERO) newExtSign = NEG; // x < 0
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP; // x < 0+ we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = NEG; // x < 0-
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSign);
            } else if (right instanceof Identifier) {
                ExtSignDomain extSign = eval(left, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP; // - < x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.POS) newExtSign = POS; // + < x
                if (extSign.sign == Sign.ZERO) newExtSign = POS; // 0 < x
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = POS; // 0+ < x
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP; // 0- < x we know that x can be -, 0, 0-, 0+ and +
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSign);
            }
        } else if (operator == ComparisonGt.INSTANCE) { // x > c
            if (left instanceof Identifier) {
                ExtSignDomain extSign = eval(right, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = TOP; // x > - we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.POS) newExtSign = POS; // x > +
                if (extSign.sign == Sign.ZERO) newExtSign = POS; // x > 0
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = POS; // x > 0+
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = TOP; // x > 0- we know that x can be -, 0, 0-, 0+ and +
                environment = environment.forgetIdentifier((Identifier) left).putState((Identifier) left, newExtSign);
            } else if (right instanceof Identifier) {
                ExtSignDomain extSign = eval(left, environment, pp);
                ExtSignDomain newExtSign = new ExtSignDomain();
                if (extSign.sign == Sign.NEG) newExtSign = NEG; // - > x
                if (extSign.sign == Sign.POS) newExtSign = TOP; // + > x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.ZERO) newExtSign = NEG; // 0 > x
                if (extSign.sign == Sign.POS_OR_ZERO) newExtSign = TOP; // 0+ > x we know that x can be -, 0, 0-, 0+ and +
                if (extSign.sign == Sign.NEG_OR_ZERO) newExtSign = NEG; // 0- > x
                environment = environment.forgetIdentifier((Identifier) right).putState((Identifier) right, newExtSign);
            }
        }
        return environment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtSignDomain that = (ExtSignDomain) o;
        return sign == that.sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(sign);
    }

}