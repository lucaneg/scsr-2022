package it.unive.scsr;

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
import it.unive.lisa.outputs.JsonReport;
import it.unive.lisa.outputs.compare.JsonReportComparer;
import it.unive.lisa.program.Program;
import it.unive.scsr.project.BricksDomain;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BricksDomainEvaluation {

    @Test
    public void testBricksDomain() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/bricks-eval.imp");

        LiSAConfiguration conf = new LiSAConfiguration();
        conf.setJsonOutput(true);
        conf.setDumpAnalysis(true);
        conf.setWorkdir("outputs/bricksdomain");
        conf.setAbstractState(
                new SimpleAbstractState<>(
                        new MonolithicHeap(),
                        new ValueEnvironment<>(new BricksDomain()),
                        new TypeEnvironment<>(new InferredTypes())));

        LiSA lisa = new LiSA(conf);
        lisa.run(program);

    }

}
