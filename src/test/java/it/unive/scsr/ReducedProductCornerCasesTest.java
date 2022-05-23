package it.unive.scsr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.imp.ParsingException;
import it.unive.scsr.ExtSignDomainSolution.Sign;

public class ReducedProductCornerCasesTest {

    @Test
    public void testCornerCasesReducedProduct() throws ParsingException, AnalysisException {
		
        ReducedProduct test = new ReducedProduct(new ExtSignDomainSolution(Sign.POS_OR_ZERO), Parity.ODD);
		test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.ODD);
        assertTrue(test.sign.getSign() == Sign.POS);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.BOTTOM), Parity.EVEN);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.BOTTOM);
        assertTrue(test.sign.getSign() == Sign.BOTTOM);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.POS_OR_ZERO), Parity.BOTTOM);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.BOTTOM);
        assertTrue(test.sign.getSign() == Sign.BOTTOM);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.POS_OR_ZERO), Parity.ODD);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.ODD);
        assertTrue(test.sign.getSign() == Sign.POS);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.NEG_OR_ZERO), Parity.ODD);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.ODD);
        assertTrue(test.sign.getSign() == Sign.NEG);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.ZERO), Parity.TOP);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.EVEN);
        assertTrue(test.sign.getSign() == Sign.ZERO);


        test = new ReducedProduct(new ExtSignDomainSolution(Sign.ZERO), Parity.ODD);
        test.parity = test.parityReduction(test);
        test.sign = test.signReduction(test);

        assertTrue(test.parity == Parity.BOTTOM);
        assertTrue(test.sign.getSign() == Sign.BOTTOM);
    }   
}
