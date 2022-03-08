package it.unive.scsr;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;

public class ReachingDefinitionsTest {

	@Test
	public void testReachingDefinitions() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/reaching-definitions.imp");
		
		LiSAConfiguration conf = new LiSAConfiguration();
		conf.setJsonOutput(true);
		conf.setDumpAnalysis(true);
		conf.setWorkdir("outputs");
		conf.setAbstractState(
				new SimpleAbstractState<>(
						new MonolithicHeap(), 
						new PossibleForwardDataflowDomain<>(new ReachingDefinitions()), // THIS IS THE DOMAIN THAT WE WANT TO EXECUTE
						new TypeEnvironment<>(new InferredTypes()))
				);
		
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
}
