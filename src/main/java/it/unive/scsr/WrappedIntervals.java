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
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.type.NumericType;
import it.unive.lisa.type.common.*;

import java.util.ArrayList;

public class WrappedIntervals extends BaseNonRelationalValueDomain<WrappedIntervals>{

    private static final WrappedIntervals TOP = new WrappedIntervals(WrappedInterval.TOP);
    private static final WrappedIntervals BOTTOM = new WrappedIntervals(WrappedInterval.BOTTOM);
    private ModularBinaryString left;
    private ModularBinaryString right;
    private final String maxSigned;
    private final WrappedInterval type;
    private final int MAX_VALUE = Integer.MAX_VALUE; //Max value used by programming language: edit before analysis

    enum WrappedInterval{
        TOP, BOTTOM, SIMPLE;
    }

    private WrappedIntervals(WrappedInterval type){
        //If type SIMPLE is provided an error is thrown
        assert type != WrappedInterval.SIMPLE;
        this.type = type;
        this.maxSigned = "";
    }

    public WrappedIntervals(){
        this.type = WrappedInterval.TOP;
        this.maxSigned = "";
        top();
    }

    //Constructor from long variables
    public WrappedIntervals(long left, long right, long maxSigned) throws ModularBinaryString.SumOverflowException {
        this.type = WrappedInterval.SIMPLE;
        this.maxSigned = Long.toBinaryString(maxSigned);
        this.left = ModularBinaryString.fromLongs(left, maxSigned);
        this.right = ModularBinaryString.fromLongs(right, maxSigned);
        assert ! this.left.equals(this.right.modularSum(ModularBinaryString.getOne(this.maxSigned))); //Checking that x != y +w 1 (according to definition of wrapped interval)
    }

    //Constructor from String variables: allows the usage of very big values (bigger than Java 64 bits implementation of long values)
    public WrappedIntervals(String left, String right, String maxSigned) throws ModularBinaryString.SumOverflowException {
        this.type = WrappedInterval.SIMPLE;
        this.maxSigned = maxSigned;
        this.left = ModularBinaryString.fromStrings(left, maxSigned);
        this.right = ModularBinaryString.fromStrings(right, maxSigned);
        assert ! this.left.equals(this.right.modularSum(ModularBinaryString.getOne(maxSigned))); //Checking that x != y +w 1 (according to definition of wrapped interval)
    }

    //Constructor from String variables: allows the usage of very big values (bigger than Java 64 bits implementation of long values)
    public WrappedIntervals(ModularBinaryString left, ModularBinaryString right, String maxSigned) throws ModularBinaryString.SumOverflowException {
        this.type = WrappedInterval.SIMPLE;
        assert ModularBinaryString.reduced(left.getOverflowAt()).equals(ModularBinaryString.reduced(right.getOverflowAt()));
        this.maxSigned = maxSigned;
        this.left = left.editMaxSigned(maxSigned);
        this.right = right.editMaxSigned(maxSigned);
        assert ! this.left.equals(this.right.modularSum(ModularBinaryString.getOne(maxSigned))); //Checking that x != y +w 1 (according to definition of wrapped interval)
    }


    //Checks according to interval inclusion definition weather this interval is included in interval t
    //This relation is what defines the ordering and so the poset of wrapped intervals
    public boolean isIncluded(WrappedIntervals t) throws ModularBinaryString.SumOverflowException{
        if(type == WrappedInterval.BOTTOM || t.type == WrappedInterval.TOP)
            return true;
        if(t.type == WrappedInterval.BOTTOM || type == WrappedInterval.TOP)
            return false;
        return t.includes(left) && t.includes(right) && (!this.includes(t.left) || !this.includes(t.right));
    }

    //Checks according to inclusion definition weather a modular string representation is included into this wrapped interval
    public boolean includes(ModularBinaryString e) throws ModularBinaryString.SumOverflowException {
        if(this.type == WrappedInterval.TOP)
            return true;
        if(this.type == WrappedInterval.BOTTOM)
            return false;
        return e.modularCompareTo(right, left.getBinaryString()) <= 0;
    }

