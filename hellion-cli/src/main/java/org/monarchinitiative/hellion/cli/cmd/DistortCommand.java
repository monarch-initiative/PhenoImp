package org.monarchinitiative.hellion.cli.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "distort",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        description = "Distort a phenopacket.")
public class DistortCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistortCommand.class);

    @CommandLine.Option(names = {"-d", "--data"},
            paramLabel = "path/to/datadir",
            description = "Path to data directory prepared by `download` command (default: ${DEFAULT-VALUE}).")
    public Path dataDirectory = Path.of("data");

    @CommandLine.Option(names = {"--random-seed"},
            paramLabel = "123",
            description = "Seed for pseudorandom number generator (default: current UNIX epoch second).")
    public Long randomSeed;

    @CommandLine.Option(names = {"--add-n-random-terms"},
            paramLabel = "2",
            description = "Add given number of random phenotype terms (default: ${DEFAULT-VALUE}).")
    public int nRandomTerms = 0;

    @CommandLine.Option(names = {"--drop-ar-variant"},
            description = "Drop one of variant interpretations associated with disease with autosomal recessive inheritance (default: ${DEFAULT-VALUE}).")
    public boolean dropVariantInAutosomalRecessiveCase = false;

    @CommandLine.Option(names = {"--approximate"},
            paramLabel = "{OFF, PARENT, GRANDPARENT}",
    description = "Replace each phenotype term with its parent or grandparent (default: ${DEFAULT-VALUE}).")
    public TermApproximation ancestor = TermApproximation.OFF;

    @CommandLine.Option(names = {"-i", "--input"},
            required = true,
            description = "Path to input phenopacket.")
    public Path phenopacket;


    @Override
    public Integer call() {
         // TODO - implement
        return 0;
    }

    public enum TermApproximation {
        OFF,
        PARENT,
        GRANDPARENT
    }
}
