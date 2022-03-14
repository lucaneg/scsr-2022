package it.unive.scsr;

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
import org.junit.Test;

public class AvailableExpressionsTest {

    @Test
    public void testAvailableExpressions() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/available-expressions.imp");

        LiSAConfiguration conf = new LiSAConfiguration();
        conf.setJsonOutput(true);
        conf.setDumpAnalysis(true);
        conf.setWorkdir("outputs");
        conf.setAbstractState(
                new SimpleAbstractState<>(
                        new MonolithicHeap(),
                        new DefiniteForwardDataflowDomain<>(new AvailableExpressions()), // THIS IS THE DOMAIN THAT WE WANT TO EXECUTE
                        new TypeEnvironment<>(new InferredTypes()))
        );

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

}