    //Returns cardinality of wrapped interval computed according to definition provided
    public long cardinality() throws ModularBinaryString.SumOverflowException {
        if(this.type == WrappedInterval.BOTTOM)
            return 0;
        if(this.type == WrappedInterval.TOP)
            return MAX_VALUE; //TODO: fix, information is lost due to constructors
        return left.modularSubtraction(right).modularSum(ModularBinaryString.getOne(left.getOverflowAt())).asUnsigned();
    }

    //Returns negation of wrapped interval computed according to definition provided
    public WrappedIntervals complement() throws ModularBinaryString.SumOverflowException {
        if(this.type == WrappedInterval.BOTTOM)
            return WrappedIntervals.TOP;
        if(this.type == WrappedInterval.TOP)
            return WrappedIntervals.BOTTOM;
        else
            return new WrappedIntervals(
                    left.modularSum(ModularBinaryString.getOne(left.getOverflowAt())).getBinaryString(),
                    right.modularSubtraction(ModularBinaryString.getOne(right.getOverflowAt())).getBinaryString(), this.maxSigned);
    }

    //Returns result of pseudo-join operation between this WrappedIntervals and another one (t) according to definition provided
    //For a nice visual explanation check Fig. 2 of the research paper
    public WrappedIntervals pseudoJoin(WrappedIntervals t) throws ModularBinaryString.SumOverflowException {
        if(this.isIncluded(t))
            return t;
        if(t.isIncluded(this))
            return this;
        if(t.includes(this.left) && t.includes(this.right) && this.includes(t.left) && this.includes(t.right))
            return TOP;
        if(t.includes(this.right) && this.includes(t.left))
            return new WrappedIntervals(this.left, t.right, this.maxSigned);
        if(this.includes(t.right) && t.includes(this.left))
            return new WrappedIntervals(t.left, this.right, this.maxSigned);
        WrappedIntervals bc = new WrappedIntervals(this.right, t.left, this.maxSigned);
        WrappedIntervals da = new WrappedIntervals(t.right, this.left, this.maxSigned);
        if( bc.cardinality() <= da.cardinality() || (bc.cardinality() == da.cardinality() && this.left.compareTo(t.left) <= 0))
            return new WrappedIntervals(this.left, t.right, this.maxSigned);
        else
            return new WrappedIntervals(t.left, this.left, this.maxSigned);
    }

    //Returns result of pseudo-meet operation between this WrappedIntervals and another one (t) according to definition provided
    public WrappedIntervals pseudoMeet(WrappedIntervals t) throws ModularBinaryString.SumOverflowException {
        return (complement().pseudoJoin(t.complement())).complement();
    }

    //Returns result of gap operation between this WrappedIntervals and another one (t) according to definition provided
    public WrappedIntervals gap(WrappedIntervals t) throws ModularBinaryString.SumOverflowException {
        if(t.equals(bottom()) || t.equals(top()) || this.equals(bottom()) || this.equals(top()))
            return BOTTOM;
        if(! t.includes(this.right) && ! this.includes(t.left))
            return new WrappedIntervals(t.left, this.right, this.maxSigned).complement();
        return BOTTOM;
    }

    //Returns result of the extend operation between this WrappedIntervals and another one (t) according to definition provided
    public WrappedIntervals extend(WrappedIntervals t) throws ModularBinaryString.SumOverflowException{
        if(isIncluded(t))
            return t;
        if(t.isIncluded(this))
            return this;
        if(t.includes(this.left) && t.includes(this.right) && this.includes(t.left) && this.includes(t.right))
            return TOP;
        if(t.includes(this.right) && this.includes(t.left))
            return new WrappedIntervals(this.left, t.right, this.maxSigned);
        if(this.includes(t.right) && t.includes(this.left))
            return new WrappedIntervals(t.left, this.right, this.maxSigned);
        return new WrappedIntervals(this.left, t.right, this.maxSigned);
    }

