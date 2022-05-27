package org.monarchinitiative.phenopacket_fuzzer.core;

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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AddNRandomPhenotypeTerms implements PhenopacketFuzzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddNRandomPhenotypeTerms.class);

    private final Ontology ontology;
    private final int numberOfTermsToAdd;
    private final Random random;

    public AddNRandomPhenotypeTerms(Ontology ontology, int numberOfTermsToAdd, long randomSeed) {
        this.ontology = Objects.requireNonNull(ontology).subOntology(HpoSubOntologyRootTermIds.PHENOTYPIC_ABNORMALITY);
        this.numberOfTermsToAdd = numberOfTermsToAdd;
        this.random = new Random(randomSeed);
    }

    @Override
    public Phenopacket fuzz(Phenopacket pp) {
        /*
        Get existing terms.

         */
        Set<TermId> presentFeatures = pp.getPhenotypicFeaturesList().stream()
                .filter(pf -> !pf.getExcluded())
                .map(PhenotypicFeature::getType)
                .map(toTermId())
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        Set<TermId> nonObsoleteTermIds = new HashSet<>(ontology.getNonObsoleteTermIds());
        nonObsoleteTermIds.remove(HpoSubOntologyRootTermIds.PHENOTYPIC_ABNORMALITY);

        TermId[] allPhenotypeFeatures = nonObsoleteTermIds.stream()
                .sorted(Comparator.comparing(TermId::getValue))
                .toArray(TermId[]::new);
        List<Term> randomTerms = new ArrayList<>(numberOfTermsToAdd);

        int i = 0;
        while (i < numberOfTermsToAdd) {
            int index = random.nextInt(allPhenotypeFeatures.length);
            TermId id = allPhenotypeFeatures[index];
            if (presentFeatures.contains(id))
                // TODO - should we care if we're adding a parent/child term?
                // we should not add already present term
                continue;

            presentFeatures.add(id); // Ensure we do not choose the same term twice
            Term term = ontology.getTermMap().get(id);
            randomTerms.add(term);
            i++;
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

    private Iterable<? extends PhenotypicFeature> toPhenotypicFeatures(List<Term> randomTerms) {
        return randomTerms.stream()
                .map(termToPhenotypicFeature())
                .toList();
    }

    private static Function<Term, PhenotypicFeature> termToPhenotypicFeature() {
        return term -> PhenotypicFeatureBuilder.builder(term.id().getValue(), term.getName()).build();
    }

}
