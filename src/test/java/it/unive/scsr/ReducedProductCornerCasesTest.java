package it.unive.scsr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;
import it.unive.scsr.ExtSignDomainSolution.Sign;

public class ReducedProductCornerCasesTest {

    @Test
    public void testCornerCasesReducedProduct() throws ParsingException, AnalysisException {
		
        ReducedProduct test = new ReducedProduct(new ExtSignDomainSolution(Sign.POS_OR_ZERO), new Parity((byte) 2));
		test.parity = test.refineParity(test);
        test.sign = test.refineSign(test);

        assertTrue(test.parity.equals(new Parity((byte) 2)));
        assertTrue(test.sign.equals(new ExtSignDomainSolution(Sign.POS)));
    }   
}
