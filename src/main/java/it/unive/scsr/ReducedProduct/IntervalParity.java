package it.unive.scsr.ReducedProduct;

import java.util.Objects;


import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.PairRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.StringLength;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;

public class IntervalParity extends BaseNonRelationalValueDomain<IntervalParity> {

    public IntInterval interval;
    public Integer parity;

    private static final Integer TOP = 1;
    private static final Integer BOTTOM = 2;
    private static final Integer ODD = 3;
    private static final Integer EVEN = 4;
	
    private static final IntInterval INT_TOP = new IntInterval(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    private static final IntInterval INT_BOTTOM = null;
    private static final IntInterval INT_ZERO = new IntInterval(0, 0);

    public IntervalParity() {
        // top and top
        this(INT_TOP, TOP);
    }
    
    public IntervalParity(IntInterval interval, Integer parity) {
        this.interval = interval;
        this.parity = parity;
    }

    @Override
    public DomainRepresentation representation() {
        return new PairRepresentation(int_repr(), parity_repr());
    }

    private DomainRepresentation parity_repr(){
        if (parity == BOTTOM)
			return Lattice.BOTTOM_REPR;
		if (parity == TOP)
			return Lattice.TOP_REPR;

		String repr;
		if (parity == EVEN)
			repr = "Even";
		else
			repr = "Odd";

		return new StringRepresentation(repr);
    }

    private DomainRepresentation int_repr(){
        if (interval == INT_BOTTOM)
			return Lattice.BOTTOM_REPR;

		return new StringRepresentation(interval.toString());
    }

    @Override
    public IntervalParity top() {
        return new IntervalParity();
    }

    @Override
    public IntervalParity bottom() {
        return new IntervalParity(INT_BOTTOM, BOTTOM);
    }

    @Override
	public boolean isTop() {
		return interval == INT_TOP && parity == TOP;
	}

	@Override
	public boolean isBottom() {
		return interval == INT_BOTTOM && parity == BOTTOM;
	}

    @Override
    protected IntervalParity lubAux(IntervalParity other) throws SemanticException {
        //return new IntervalParity(INT_TOP, TOP);
        return new IntervalParity(lubAuxInterval(other.interval), lubAuxParity(other.parity));
    }

    @Override
	protected IntervalParity glbAux(IntervalParity other) {
        //return new IntervalParity(INT_TOP, TOP);
		return new IntervalParity(glbAuxInterval(other.interval), BOTTOM);
	}

    @Override
    protected IntervalParity wideningAux(IntervalParity other) throws SemanticException {
        //return new IntervalParity(INT_TOP, TOP);
        return new IntervalParity(wideningAuxInterval(other.interval), wideningAuxParity(other.parity));
    }

    @Override
    protected boolean lessOrEqualAux(IntervalParity other) throws SemanticException {
        //return false;
        return lessOrEqualAuxInterval(other.interval) && lessOrEqualAuxParity(other.parity);
    }

    @Override
	protected IntervalParity evalNonNullConstant(Constant constant, ProgramPoint pp) {
        //return new IntervalParity(INT_TOP, TOP);
        return new IntervalParity(evalNonNullConstantInterval(constant, pp), evalNonNullConstantParity(constant, pp));
	}

	@Override
	protected IntervalParity evalUnaryExpression(UnaryOperator operator, IntervalParity arg, ProgramPoint pp) {
        //return new IntervalParity(INT_TOP, TOP);
        return new IntervalParity(evalUnaryExpressionInterval(operator, arg.interval, pp), evalUnaryExpressionParity(operator, arg.parity, pp));
	}

	@Override
	protected IntervalParity evalBinaryExpression(BinaryOperator operator, IntervalParity left, IntervalParity right, ProgramPoint pp) {
		//return new IntervalParity(INT_TOP, TOP);
        return new IntervalParity(evalBinaryExpressionInterval(operator, left.interval, right.interval, pp),
            evalBinaryExpressionParity(operator, left.parity, right.parity, pp));
	}

    // Interval

	protected IntInterval evalNonNullConstantInterval(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			Integer i = (Integer) constant.getValue();
			return new IntInterval(new MathNumber(i), new MathNumber(i));
		}

		return INT_TOP;
	}

