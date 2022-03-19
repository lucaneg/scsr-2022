package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
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

    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);

    enum ExtSign {
        BOTTOM, MINUS, ZERO, PLUS, ZERO_MINUS, ZERO_PLUS, TOP
    }

    private final ExtSign extSign;

    public ExtSignDomain() {
        this(ExtSign.TOP);
    }

    public ExtSignDomain(ExtSign extSign) {
        this.extSign = extSign;
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
        /* this first condition is added to check if the ExtSign field of this and other are not null
         * we do not need to check nullity for this and other (because the nullity check has already been handled).
         * furthermore, this and other are neither, top nor bottom, nor the same object, so we can safely
         * spare some cases.
         */
        if (this.extSign == null || other.extSign == null)
            throw new SemanticException("Called object or other object contains null fields.");
        switch (this.extSign) {
            case MINUS:
                if (other.extSign == ExtSign.ZERO_MINUS) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO_PLUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.PLUS) return new ExtSignDomain(ExtSign.TOP);
            case ZERO:
                if (other.extSign == ExtSign.MINUS) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO_MINUS) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO_PLUS) return new ExtSignDomain(ExtSign.ZERO_PLUS);
                if (other.extSign == ExtSign.PLUS) return new ExtSignDomain(ExtSign.ZERO_PLUS);
            case PLUS:
                if (other.extSign == ExtSign.MINUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.ZERO_MINUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.ZERO) return new ExtSignDomain(ExtSign.ZERO_PLUS);
                if (other.extSign == ExtSign.ZERO_PLUS) return new ExtSignDomain(ExtSign.ZERO_PLUS);
            case ZERO_MINUS:
                if (other.extSign == ExtSign.MINUS) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO) return new ExtSignDomain(ExtSign.ZERO_MINUS);
                if (other.extSign == ExtSign.ZERO_PLUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.PLUS) return new ExtSignDomain(ExtSign.TOP);
            case ZERO_PLUS:
                if (other.extSign == ExtSign.MINUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.ZERO_MINUS) return new ExtSignDomain(ExtSign.TOP);
                if (other.extSign == ExtSign.ZERO) return new ExtSignDomain(ExtSign.ZERO_PLUS);
                if (other.extSign == ExtSign.PLUS) return new ExtSignDomain(ExtSign.ZERO_PLUS);
        }
        return top();
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        if (this.extSign == null || other.extSign == null)
            throw new SemanticException("Called object or other object contains null fields.");
        return this.lubAux(other);
    }

    /* since Top and Bottom cases are already handled then, MINUS, ZERO, PLUS are in relation with all the other
     * elements of the lattice (including themselves).
     * ZERO_MINUS is in relation with itself (case already handled), and ZERO_PLUS
     * ZERO_PLUS is in relation with itself (case already handled), and ZERO_MINUS
     * check the ascii design of the lattice at the comment on top of this class for reference.
     */
    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this.extSign == null || other.extSign == null)
            throw new SemanticException("Called object or other object contains null fields.");
        if (this.extSign == ExtSign.MINUS || this.extSign == ExtSign.ZERO || this.extSign == ExtSign.PLUS)
            return true;
        else if (this.extSign == ExtSign.ZERO_MINUS)
            return (other.extSign == ExtSign.ZERO_PLUS);
        else if (this.extSign == ExtSign.ZERO_PLUS)
            return (other.extSign == ExtSign.ZERO_MINUS);
        return false;
    }

    // same implementation as the simple Sign Domain (+,0,-)
    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0) return new ExtSignDomain(ExtSign.PLUS);
            else if (v == 0) return new ExtSignDomain(ExtSign.ZERO);
            else return new ExtSignDomain(ExtSign.MINUS);
        }
        return top();
    }

    // handles "operator arg" expressions. only the negation operator supported (!)
    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        return isNegationOperator(operator) ? arg.negate() : top();
    }

    // handles "left operator right" expressions. four arithmetic operators supported: (+, -, *, /)
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (isAdditionOperator(operator))
            return add(left, right);
        else if (isSubtractionOperator(operator))
            return subtract(left, right);
        else if (isMultiplicationOperator(operator))
            return multiply(left, right);
        else if (isDivisionOperator(operator))
            return divide(left, right);
        else
            return this.top();
    }

    private ExtSignDomain add(ExtSignDomain left, ExtSignDomain right) {
        switch (left.extSign) {
            case MINUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case ZERO:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case PLUS:
                    case ZERO_PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO: // 0 + X is always X
                return new ExtSignDomain(right.extSign);
            case PLUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                    case ZERO:
                    case PLUS:
                    case ZERO_PLUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                }
            case ZERO_MINUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case ZERO_MINUS:
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case ZERO_PLUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO_PLUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                    case ZERO:
                    case ZERO_PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                }
            case TOP: // TOP + X is always TOP
                return new ExtSignDomain(ExtSign.TOP);
        }
        return this.top();
    }

    private ExtSignDomain subtract(ExtSignDomain left, ExtSignDomain right) {
        switch (left.extSign) {
            case MINUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                    case ZERO:
                    case ZERO_PLUS:
                    case PLUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                }
            case ZERO:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case ZERO_MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO);
                    case ZERO_PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case PLUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case ZERO:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case ZERO_PLUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO_MINUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                    case ZERO:
                    case ZERO_PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                }
            case ZERO_PLUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case ZERO_MINUS:
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case ZERO_PLUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case TOP: // TOP - X is always TOP
                return new ExtSignDomain(ExtSign.TOP);
        }
        return this.top();
    }

    private ExtSignDomain multiply(ExtSignDomain left, ExtSignDomain right) {
        switch (left.extSign) {
            case MINUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case ZERO_MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO);
                    case ZERO_PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO: // 0 * X is always 0
                return new ExtSignDomain(ExtSign.ZERO);
            case PLUS: // (+) * X is always X
                return new ExtSignDomain(right.extSign);
            case ZERO_MINUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO);
                    case ZERO_PLUS:
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO_PLUS:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO);
                    case ZERO_PLUS:
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case TOP:
                switch (right.extSign) {
                    case MINUS:
                    case ZERO_MINUS:
                    case ZERO_PLUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                    case ZERO:
                        return new ExtSignDomain(ExtSign.ZERO);
                }
        }
        return this.top();
    }

    private ExtSignDomain divide(ExtSignDomain left, ExtSignDomain right) {
        if (right.extSign == ExtSign.ZERO || right.extSign == ExtSign.ZERO_MINUS || right.extSign == ExtSign.ZERO_PLUS)
            return bottom();

        switch (left.extSign) {
            case MINUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO:
                switch (right.extSign) {
                    case MINUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.ZERO);
                }
            case PLUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.MINUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.PLUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO_MINUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case ZERO_PLUS:
                switch (right.extSign) {
                    case MINUS:
                        return new ExtSignDomain(ExtSign.ZERO_MINUS);
                    case PLUS:
                        return new ExtSignDomain(ExtSign.ZERO_PLUS);
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
            case TOP:
                switch (right.extSign) {
                    case MINUS:
                    case PLUS:
                    case TOP:
                        return new ExtSignDomain(ExtSign.TOP);
                }
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (this.extSign == ExtSign.MINUS)
            return new ExtSignDomain(ExtSign.PLUS);
        else if (this.extSign == ExtSign.PLUS)
            return new ExtSignDomain(ExtSign.MINUS);
        else if (this.extSign == ExtSign.ZERO_MINUS)
            return new ExtSignDomain(ExtSign.ZERO_PLUS);
        else if (this.extSign == ExtSign.ZERO_PLUS)
            return new ExtSignDomain(ExtSign.ZERO_MINUS);
        else  // ZERO, BOTTOM, TOP case
            return this;
    }

    /* wrappers for operator type checks */
    private boolean isNegationOperator(UnaryOperator operator) { return (operator instanceof NumericNegation); }
    private boolean isAdditionOperator(BinaryOperator operator) { return (operator instanceof AdditionOperator); }
    private boolean isSubtractionOperator(BinaryOperator operator) { return (operator instanceof SubtractionOperator); }
    private boolean isMultiplicationOperator(BinaryOperator operator) { return (operator instanceof Multiplication); }
    private boolean isDivisionOperator(BinaryOperator operator) { return (operator instanceof DivisionOperator); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtSignDomain that = (ExtSignDomain) o;
        return extSign == that.extSign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extSign);
    }

    // already given implementation in order to compile the code
    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(extSign);
    }

}