    //Returns result of the bigger operation between this WrappedIntervals and another one (t) according to definition provided
    public WrappedIntervals bigger(WrappedIntervals t) throws ModularBinaryString.SumOverflowException {
        if(this.cardinality() > t.cardinality())
            return this;
        return t;
    }

    public long getUnsignedLeft(){
        return left.asUnsigned();
    }

    public long getUnsignedRight(){
        return right.asUnsigned();
    }

    public long getSignedLeft() throws ModularBinaryString.SumOverflowException {
        return left.asSigned();
    }

    public long getSignedRight() throws ModularBinaryString.SumOverflowException {
        return left.asSigned();
    }

    public String getStringedInterval() throws ModularBinaryString.SumOverflowException {
        if(this.type == WrappedInterval.SIMPLE) {
            String asSigned = " and sig[" + getSignedLeft() + ", " + getSignedRight() + "] ";
            String asUnsigned = " = usig[" + getUnsignedLeft() + ", " + getUnsignedRight() + "]";
            return "[" + ModularBinaryString.reduced(left.getBinaryString()) + ", " + ModularBinaryString.reduced(right.getBinaryString()) + "] % " + left.getOverflowAt() + asUnsigned + asSigned;
        }
        if(this.type == WrappedInterval.TOP)
            return "TOP";
        else return "BOTTOM";
    }

    /**\*************************************************************************************************\**/



    //Function could be easily extended to compute lub of a set of WrappedIntervals
    @Override
    protected WrappedIntervals lubAux(WrappedIntervals other) throws SemanticException {
        WrappedIntervals f = BOTTOM;
        WrappedIntervals g = BOTTOM;
        ArrayList <WrappedIntervals> lexOrdering = new ArrayList<>();
        if(this.left.compareTo(other.left) <= 0){ //TODO check if this is definition
            lexOrdering.add(this);
            lexOrdering.add(other);
        }
        else{
            lexOrdering.add(other);
            lexOrdering.add(this);
        }
        try {
            for (WrappedIntervals s : lexOrdering) {
                if (s.equals(TOP) || s.right.compareTo(s.left) <=0) {
                    f = f.extend(s);
                }
            }
            for (WrappedIntervals s : lexOrdering) {
                g = g.bigger(f.gap(s));
                f = f.extend(s);
            }
            return (g.bigger(f.complement())).complement();
        } catch (ModularBinaryString.SumOverflowException e) {
            throw new SemanticException(e);
        }
    }


    @Override
    protected WrappedIntervals wideningAux(WrappedIntervals other) throws SemanticException {
        try {
            if(other.isIncluded(this))
                return this;
            if(this.cardinality() >= Long.parseLong(this.maxSigned, 2))
                return TOP;
            if(this.pseudoJoin(other).equals(new WrappedIntervals(this.left, other.right, this.maxSigned))){
                ModularBinaryString twoV = this.right.modularMultiplication(ModularBinaryString.fromStrings("2", this.right.getOverflowAt()));
                WrappedIntervals c = new WrappedIntervals(this.left, twoV.modularSubtraction(this.left).modularSum(ModularBinaryString.getOne(this.right.getOverflowAt())), this.maxSigned); //[u, 2v-u+1]
                return new WrappedIntervals(this.left, other.right, this.maxSigned).pseudoJoin(c);
            }
            if(this.pseudoJoin(other).equals(new WrappedIntervals(other.left, this.right, this.maxSigned))){
                ModularBinaryString twoU = this.left.modularMultiplication(ModularBinaryString.fromStrings("2", this.left.getOverflowAt()));
                WrappedIntervals c = new WrappedIntervals(twoU.modularSubtraction(this.right).modularSubtraction(ModularBinaryString.getOne(this.right.getOverflowAt())), this.right, this.maxSigned); //[2u-v-1, v]
                return new WrappedIntervals(other.left, this.right, this.maxSigned).pseudoJoin(c);
            }
            if(other.includes(this.left) && other.includes(this.right)){
                ModularBinaryString twoV = this.right.modularMultiplication(ModularBinaryString.fromStrings("2", this.right.getOverflowAt()));
                ModularBinaryString twoU = this.left.modularMultiplication(ModularBinaryString.fromStrings("2", this.left.getOverflowAt()));
                WrappedIntervals c = new WrappedIntervals(other.left, other.left.modularSum(twoV).modularSubtraction(twoU).modularSum(ModularBinaryString.getOne(this.right.getOverflowAt())), this.maxSigned);//[x, x+2v-2u+1]
                return new WrappedIntervals(other.left, other.right, this.maxSigned).pseudoJoin(c);
            }
            return TOP;
        } catch (ModularBinaryString.SumOverflowException e) {
            throw new SemanticException(e);
        }
    }



