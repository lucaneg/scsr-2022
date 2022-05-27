package it.unive.scsr;

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

public class CharactersInclusionTest {
	
	@Test
	public void testCharactersInclusion() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/character-inclusion.imp");
		
		LiSAConfiguration conf = new LiSAConfiguration();
		conf.setJsonOutput(true);
		conf.setDumpAnalysis(true);
		conf.setWorkdir("outputs/character_inclusion");
		conf.setAbstractState(
				new SimpleAbstractState<>(
						new MonolithicHeap(), // THIS IS THE HEAP DOMAIN 
						new ValueEnvironment<>(new CharactersInclusion()), // THIS IS THE VALUE DOMAIN 
						new TypeEnvironment<>(new InferredTypes())) // DOMAIN FOR TYPE ANALYSIS
				);
		
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
	
}
