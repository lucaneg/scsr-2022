package it.unive.scsr;

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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
