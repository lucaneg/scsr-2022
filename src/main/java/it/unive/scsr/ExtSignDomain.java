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

import static it.unive.scsr.ExtSignDomain.ExtSign.*;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);

    enum ExtSign {
        TOP, BOTTOM, POSITIVE, NEGATIVE, EMPTY, EMPTYPOSITIVE, EMPTYNEGATIVE
    }

    private final ExtSign _sign;

    public ExtSignDomain(ExtSign _sign) {
        this._sign = _sign;
    }

    public ExtSignDomain() {
        this(null);
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this._sign == NEGATIVE) {
            if (other._sign == EMPTYNEGATIVE || other._sign == EMPTY) {
                return new ExtSignDomain(ExtSign.EMPTYNEGATIVE);
            } else {
                return TOP;
            }
        } else if (this._sign == EMPTY) {
            if (other._sign == EMPTYNEGATIVE || other._sign == NEGATIVE) {
                return new ExtSignDomain(ExtSign.EMPTYNEGATIVE);
            } else if (other._sign == EMPTYPOSITIVE || other._sign == POSITIVE) {
                return new ExtSignDomain(ExtSign.EMPTYPOSITIVE);
            } else {
                return TOP;
            }
        } else if (this._sign == POSITIVE) {
            if (other._sign == EMPTY || other._sign == EMPTYPOSITIVE) {
                return new ExtSignDomain(ExtSign.EMPTYPOSITIVE);
            } else {
                return TOP;
            }
        } else if (this._sign == EMPTYNEGATIVE) {
            if (other._sign == EMPTY || other._sign == NEGATIVE) {
                return new ExtSignDomain(ExtSign.EMPTYNEGATIVE);
            } else {
                return TOP;
            }
        } else if (this._sign == EMPTYPOSITIVE) {
            if (other._sign == EMPTY || other._sign == POSITIVE) {
                return new ExtSignDomain(ExtSign.EMPTYPOSITIVE);
            } else {
                return TOP;
            }
        } else {
            return TOP;
        }
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return (this._sign == NEGATIVE && other._sign == EMPTYNEGATIVE) ||
                (this._sign == EMPTY && other._sign == EMPTYNEGATIVE) ||
                (this._sign == EMPTY && other._sign == ExtSign.EMPTYPOSITIVE) ||
                (this._sign == POSITIVE && other._sign == EMPTYPOSITIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExtSignDomain))
            return false;
        ExtSignDomain that = (ExtSignDomain) o;
        return _sign == that._sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_sign);
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    private ExtSignDomain negate() {
        if (_sign == ExtSign.NEGATIVE)
            return new ExtSignDomain(ExtSign.POSITIVE);
        if (_sign == ExtSign.POSITIVE)
            return new ExtSignDomain(ExtSign.NEGATIVE);
        if (_sign == EMPTYPOSITIVE)
            return new ExtSignDomain(EMPTYNEGATIVE);
        if (_sign == EMPTYNEGATIVE)
            return new ExtSignDomain(EMPTYPOSITIVE);
        else
            return this;
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(POSITIVE);
            if (v == 0)
                return new ExtSignDomain(EMPTY);
            else
                return new ExtSignDomain(NEGATIVE);
        }
        return top();
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp)
            throws SemanticException {
        // We evaluate just this case since we are considering just the integer numbers
        if (operator instanceof NumericNegation)
            return arg.negate();

        return top();
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain _left, ExtSignDomain _right,
            ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            if (_left._sign == NEGATIVE) {
                if (_right._sign == NEGATIVE || _right._sign == EMPTY || _right._sign == EMPTYNEGATIVE) {
                    return _left;
                } else {
                    return TOP;
                }
            } else if (_left._sign == POSITIVE) {
                if (_right._sign == POSITIVE || _right._sign == EMPTY || _right._sign == EMPTYPOSITIVE) {
                    return _left;
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYPOSITIVE) {
                if (_right._sign == EMPTYNEGATIVE || _right._sign == EMPTY) {
                    return _left;
                } else if (_right._sign == NEGATIVE) {
                    return _right;
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYNEGATIVE) {
                if (_right._sign == EMPTYPOSITIVE || _right._sign == EMPTY) {
                    return _left;
                } else if (_right._sign == POSITIVE) {
                    return _right;
                } else {
                    return TOP;
                }
            } else if (_left._sign == TOP._sign) {
                return TOP;
            } else if (_left._sign == EMPTY) {
                return _right;
            } else {
                return TOP;
            }
        } else if (operator instanceof SubtractionOperator) {
            if (_left._sign == NEGATIVE) {
                if (_right._sign == POSITIVE || _right._sign == EMPTY || _right._sign == EMPTYPOSITIVE) {
                    return _left;
                } else {
                    return TOP;
                }
            } else if (_left._sign == POSITIVE) {
                if (_right._sign == NEGATIVE || _right._sign == EMPTY) {
                    return _left;
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYPOSITIVE) {
                if (_right._sign == EMPTYNEGATIVE || _right._sign == EMPTY) {
                    return _left;
                } else if (_right._sign == NEGATIVE) {
                    return new ExtSignDomain(POSITIVE);
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYNEGATIVE) {
                if (_right._sign == EMPTYPOSITIVE || _right._sign == EMPTY) {
                    return _left;
                } else if (_right._sign == POSITIVE) {
                    return new ExtSignDomain(NEGATIVE);
                } else {
                    return TOP;
                }
            } else if (_left._sign == TOP._sign) {
                return TOP;
            } else if (_left._sign == EMPTY) {
                return _right.negate();
            } else {
                return TOP;
            }
        } else if (operator instanceof Multiplication) {
            if (_left._sign == NEGATIVE) {
                return _right.negate();
            } else if (_left._sign == POSITIVE) {
                return _right;
            } else if (_left._sign == EMPTYPOSITIVE) {
                if (_right._sign == EMPTYPOSITIVE || _right._sign == POSITIVE) {
                    return _left;
                } else if (_right._sign == EMPTYNEGATIVE || _right._sign == EMPTY) {
                    return _right;
                } else if (_right._sign == NEGATIVE) {
                    return new ExtSignDomain(EMPTYNEGATIVE);
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYNEGATIVE) {
                if (_right._sign == EMPTYPOSITIVE || _right._sign == POSITIVE) {
                    return _left;
                } else if (_right._sign == EMPTYNEGATIVE || _right._sign == NEGATIVE) {
                    return new ExtSignDomain(EMPTYPOSITIVE);
                } else if (_right._sign == EMPTY) {
                    return _right;
                } else {
                    return TOP;
                }
            } else if (_left._sign == TOP._sign) {
                return TOP;
            } else if (_left._sign == EMPTY) {
                return new ExtSignDomain(EMPTY);
            } else {
                return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (_right._sign == EMPTY ||
                    _right._sign == EMPTYPOSITIVE ||
                    _right._sign == EMPTYNEGATIVE)
                return BOTTOM;

            if (_left._sign == NEGATIVE) {
                return _right.negate();
            } else if (_left._sign == POSITIVE) {
                return _right;
            } else if (_left._sign == EMPTYPOSITIVE) {
                if (_right._sign == POSITIVE) {
                    return _left;
                } else if (_right._sign == NEGATIVE) {
                    return new ExtSignDomain(EMPTYPOSITIVE);
                } else {
                    return TOP;
                }
            } else if (_left._sign == EMPTYNEGATIVE) {
                if (_right._sign == POSITIVE) {
                    return _left;
                } else if (_right._sign == NEGATIVE) {
                    return new ExtSignDomain(EMPTYPOSITIVE);
                } else {
                    return TOP;
                }
            } else if (_left._sign == TOP._sign) {
                return TOP;
            } else if (_left._sign == EMPTY) {
                return new ExtSignDomain(EMPTY);
            } else {
                return TOP;
            }
        }
        return top();
    }
    // IMPLEMENTATION NOTE:
    // the code below is outside of the scope of the course. You can uncomment it to
    // get
    // your code to compile. Beware that the code is written expecting that a field
    // named
    // "_sign" containing an enumeration (similar to the one saw during class)
    // exists in
    // this class: if you name it differently, change also the code below to make it
    // work
    // by just using the name of your choice instead of "_sign"

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(_sign);
    }
}
