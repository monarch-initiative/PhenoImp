package org.monarchinitiative.phenoimp.cli.cmd;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.monarchinitiative.phenoimp.configuration.PhenoImpBuilder;
import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.PhenoImp;
import org.monarchinitiative.phenoimp.core.PhenoImpRuntimeException;
import org.monarchinitiative.phenoimp.core.PhenopacketVersion;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            paramLabel = "0",
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
            description = "Path to input phenopacket in JSON format.")
    public Path phenopacket;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Where to write the distorted phenopacket %n  (default: the input name + \"distorted\", %n  e.g. \"input.json\" -> \"input.distorted.json\").")
    public Path output = null;

    @Override
    public Integer call() {
        try {
            // 0 - Read input phenopacket.
            Phenopacket pp = readPhenopacket(phenopacket);

            // 1 - Bootstrap the runner.
            PhenoImpBuilder builder = PhenoImpBuilder.builder(dataDirectory)
                    .addNRandomPhenotypeTerms(nRandomTerms)
                    .dropOneOfTwoRecessiveVariants(dropVariantInAutosomalRecessiveCase);

            if (randomSeed != null)
                builder.setRandomSeed(randomSeed);

            switch (ancestor) {
                case OFF -> {
                }
                case PARENT -> builder.nHopsForTermGeneralization(1);
                case GRANDPARENT -> builder.nHopsForTermGeneralization(2);
            }

            PhenoImp phenoImp = builder.build();
            DistortionRunner runner = phenoImp.forPhenopacket(PhenopacketVersion.V2).get();

            // 2 - Distort the phenopacket.
            Message distorted = runner.run(pp);

            // 3 - Write out the distorted phenopacket.
            Path output = prepareOutputPath(this.output, this.phenopacket);
            writePhenopacket(distorted, output);

            LOGGER.info("We're done here, bye!");
            return 0;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return 1;
        }
    }

    private static Phenopacket readPhenopacket(Path phenopacket) throws IOException {
        // `phenopacket` should not be null as it is a required parameter.
        LOGGER.info("Reading input phenopacket from {}", phenopacket.toAbsolutePath());

        JsonFormat.Parser parser = JsonFormat.parser();
        Phenopacket.Builder builder = Phenopacket.newBuilder();
        try (BufferedReader reader = Files.newBufferedReader(phenopacket)) {
            parser.merge(reader, builder);
        }

        return builder.build();
    }

    private static void writePhenopacket(Message distorted, Path output) throws IOException {
        LOGGER.info("Writing distorted phenopacket to {}", output.toAbsolutePath());
        JsonFormat.Printer printer = JsonFormat.printer();
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            printer.appendTo(distorted, writer);
        }
    }

    private static Path prepareOutputPath(Path output, Path phenopacket) {
        if (output == null) {
            Pattern pt = Pattern.compile("(?<name>\\w+)\\.json");
            String name = phenopacket.toFile().getName();
            Matcher matcher = pt.matcher(name);
            String base;
            if (matcher.matches())
                base = matcher.group("name");
            else
                throw new PhenoImpRuntimeException("The input file name '%s' does not match '\\w+\\.json' pattern!".formatted(phenopacket.toAbsolutePath()));
            Path parent = phenopacket.getParent();
            return parent.resolve("%s.distorted.json".formatted(base));
        }
        return output;
    }

    public enum TermApproximation {
        OFF,
        PARENT,
        GRANDPARENT
    }
}
