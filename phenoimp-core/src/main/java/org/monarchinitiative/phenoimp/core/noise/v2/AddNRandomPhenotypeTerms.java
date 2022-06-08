package org.monarchinitiative.phenoimp.core.noise.v2;

import org.monarchinitiative.phenoimp.core.noise.base.BaseAddNRandomPhenotypeTerms;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that adds <em>n</em> random phenotypic abnormalities to given phenopacket.
 */
public class AddNRandomPhenotypeTerms extends BaseAddNRandomPhenotypeTerms<Phenopacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddNRandomPhenotypeTerms.class);

    /**
     * Create an instance with randomness seeded by the current epoch seconds.
     *
     * @param ontology ontology to use.
     * @param numberOfTermsToAdd number of terms to add to the phenopacket.
     */
    public AddNRandomPhenotypeTerms(Ontology ontology, int numberOfTermsToAdd) {
        this(ontology, numberOfTermsToAdd, Instant.now().getEpochSecond());
    }

    /**
     * Create an instance with randomness seeded by provided seed.
     *
     * @param ontology ontology to use.
     * @param numberOfTermsToAdd number of terms to add to the phenopacket.
     * @param randomSeed random seed
     */
    public AddNRandomPhenotypeTerms(Ontology ontology, int numberOfTermsToAdd, long randomSeed) {
        super(ontology, numberOfTermsToAdd, randomSeed);
    }

    @Override
    public Phenopacket distort(Phenopacket pp) {
        Set<TermId> presentTermIds = pp.getPhenotypicFeaturesList().stream()
                .filter(pf -> !pf.getExcluded())
                .map(PhenotypicFeature::getType)
                .map(toTermId())
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(HashSet::new));

        List<Term> randomTerms = selectRandomTerms(presentTermIds);

        return Phenopacket.newBuilder(pp)
                .addAllPhenotypicFeatures(toPhenotypicFeatures(randomTerms))
                .build();
    }

    private static Function<OntologyClass, Optional<TermId>> toTermId() {
        return id -> {
            try {
                return Optional.of(TermId.of(id.getId()));
            } catch (PhenolRuntimeException e) {
                LOGGER.warn("Dropping phenotype feature due to non-parsable ID: '{}'", id.getId());
                return Optional.empty();
            }
        };
    }

    private static Iterable<? extends PhenotypicFeature> toPhenotypicFeatures(List<Term> randomTerms) {
        return randomTerms.stream()
                .map(termToPhenotypicFeature())
                .toList();
    }

    private static Function<Term, PhenotypicFeature> termToPhenotypicFeature() {
        return term -> PhenotypicFeatureBuilder.builder(term.id().getValue(), term.getName()).build();
    }

}
