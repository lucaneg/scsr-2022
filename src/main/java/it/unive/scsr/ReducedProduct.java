package it.unive.scsr;

import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Red;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.numeric.Parity;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.PairRepresentation;

public class ReducedProduct extends BaseNonRelationalValueDomain<ReducedProduct> {
    
    // Left field is related to the abstract domain: "Extended sign"
    public ExtSignDomainSolution left;

    // Right field is reletad to the abstract domain: "Parity"
    public Parity right;

    public ReducedProduct(ExtSignDomainSolution left, Parity right) {
        this.left = left;
        this.right = right;
    }

    public ReducedProduct() {
        this(new ExtSignDomainSolution(), new Parity());
    }

    @Override
    public DomainRepresentation representation() {
        // Build a representation which contains left (ExtSign domain) and right (Parity domain)
        return new PairRepresentation(left.representation(), right.representation());
    }

    @Override
    public ReducedProduct top() {
        // Create a new element in the domain with top pair
        return new ReducedProduct(left.top(), right.top());
    }

    @Override
    public ReducedProduct bottom() {
        // Create a new element in the domain with bottom pair
        return new ReducedProduct(left.bottom(), right.bottom());
    }

    @Override
    public boolean isBottom() {
        // Element in the domain is bottom if both left and right are bottom
        return this.left.isBottom() && this.right.isBottom();
    }

    @Override
    public boolean isTop() {
        // Element in the domain is top if both left and right are top
        return this.left.isTop() && this.right.isTop();
    }

    @Override
    protected ReducedProduct lubAux(ReducedProduct other) throws SemanticException {
        // Create new element in the domain where each element of the pair is a comparison between this and other element of the domain
        return new ReducedProduct(this.left.lub(other.left), this.right.lub(other.right));
    }

    @Override
    protected ReducedProduct wideningAux(ReducedProduct other) throws SemanticException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean lessOrEqualAux(ReducedProduct other) throws SemanticException {
        // TODO Auto-generated method stub
        return false;
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
        if (this.left == null && other.left != null) {
            return false;
        } else if (!this.left.equals(other.left)) {
            return false;
        }

        // Check if right field of this and other are not equal
        if (this.right == null && other.right != null) {
            return false;
        } else if(!this.right.equals(other.right)) {
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
        res = prime * res + ((this.left == null) ? 0 : this.left.hashCode());

        // Calculate result using hash code of right (Parity domain)
        res = prime * res + ((this.right == null) ? 0 : this.right.hashCode());

        return res;
    }

}