	protected IntInterval evalUnaryExpressionInterval(UnaryOperator operator, IntInterval arg, ProgramPoint pp) {
		if (operator == NumericNegation.INSTANCE)
			if (arg == INT_TOP)
				return INT_TOP;
			else
				return arg.mul(IntInterval.MINUS_ONE);
		else if (operator == StringLength.INSTANCE)
			return new IntInterval(MathNumber.ZERO, MathNumber.PLUS_INFINITY);
		else
			return INT_TOP;
	}

	private boolean interval_is(IntInterval i, int n) {
		return interval != INT_BOTTOM && interval.isSingleton() && interval.getLow().is(n);
	}

	protected IntInterval evalBinaryExpressionInterval(BinaryOperator operator, IntInterval left, IntInterval right, ProgramPoint pp) {
		if (!(operator instanceof DivisionOperator) && (left == INT_TOP || right == INT_TOP))
			// with div, we can return zero or bottom even if one of the
			// operands is top
			return INT_TOP;

		if (operator instanceof AdditionOperator) {
			return left.plus(right);
		}
		else if (operator instanceof SubtractionOperator)
			return left.diff(right);
		else if (operator instanceof Multiplication)
			if (interval_is(left, 0) || interval_is(right, 0))
				return INT_ZERO;
			else
				return left.mul(right);
		else if (operator instanceof DivisionOperator)
			if (interval_is(right, 0))
				return INT_BOTTOM;
			else if (interval_is(left, 0))
				return INT_ZERO;
			else if (left == INT_TOP || right == INT_TOP)
				return INT_TOP;
			else
				return left.div(right, false, false);
		return INT_TOP;
	}

	protected IntInterval lubAuxInterval(IntInterval other) throws SemanticException {
		MathNumber newLow = interval.getLow().min(other.getLow());
		MathNumber newHigh = interval.getHigh().max(other.getHigh());
		return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? INT_TOP : new IntInterval(newLow, newHigh);
	}

	protected IntInterval glbAuxInterval(IntInterval other) {
		MathNumber newLow = interval.getLow().max(other.getLow());
		MathNumber newHigh = interval.getHigh().min(other.getHigh());

		if (newLow.compareTo(newHigh) > 0)
			return INT_BOTTOM;
		return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? INT_TOP : new IntInterval(newLow, newHigh);
	}

	protected IntInterval wideningAuxInterval(IntInterval other) throws SemanticException {
		MathNumber newLow, newHigh;
		if (other.getHigh().compareTo(interval.getHigh()) > 0)
			newHigh = MathNumber.PLUS_INFINITY;
		else
			newHigh = interval.getHigh();

		if (other.getLow().compareTo(interval.getLow()) < 0)
			newLow = MathNumber.MINUS_INFINITY;
		else
			newLow = interval.getLow();

		return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? INT_TOP : new IntInterval(newLow, newHigh);
	}

	protected boolean lessOrEqualAuxInterval(IntInterval other) throws SemanticException {
		return other.includes(interval);
	}

    // Parity

	protected Integer evalNullConstantParity(ProgramPoint pp) {
		return TOP;
	}

	protected Integer evalNonNullConstantParity(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			Integer i = (Integer) constant.getValue();
			return i % 2 == 0 ? EVEN : ODD;
		}

