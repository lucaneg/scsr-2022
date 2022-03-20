package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    enum Sign {
        BOTTOM, MINUS, ZERO_MINUS, ZERO, ZERO_PLUS, PLUS, TOP;
    }

    private final Sign sign;

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sign);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        ExtSignDomain other = (ExtSignDomain) obj;
        return this.sign == other.sign;
    }

    @Override
    public ExtSignDomain top() {
        return new ExtSignDomain(Sign.TOP);
    }

    @Override
    public ExtSignDomain bottom() {
        return new ExtSignDomain(Sign.BOTTOM);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(this.sign);
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            switch (this.sign) {
                case MINUS:
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO_MINUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    break;
                case ZERO_PLUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    break;
                case PLUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    break;
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else {
            throw new SemanticException("Some objects are null");
        }
        return this.top();
    }


    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            return this.lubAux(other);
        } else {
            throw new SemanticException("Some objects are null");
        }
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0) return new ExtSignDomain(Sign.PLUS);
            else if (v == 0) return new ExtSignDomain(Sign.ZERO);
            else return new ExtSignDomain(Sign.MINUS);
        }
        return top();
    }


    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            if (this.sign == Sign.PLUS || this.sign == Sign.ZERO || this.sign == Sign.MINUS) {
                return true;
            } else if (this.sign == Sign.ZERO_PLUS || this.sign == Sign.ZERO_MINUS) {
                if (other.sign == Sign.ZERO_MINUS || other.sign == Sign.ZERO_PLUS) {
                    return true;
                }
            }
            return false;
        } else {
            throw new SemanticException("Some objects are null");
        }
    }

    private ExtSignDomain negate() {
        if (this.sign == Sign.MINUS) {
            return new ExtSignDomain(Sign.PLUS);
        } else if (this.sign == Sign.PLUS) {
            return new ExtSignDomain(Sign.MINUS);
        } else if (this.sign == Sign.ZERO_MINUS) {
            return new ExtSignDomain(Sign.ZERO_PLUS);
        } else if (this.sign == Sign.ZERO_PLUS) {
            return new ExtSignDomain(Sign.ZERO_MINUS);
        } else {
            return this;
        }
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation) {
            return arg.negate();
        } else {
            return top();
        }
    }


    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                        case ZERO:
                            return new ExtSignDomain(Sign.MINUS);
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO:
                        case ZERO_PLUS:
                        case PLUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_MINUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case ZERO_MINUS:
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case TOP:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO:
                        case ZERO_PLUS:
                        case PLUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                        case ZERO:
                            return new ExtSignDomain(Sign.PLUS);
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case ZERO_MINUS:
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_MINUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case TOP:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
            }
        } else if (operator instanceof Multiplication) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case PLUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.ZERO);
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                        case PLUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_MINUS:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                        case PLUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case TOP:
                    switch (right.sign) {
                        case MINUS:
                        case ZERO_MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_PLUS:
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
            }
        } else if (operator instanceof DivisionOperator) {
            switch (left.sign) {
                case MINUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.MINUS);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                            return new ExtSignDomain(Sign.PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.ZERO);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.ZERO);
                    }
                case ZERO_PLUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case ZERO_MINUS:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.ZERO_PLUS);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                            return new ExtSignDomain(Sign.ZERO_MINUS);
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
                case TOP:
                    switch (right.sign) {
                        case MINUS:
                            return new ExtSignDomain(Sign.TOP);
                        case ZERO_MINUS:
                        case ZERO:
                        case ZERO_PLUS:
                            return new ExtSignDomain(Sign.BOTTOM);
                        case PLUS:
                        case TOP:
                            return new ExtSignDomain(Sign.TOP);
                    }
            }
        }
        return this.top();
    }


}