    @Override
    protected boolean lessOrEqualAux(WrappedIntervals other) throws SemanticException {
        try {
            return isIncluded(other);
        } catch (ModularBinaryString.SumOverflowException e) {
            throw new SemanticException(e);
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
        WrappedIntervals other = (WrappedIntervals) obj;
        if(type == WrappedInterval.TOP && other.type == WrappedInterval.TOP
        || type == WrappedInterval.BOTTOM && other.type == WrappedInterval.BOTTOM)
            return true;
        if(type != WrappedInterval.SIMPLE || ((WrappedIntervals) obj).type != WrappedInterval.SIMPLE)
            return false;
        return this.left.equals(((WrappedIntervals) obj).left) && this.right.equals(((WrappedIntervals) obj).right);
    }


    @Override
    public int hashCode() {
        return 0;
    }


    @Override
    public WrappedIntervals top() {
        return TOP;
    }


    @Override
    public WrappedIntervals bottom() {
        return BOTTOM;
    }


    @Override
    protected WrappedIntervals evalUnaryExpression(UnaryOperator operator, WrappedIntervals arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation) {
            try {
                return complement();
            } catch (ModularBinaryString.SumOverflowException e) {
                throw new SemanticException(e);
            }
        }
        return top();
    }


    @Override
    protected WrappedIntervals evalBinaryExpression(BinaryOperator operator, WrappedIntervals sx, WrappedIntervals dx, ProgramPoint pp) throws SemanticException {
        try{
            if(operator instanceof TypeCast){
                operator = (TypeCast) operator;//TODO: implement type cast & conversion
            }

            if(operator instanceof NumericNonOverflowingAdd){ //Cheating again...
                operator = Numeric8BitAdd.INSTANCE;
            } else if(operator instanceof NumericNonOverflowingSub){
                operator = Numeric8BitSub.INSTANCE;
            }else if(operator instanceof NumericNonOverflowingMul){
                operator = Numeric8BitMul.INSTANCE;
            }else if(operator instanceof NumericNonOverflowingDiv){
                operator = Numeric8BitDiv.INSTANCE;
            }else if(operator instanceof NumericNonOverflowingMod){
                operator = Numeric8BitMod.INSTANCE;
            }

            long memorySize = 1;
            if (operator instanceof Numeric8BitAdd || operator instanceof Numeric8BitSub || operator instanceof Numeric8BitMul || operator instanceof Numeric8BitDiv ||operator instanceof Numeric8BitMod)
                memorySize = 8;
            else if (operator instanceof Numeric16BitAdd || operator instanceof Numeric16BitSub || operator instanceof Numeric16BitMul || operator instanceof Numeric16BitDiv || operator instanceof Numeric16BitMod)
                memorySize = 16;
            else if (operator instanceof Numeric32BitAdd || operator instanceof Numeric32BitSub || operator instanceof Numeric32BitMul || operator instanceof Numeric32BitDiv || operator instanceof Numeric32BitMod)
                memorySize = 32;
            else if (operator instanceof Numeric64BitAdd || operator instanceof Numeric64BitSub || operator instanceof Numeric64BitMul || operator instanceof Numeric64BitDiv || operator instanceof Numeric64BitMod)
            memorySize = 64;
            WrappedIntervals correctedSx = sx;
            WrappedIntervals correctedDx = dx;
            if(sx.type == WrappedInterval.SIMPLE)
                correctedSx = new WrappedIntervals(sx.left, sx.right, Long.toBinaryString((long)Math.pow(2, memorySize))); //Type casting for sx
            if(dx.type == WrappedInterval.SIMPLE)
                correctedDx = new WrappedIntervals(dx.left, dx.right, Long.toBinaryString((long)Math.pow(2, memorySize))); //Type casting for dx

            if(operator instanceof AdditionOperator){
                if(correctedSx.equals(BOTTOM) || correctedDx.equals(BOTTOM))
                    return bottom();
                if(correctedSx.cardinality() + correctedDx.cardinality() <= (long)Math.pow(2, memorySize))
                    return new WrappedIntervals(correctedSx.left.modularSum(correctedDx.left), correctedSx.right.modularSum(correctedDx.right), correctedSx.maxSigned);
                return top();
            }
            else if(operator instanceof SubtractionOperator){
                if(correctedSx.equals(BOTTOM) || correctedDx.equals(BOTTOM))
                    return bottom();
                if(correctedSx.cardinality() + correctedDx.cardinality() <= (long)Math.pow(2, memorySize))
                    return new WrappedIntervals(correctedSx.left.modularSubtraction(correctedDx.left), correctedSx.right.modularSubtraction(correctedDx.right), correctedSx.maxSigned);
                return top();
            }
            else if(operator instanceof Multiplication){
                //TODO: implement
            }
            else if(operator instanceof DivisionOperator){
                //TODO: implement
            }
        }
        catch (ModularBinaryString.SumOverflowException e){
            throw new SemanticException(e);
        }

        return bottom();
    }


