package it.unive.scsr.ReducedProduct;

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

public class IntervalParityTest {

	@Test
	public void testIntervalParity() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/reduced-product.imp");
		
		LiSAConfiguration conf = new LiSAConfiguration();
		conf.setJsonOutput(true);
		conf.setDumpAnalysis(true);
		conf.setWorkdir("outputs/reduced-product");
		conf.setAbstractState(
				new SimpleAbstractState<>(
						new MonolithicHeap(), // THIS IS THE HEAP DOMAIN 
						new ValueEnvironment<>(new IntervalParity()), // THIS IS THE VALUE DOMAIN 
						new TypeEnvironment<>(new InferredTypes())) // DOMAIN FOR TYPE ANALYSIS
				);
		
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
}