package org.monarchinitiative.phenoimp.core;

import org.monarchinitiative.phenoimp.core.io.PhenoImpDataResolver;
import org.monarchinitiative.phenoimp.core.noise.AddNRandomPhenotypeTerms;
import org.monarchinitiative.phenoimp.core.noise.DropOneOfTwoRecessiveVariants;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.monarchinitiative.phenoimp.core.noise.ReplaceHpoWithParent;
import org.monarchinitiative.phenoimp.core.runner.SequentialDistortionRunner;
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

public class DistortionRunnerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistortionRunnerBuilder.class);

    private final Path dataDirectory;
    private int nRandomTerms = 0;

    private int nHops = 0;

    private boolean dropArVariant = false;

    private long randomSeed = Instant.now().getEpochSecond();

    DistortionRunnerBuilder(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory);
    }

    public DistortionRunnerBuilder addNRandomPhenotypeTerms(int nRandomTerms) {
        this.nRandomTerms = nRandomTerms;
        return this;
    }

    public DistortionRunnerBuilder nHopsForTermGeneralization(int nHops) {
        this.nHops = nHops;
        return this;
    }

    public DistortionRunnerBuilder dropOneOfTwoRecessiveVariants(boolean dropArVariant) {
        this.dropArVariant = dropArVariant;
        return this;
    }

    public DistortionRunnerBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public synchronized DistortionRunner build() {
        // 0 - Check the input arguments.
        List<String> errors = new LinkedList<>();

        if (nHops < 0)
            errors.add("Number of hops must be non-negative, got %s.".formatted(nHops));

        if (nRandomTerms < 0)
            errors.add("Number of random phenotype terms must be non-negative, got %s.".formatted(nRandomTerms));

        if (!errors.isEmpty()) {
            String missing = errors.stream().collect(Collectors.joining("', '", "'", "'"));
            String message = String.format("Cannot run the distortion: %s.", missing);
            throw new PhenoImpRuntimeException(message);
        }

        // 1 - Load resources.
        PhenoImpDataResolver dataResolver = new PhenoImpDataResolver(dataDirectory);
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
                throw new PhenoImpRuntimeException(e);
            }
            DropOneOfTwoRecessiveVariants dropOneOfTwoRecessiveVariants = new DropOneOfTwoRecessiveVariants(hpo, diseases, randomSeed);
            noise.add(dropOneOfTwoRecessiveVariants);
        }

        return new SequentialDistortionRunner(noise);
    }

    private static HpoDiseases loadHpoDiseases(Ontology hpo, Path hpoAssociation) throws IOException {
        LOGGER.info("Loading HPO disease annotations from {}.", hpoAssociation.toAbsolutePath());
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(hpo, HpoDiseaseLoaderOptions.defaultOptions());
        return loader.load(hpoAssociation);
    }

}