		return TOP;
	}

	protected Integer evalUnaryExpressionParity(UnaryOperator operator, Integer arg, ProgramPoint pp) {
		if (operator == NumericNegation.INSTANCE)
			return arg;
		return TOP;
	}

	protected Integer evalBinaryExpressionParity(BinaryOperator operator, Integer left, Integer right, ProgramPoint pp) {
		if (left == TOP || right == TOP)
			return TOP;

		if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator)
			if (right == left)
				return EVEN;
			else
				return ODD;
		else if (operator instanceof Multiplication)
			if (left == EVEN || right == EVEN)
				return EVEN;
			else
				return ODD;
		else if (operator instanceof DivisionOperator)
			if (left == ODD)
				return right == ODD ? ODD : EVEN;
			else
				return right == ODD ? EVEN : TOP;

		return TOP;
	}

	protected Integer lubAuxParity(Integer other) throws SemanticException {
		return TOP;
	}

	protected Integer wideningAuxParity(Integer other) throws SemanticException {
		return lubAuxParity(other);
	}

	protected boolean lessOrEqualAuxParity(Integer other) throws SemanticException {
		return false;
	}

    // END

    @Override
    public boolean equals(Object obj) {
        // from CartesianProduct
        if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
        
        IntervalParity other = (IntervalParity) obj;

        if (interval == null) {
			if (other.interval != null)
				return false;
		} else if (!interval.equals(other.interval))
			return false;
		if (parity == null) {
			if (other.parity != null)
				return false;
		} else if (parity != other.parity)
			return false;
		return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parity, interval);
    }

    @Override
	protected ValueEnvironment<IntervalParity> assumeBinaryExpression(
			ValueEnvironment<IntervalParity> environment, BinaryOperator operator, ValueExpression left,
			ValueExpression right, ProgramPoint pp) throws SemanticException {

        Identifier id;
        IntervalParity eval;
        boolean rightIsExpr;
        if (left instanceof Identifier) {
            eval = eval(right, environment, pp);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval = eval(left, environment, pp);
            id = (Identifier) right;
            rightIsExpr = false;
        } else
            return environment;

        if (eval.isBottom())
            return environment.bottom();

        boolean lowIsMinusInfinity = eval.interval.lowIsMinusInfinity();
        IntervalParity low_inf = new IntervalParity(new IntInterval(eval.interval.getLow(), MathNumber.PLUS_INFINITY), parity);
        IntervalParity lowp1_inf = new IntervalParity(new IntInterval(eval.interval.getLow().add(MathNumber.ONE), MathNumber.PLUS_INFINITY), parity);
        IntervalParity inf_high = new IntervalParity(new IntInterval(MathNumber.MINUS_INFINITY, eval.interval.getHigh()), parity);
        IntervalParity inf_highm1 = new IntervalParity(new IntInterval(MathNumber.MINUS_INFINITY, eval.interval.getHigh().subtract(MathNumber.ONE)), parity);

        if (operator == ComparisonEq.INSTANCE)
            return environment.putState(id, eval);
        else if (operator == ComparisonGe.INSTANCE)
            if (rightIsExpr)
                return lowIsMinusInfinity ? environment : environment.putState(id, low_inf);
            else
                return environment.putState(id, inf_high);
        else if (operator == ComparisonGt.INSTANCE)
            if (rightIsExpr)
                return lowIsMinusInfinity ? environment : environment.putState(id, lowp1_inf);
            else
                return environment.putState(id, lowIsMinusInfinity ? eval : inf_highm1);
        else if (operator == ComparisonLe.INSTANCE)
            if (rightIsExpr)
                return environment.putState(id, inf_high);
            else
                return lowIsMinusInfinity ? environment : environment.putState(id, low_inf);
        else if (operator == ComparisonLt.INSTANCE)
            if (rightIsExpr)
                return environment.putState(id, lowIsMinusInfinity ? eval : inf_highm1);
            else
                return lowIsMinusInfinity ? environment : environment.putState(id, lowp1_inf);
        else
            return environment;
	}

    /**@Override
    public IntervalParity eval(ValueExpression expression, ValueEnvironment<IntervalParity> environment,
            ProgramPoint pp) throws SemanticException {

        //ValueEnvironment<Interval> vi = interval.assume(new ValueEnvironment<Interval>(environment.lattice.evalBinaryExpression(operator, left, right, pp)), expression, pp);
        //ValueEnvironment<Parity> vp = new ValueEnvironment<Parity>(environment.lattice.parity);
        //Interval i = interval().ev
        //parity p = environment.lattice.eval(expression, vp, pp);
        //this.interval = i;
        //System.out.println("eval");
        return new IntervalParity(interval, EVEN);
        
    }**/
    
}
