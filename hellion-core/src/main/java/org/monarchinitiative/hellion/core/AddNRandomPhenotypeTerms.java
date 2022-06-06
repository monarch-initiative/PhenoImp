package org.monarchinitiative.hellion.core;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.constants.hpo.HpoSubOntologyRootTermIds;
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
 * A phenopacket hellion that adds <em>n</em> random phenotypic abnormalities to given phenopacket.
 */
public class AddNRandomPhenotypeTerms implements PhenopacketHellion {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddNRandomPhenotypeTerms.class);
    private final RandomOntologyTermGenerator termGenerator;
    private final int numberOfTermsToAdd;

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
        Ontology phenotypicAbnormality = Objects.requireNonNull(ontology).subOntology(HpoSubOntologyRootTermIds.PHENOTYPIC_ABNORMALITY);
        this.termGenerator = new RandomOntologyTermGenerator(phenotypicAbnormality, randomSeed);
        this.numberOfTermsToAdd = numberOfTermsToAdd;
    }

    @Override
    public Phenopacket distort(Phenopacket pp) {
        Set<TermId> presentTermIds = pp.getPhenotypicFeaturesList().stream()
                .filter(pf -> !pf.getExcluded())
                .map(PhenotypicFeature::getType)
                .map(toTermId())
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(HashSet::new));

        List<Term> randomTerms = new ArrayList<>(numberOfTermsToAdd);

        while (randomTerms.size() < numberOfTermsToAdd && termGenerator.hasNext()) {
            Term term = termGenerator.next();
            if (presentTermIds.contains(term.id()))
                // we should not add already present term
                // TODO - should we care if we're adding a parent/child of an already existing term?
                continue;

            presentTermIds.add(term.id()); // Ensure we do not choose the same term twice
            randomTerms.add(term);
        }

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
