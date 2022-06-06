package org.monarchinitiative.hellion.core;

import org.monarchinitiative.hellion.core.io.PhenopacketHellionDataResolver;
import org.monarchinitiative.hellion.core.noise.AddNRandomPhenotypeTerms;
import org.monarchinitiative.hellion.core.noise.DropOneOfTwoRecessiveVariants;
import org.monarchinitiative.hellion.core.noise.PhenopacketNoise;
import org.monarchinitiative.hellion.core.noise.ReplaceHpoWithParent;
import org.monarchinitiative.hellion.core.runner.SequentialHellionRunner;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaders;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HellionRunnerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HellionRunnerBuilder.class);

    private final Path dataDirectory;
    private int nRandomTerms = 0;

    private int nHops = 0;

    private boolean dropArVariant = false;

    private long randomSeed = Instant.now().getEpochSecond();

    HellionRunnerBuilder(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory);
    }

    public HellionRunnerBuilder addNRandomPhenotypeTerms(int nRandomTerms) {
        this.nRandomTerms = nRandomTerms;
        return this;
    }

    public HellionRunnerBuilder nHopsForTermGeneralization(int nHops) {
        this.nHops = nHops;
        return this;
    }

    public HellionRunnerBuilder dropOneOfTwoRecessiveVariants(boolean dropArVariant) {
        this.dropArVariant = dropArVariant;
        return this;
    }

    public HellionRunnerBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public synchronized HellionRunner build() {
        // 0 - Check the input arguments.
        List<String> errors = new LinkedList<>();

        if (nHops < 0)
            errors.add("Number of hops must be non-negative, got %s.".formatted(nHops));

        if (nRandomTerms < 0)
            errors.add("Number of random phenotype terms must be non-negative, got %s.".formatted(nRandomTerms));

        if (!errors.isEmpty()) {
            String missing = errors.stream().collect(Collectors.joining("', '", "'", "'"));
            String message = String.format("Cannot run the distortion: %s.", missing);
            throw new PhenopacketHellionRuntimeException(message);
        }

        // 1 - Load resources.
        PhenopacketHellionDataResolver dataResolver = new PhenopacketHellionDataResolver(dataDirectory);
        LOGGER.info("Loading HPO from {}.", dataResolver.hpJsonPath().toAbsolutePath());
        Ontology hpo = OntologyLoader.loadOntology(dataResolver.hpJsonPath().toFile());


        List<PhenopacketNoise> noise = new ArrayList<>();
        LOGGER.info("Using {} as the random seed.", randomSeed);

        // 2 - Replace with parents or grandparents.
        if (nHops > 0) {
            LOGGER.info("Replacing each phenotype term with ancestor {} hops upstream.", nHops);
            ReplaceHpoWithParent replaceHpoWithParent = new ReplaceHpoWithParent(hpo, nHops, randomSeed);
            noise.add(replaceHpoWithParent);
        }

        // 3 - Add n random terms.
        if (nRandomTerms > 0) {
            LOGGER.info("Adding {} random phenotype terms.", nRandomTerms);
            AddNRandomPhenotypeTerms addNRandomPhenotypeTerms = new AddNRandomPhenotypeTerms(hpo, nRandomTerms, randomSeed);
            noise.add(addNRandomPhenotypeTerms);
        }

        // 4 - Drop random variant for AR diseases.
        if (dropArVariant) {
            LOGGER.info("Dropping random variant for diseases segregating with autosomal recessive mode of inheritance.");
            HpoDiseases diseases;
            try {
                diseases = loadHpoDiseases(hpo, dataResolver.hpoAnnotationPath());
            } catch (IOException e) {
                throw new PhenopacketHellionRuntimeException(e);
            }
            DropOneOfTwoRecessiveVariants dropOneOfTwoRecessiveVariants = new DropOneOfTwoRecessiveVariants(hpo, diseases, randomSeed);
            noise.add(dropOneOfTwoRecessiveVariants);
        }

        return new SequentialHellionRunner(noise);
    }

    private static HpoDiseases loadHpoDiseases(Ontology hpo, Path hpoAssociation) throws IOException {
        LOGGER.info("Loading HPO disease annotations from {}.", hpoAssociation.toAbsolutePath());
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(hpo, HpoDiseaseLoaderOptions.defaultOptions());
        return loader.load(hpoAssociation);
    }

}