    @Override
    protected WrappedIntervals evalTernaryExpression(TernaryOperator operator, WrappedIntervals sx, WrappedIntervals middle, WrappedIntervals dx, ProgramPoint pp) throws SemanticException {
        return super.evalTernaryExpression(operator, sx, middle, dx, pp);
    }


    @Override
    protected WrappedIntervals evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        long memorySize = 4;
        if (constant.getStaticType() instanceof NumericType
                && !(constant.getStaticType() instanceof Float32) && !(constant.getStaticType() instanceof Float64)) {

            constant = new Constant(Int8.INSTANCE, constant.getValue(), pp.getLocation()); //TODO: cheating
            try {
                if(constant.getStaticType() instanceof Int8 || constant.getStaticType() instanceof UInt8) //Safe to deal here thanks to cast to int type
                    memorySize = 8;
                else if(constant.getStaticType() instanceof Int16 || constant.getStaticType() instanceof UInt16) //Safe to deal here thanks to cast to int type
                    memorySize = 16;
                else if(constant.getStaticType() instanceof Int32 || constant.getStaticType() instanceof UInt32) //Safe to deal here thanks to cast to long type
                    memorySize = 32;
                else if(constant.getStaticType() instanceof Int64)
                    memorySize = 64;
                else if(constant.getStaticType() instanceof UInt64){ //Need to be created from string due to possible truncation if value exceeds max long
                    memorySize = 64;
                    return new WrappedIntervals((String)constant.getValue(), (String)constant.getValue(), Long.toBinaryString((long)Math.pow(2, memorySize)));
                }
                return new WrappedIntervals((Integer)constant.getValue(), (Integer)constant.getValue(), (long)Math.pow(2, memorySize)); //TODO: fix taking from int
            } catch (ModularBinaryString.SumOverflowException e) {
                throw new SemanticException(e);
            }
        }
        return top();
    }

    /************************************************/

    @Override
    public DomainRepresentation representation() {
        try {
            return new StringRepresentation(getStringedInterval());
        } catch (ModularBinaryString.SumOverflowException e) {
            return new StringRepresentation("Error in representation");
        }
    }
}
