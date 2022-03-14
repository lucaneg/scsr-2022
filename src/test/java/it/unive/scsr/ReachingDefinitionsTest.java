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
		Program program3 = IMPFrontend.processFile("inputs/reaching-definitions3.imp");
		Program program4 = IMPFrontend.processFile("inputs/reaching-definitions4.imp");
		Program program5 = IMPFrontend.processFile("inputs/reaching-definitions5.imp");
		Program program6 = IMPFrontend.processFile("inputs/reaching-definitions6.imp");
		
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
		lisa.run(program2);
		lisa.run(program3);
		lisa.run(program4);
		lisa.run(program5);
		lisa.run(program6);
	}
}
