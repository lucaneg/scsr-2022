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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain BOTTOM = new ExtSignDomain(ExtSign.BOTTOM);
    private static final ExtSignDomain TOP = new ExtSignDomain(ExtSign.TOP);

    enum ExtSign {
        BOTTOM, MINUS, ZERO, PLUS, MINZERO, PLUSZERO, TOP;
    }

    private final ExtSign extSign;

    public ExtSignDomain() {
        this.extSign = ExtSign.TOP;
    }

    public ExtSignDomain(ExtSign extSign) {
        this.extSign = extSign;
    }


    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.ZERO) {
            return new ExtSignDomain(ExtSign.MINZERO);
        }else if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.ZERO) {
            return new ExtSignDomain(ExtSign.PLUSZERO);
        }else {
            return new ExtSignDomain(ExtSign.TOP);
        }
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return TOP;
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if(this.extSign == ExtSign.MINUS && other.extSign == ExtSign.MINZERO) {
            return true;
        }else if(this.extSign == ExtSign.ZERO && (other.extSign == ExtSign.MINZERO || other.extSign == ExtSign.PLUSZERO)) {
            return true;
        }else if(this.extSign == ExtSign.PLUS && other.extSign == ExtSign.PLUSZERO) {
            return true;
        }else {
            return false;
        }
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
        if (extSign != other.extSign)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extSign);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(extSign);
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
        if (this.extSign == ExtSign.MINUS) {
            return new ExtSignDomain(ExtSign.PLUS);
        }else if (this.extSign == ExtSign.PLUS) {
            return new ExtSignDomain(ExtSign.MINUS);
        }else if(this.extSign == ExtSign.MINZERO) {
            return new ExtSignDomain(ExtSign.PLUSZERO);
        }else if(this.extSign == ExtSign.PLUSZERO) {
            return new ExtSignDomain(ExtSign.MINZERO);
        }else {
            return this;
        }

    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            Integer value = (Integer) constant.getValue();
            if(value>0) {
                return new ExtSignDomain(ExtSign.PLUS);
            }else if(value == 0) {
                return new ExtSignDomain(ExtSign.ZERO);
            }else
                return new ExtSignDomain(ExtSign.MINUS);
        }
        return top();
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
        if(operator instanceof AdditionOperator) {
            switch(left.extSign) {
                case MINUS:
                    switch(right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case PLUS:
                            return TOP;
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                            return TOP;
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.extSign) {
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(extSign.PLUSZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(extSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(extSign.MINZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUSZERO:
                    switch (right.extSign) {
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(extSign.PLUSZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(extSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(extSign.ZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case MINZERO:
                    switch (right.extSign) {
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(extSign.MINZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(extSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(extSign.ZERO);
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                default:
                    return TOP;
            }
        }
        else if(operator instanceof SubtractionOperator) {
            switch(left.extSign) {
                case MINUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case PLUS:
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.extSign) {
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUSZERO:
                    switch (right.extSign) {
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case MINZERO:
                    switch (right.extSign) {
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                default:
                    return TOP;
            }
        }
        else if(operator instanceof Multiplication) {
            switch(left.extSign) {
                case MINUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUSZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case MINZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
                default:
                    return TOP;
            }
        }
        else if(operator instanceof DivisionOperator) {
            if (right.extSign == ExtSign.ZERO || right.extSign == ExtSign.PLUSZERO || right.extSign == ExtSign.MINZERO) {
                return BOTTOM;
            }
            switch(left.extSign) {
                case MINUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUS:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUS);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINUS);
                        case TOP:
                        default:
                            return TOP;
                    }
                case ZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.ZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case PLUSZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case MINZERO:
                    switch (right.extSign) {
                        case ZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case PLUSZERO:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINZERO:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case PLUS:
                            return new ExtSignDomain(ExtSign.MINZERO);
                        case MINUS:
                            return new ExtSignDomain(ExtSign.PLUSZERO);
                        case TOP:
                        default:
                            return TOP;
                    }
                case TOP:
                    return TOP;
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

}
