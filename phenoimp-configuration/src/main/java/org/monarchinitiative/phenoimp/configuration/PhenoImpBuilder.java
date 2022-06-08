package org.monarchinitiative.phenoimp.configuration;

import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.PhenoImp;
import org.monarchinitiative.phenoimp.core.PhenoImpRuntimeException;
import org.monarchinitiative.phenoimp.core.PhenopacketVersion;
import org.monarchinitiative.phenoimp.core.noise.v2.AddNRandomPhenotypeTerms;
import org.monarchinitiative.phenoimp.core.noise.v2.DropOneOfTwoRecessiveVariants;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.monarchinitiative.phenoimp.core.noise.v2.ReplaceHpoWithParent;
import org.monarchinitiative.phenoimp.core.runner.SequentialV2DistortionRunner;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaders;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class PhenoImpBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhenoImpBuilder.class);

    private final PhenoImpDataResolver dataResolver;

    private final Ontology hpo;
    private volatile HpoDiseases diseases = null;
    private int nRandomTerms = 0;

    private int nHops = 0;

    private boolean dropArVariant = false;

    private long randomSeed = Instant.now().getEpochSecond();

    public static PhenoImpBuilder builder(Path dataDirectory) throws PhenoImpConfigurationException {
        return new PhenoImpBuilder(dataDirectory);
    }

    private PhenoImpBuilder(Path dataDirectory)  {
        this.dataResolver = new PhenoImpDataResolver(Objects.requireNonNull(dataDirectory));
        LOGGER.info("Loading HPO from {}.", dataResolver.hpJsonPath().toAbsolutePath());
        this.hpo = OntologyLoader.loadOntology(dataResolver.hpJsonPath().toFile());
    }

    public PhenoImpBuilder addNRandomPhenotypeTerms(int nRandomTerms) {
        this.nRandomTerms = nRandomTerms;
        return this;
    }

    public PhenoImpBuilder nHopsForTermGeneralization(int nHops) {
        this.nHops = nHops;
        return this;
    }

    public PhenoImpBuilder dropOneOfTwoRecessiveVariants(boolean dropArVariant) {
        this.dropArVariant = dropArVariant;
        return this;
    }

    public PhenoImpBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }


    public PhenoImp build() throws PhenoImpConfigurationException {
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

        LOGGER.info("Using {} as the random seed.", randomSeed);

        // 1 - Build distortion runners.
        Map<PhenopacketVersion, DistortionRunner> runnerMap = new HashMap<>();

        DistortionRunner v2Runner = buildV2DistortionRunner(hpo);
        runnerMap.put(PhenopacketVersion.V2, v2Runner);

        // 2 - Wrap up
        return new PhenoImpImpl(runnerMap);
    }

    private DistortionRunner buildV2DistortionRunner(Ontology hpo) {
        List<PhenopacketNoise<Phenopacket>> noise = new ArrayList<>();
        // 0 - Replace with parents or grandparents.
        if (nHops > 0) {
            LOGGER.info("Replacing each phenotype term with ancestor {} hops upstream.", nHops);
            ReplaceHpoWithParent replaceHpoWithParent = new ReplaceHpoWithParent(hpo, nHops, randomSeed);
            noise.add(replaceHpoWithParent);
        }

        // 1 - Add n random terms.
        if (nRandomTerms > 0) {
            LOGGER.info("Adding {} random phenotype terms.", nRandomTerms);
            AddNRandomPhenotypeTerms addNRandomPhenotypeTerms = new AddNRandomPhenotypeTerms(hpo, nRandomTerms, randomSeed);
            noise.add(addNRandomPhenotypeTerms);
        }

        // 2 - Drop random variant for AR diseases.
        if (dropArVariant) {
            LOGGER.info("Dropping random variant for diseases segregating with autosomal recessive mode of inheritance.");
            if (diseases == null) {
                synchronized (this) {
                    if (diseases == null) {
                        try {
                            diseases = loadHpoDiseases(hpo, dataResolver.hpoAnnotationPath());
                        } catch (IOException e) {
                            throw new PhenoImpRuntimeException(e);
                        }
                    }
                }
            }

            DropOneOfTwoRecessiveVariants dropOneOfTwoRecessiveVariants = new DropOneOfTwoRecessiveVariants(hpo, diseases, randomSeed);
            noise.add(dropOneOfTwoRecessiveVariants);
        }

        return new SequentialV2DistortionRunner(noise);
    }

    private static HpoDiseases loadHpoDiseases(Ontology hpo, Path hpoAssociation) throws IOException {
        LOGGER.info("Loading HPO disease annotations from {}.", hpoAssociation.toAbsolutePath());
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(hpo, HpoDiseaseLoaderOptions.defaultOptions());
        return loader.load(hpoAssociation);
    }

}
