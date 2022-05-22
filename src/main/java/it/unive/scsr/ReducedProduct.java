package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.PairRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ReducedProduct extends BaseNonRelationalValueDomain<ReducedProduct> {
    
    // Left field is related to the abstract domain: "Extended sign"
    public ExtSignDomainSolution sign;

    // Right field is reletad to the abstract domain: "Parity"
    public Parity parity;

    public ReducedProduct(ExtSignDomainSolution sign, Parity parity) {
        this.sign = sign;
        this.parity = parity;
    }

    public ReducedProduct() {
        this(new ExtSignDomainSolution(), new Parity());
    }

    @Override
    public DomainRepresentation representation() {
        // Build a representation which contains left (ExtSign domain) and right (Parity domain)
        return new PairRepresentation(sign.representation(), parity.representation());
    }

    @Override
    public ReducedProduct top() {
        // Create a new element in the domain with top pair
        return new ReducedProduct(sign.top(), parity.top());
    }

    @Override
    public ReducedProduct bottom() {
        // Create a new element in the domain with bottom pair
        return new ReducedProduct(sign.bottom(), parity.bottom());
    }

    @Override
    public boolean isBottom() {
        // Element in the domain is bottom if both left and right are bottom
        return this.sign.isBottom() && this.parity.isBottom();
    }

    @Override
    public boolean isTop() {
        // Element in the domain is top if both left and right are top
        return this.sign.isTop() && this.parity.isTop();
    }

    @Override
    protected ReducedProduct lubAux(ReducedProduct other) throws SemanticException {
        // Create new element in the domain where each element of the pair is a comparison between this and other element of the domain
        return new ReducedProduct(this.sign.lub(other.sign), this.parity.lub(other.parity));
    }

    @Override
    protected ReducedProduct glbAux(ReducedProduct other) throws SemanticException {
        return new ReducedProduct(this.sign.glb(other.sign), this.parity.glb(other.parity));
    }

    @Override
    protected ReducedProduct wideningAux(ReducedProduct other) throws SemanticException {
        return new ReducedProduct(this.sign.wideningAux(other.sign), this.parity.wideningAux(other.parity));
    }

    @Override
    protected boolean lessOrEqualAux(ReducedProduct other) throws SemanticException {
        // Returns true if and only if this lattice element <= than given one.
        return this.sign.lessOrEqual(other.sign) && this.parity.lessOrEqual(other.parity);
    }

    @Override
    public boolean equals(Object obj) {

        // Same object
        if (this == obj)
            return true;
        
        // Object is null or this and obj belong to different classes
        if (obj == null || getClass() != obj.getClass())
            return false;

        // In this case, assume obj is an element of reduced product domain
        ReducedProduct other = (ReducedProduct) obj;

        // Check if left field of this and other are not equal
        if (this.sign == null && other.sign != null) {
            return false;
        } else if (!this.sign.equals(other.sign)) {
            return false;
        }

        // Check if right field of this and other are not equal
        if (this.parity == null && other.parity != null) {
            return false;
        } else if(!this.parity.equals(other.parity)) {
            return false;
        }
        
        // this and obj are equals
        return true;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int res = 1;

        // Calculate result using hash code of left (ExtSign domain)
        res = prime * res + ((this.sign == null) ? 0 : this.sign.hashCode());

        // Calculate result using hash code of right (Parity domain)
        res = prime * res + ((this.parity == null) ? 0 : this.parity.hashCode());

        return res;
    }

    @Override
    public ReducedProduct eval(ValueExpression expression, ValueEnvironment<ReducedProduct> environment,
            ProgramPoint pp) throws SemanticException {

        ReducedProduct evalResult = super.eval(expression, environment, pp);

        // Only possible refinements:
        if(evalResult.parity.isBottom() || evalResult.sign.isBottom()){
            return bottom();
        }

        return this.glb(evalResult);
    } 


    @Override
    protected ReducedProduct evalBinaryExpression(BinaryOperator operator, ReducedProduct left, ReducedProduct right,
            ProgramPoint pp) throws SemanticException {
        return new ReducedProduct(
            this.sign.evalBinaryExpression(operator, left.sign, right.sign, pp), 
            this.parity.evalBinaryExpression(operator, left.parity, right.parity, pp)
        );
    }

    @Override
    protected ReducedProduct evalUnaryExpression(UnaryOperator operator, ReducedProduct arg, ProgramPoint pp)
            throws SemanticException {
        return new ReducedProduct(
            this.sign.evalUnaryExpression(operator, arg.sign, pp),
            this.parity.evalUnaryExpression(operator, arg.parity, pp)
        );
    }

    @Override
    protected ReducedProduct evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
            return new ReducedProduct(
                this.sign.evalNonNullConstant(constant, pp), 
                this.parity.evalNonNullConstant(constant, pp)    
            );
		}
		return top();
    }
    
}
