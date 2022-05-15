package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Parity;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.PairRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.ValueExpression;

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
    protected ReducedProduct wideningAux(ReducedProduct other) throws SemanticException {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        
        //System.out.println("eval");

        ValueEnvironment<Parity> vp = new ValueEnvironment<Parity>(environment.lattice.parity);
        ValueEnvironment<ExtSignDomainSolution> vs = new ValueEnvironment<ExtSignDomainSolution>(environment.lattice.sign);
        Parity p = this.parity.eval(expression, vp, pp);
        ExtSignDomainSolution s = this.sign.eval(expression, vs, pp);
        return new ReducedProduct(s, p);
    }
}
