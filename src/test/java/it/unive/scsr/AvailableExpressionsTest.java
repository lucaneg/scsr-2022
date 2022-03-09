package it.unive.scsr;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;

public class AvailableExpressionsTest {
    
    @Test
    public void testAvailableExpressions() throws ParsingException, AnalysisException {

        // Set vars
        Program program = IMPFrontend.processFile("inputs/available-expressions.imp");
        LiSAConfiguration conf = new LiSAConfiguration();

        // Set LiSA config
        conf.setJsonOutput(true);
        conf.setDumpAnalysis(true);
        conf.setWorkdir("outputs");
        conf.setAbstractState(
            new SimpleAbstractState<>(
                new MonolithicHeap(),
                new DefiniteForwardDataflowDomain<>(new AvailableExpressions()),
                new TypeEnvironment<>(new InferredTypes())
            )
        );

        // Set LiSA and run
        LiSA lisa = new LiSA(conf);
        lisa.run(program);

    }

}
