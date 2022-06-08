package org.monarchinitiative.phenoimp.core.noise.v1;

import org.monarchinitiative.phenoimp.core.noise.base.BaseAddNRandomPhenotypeTerms;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.PhenotypicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AddNRandomPhenotypeTerms extends BaseAddNRandomPhenotypeTerms<Phenopacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(org.monarchinitiative.phenoimp.core.noise.v2.AddNRandomPhenotypeTerms.class);


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
                .filter(pf -> !pf.getNegated())
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
        return term -> PhenotypicFeature.newBuilder()
                .setType(OntologyClass.newBuilder()
                        .setId(term.id().getValue())
                        .setLabel(term.getName())
                        .build())
                .build();
    }
}
