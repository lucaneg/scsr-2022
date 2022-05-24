package it.unive.scsr.final_project;
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
import org.junit.Test;

public class ExtSignParityDomainTest {

    @Test
    public void testExtSignParity() throws ParsingException, AnalysisException {
        Program testProgram = IMPFrontend.processFile("inputs/final_project/extSignParityDomain.imp");

        LiSAConfiguration config = new LiSAConfiguration();
        config.setDumpAnalysis(true);
        config.setJsonOutput(true);
        config.setWorkdir("outputs/final_project/extSignParity");
        config.setAbstractState(
                new SimpleAbstractState<>(
                        new MonolithicHeap(),
                        new ValueEnvironment<>(new ExtSignParityDomain()),
                        new TypeEnvironment<>(new InferredTypes()))
        );

        LiSA lisa = new LiSA(config);
        lisa.run(testProgram);
    }

}
