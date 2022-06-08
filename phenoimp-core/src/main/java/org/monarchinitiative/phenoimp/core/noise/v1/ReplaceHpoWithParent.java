package org.monarchinitiative.phenoimp.core.noise.v1;

import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
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

import static org.monarchinitiative.phenol.constants.hpo.HpoSubOntologyRootTermIds.PHENOTYPIC_ABNORMALITY;

/**
 * A class for adding noise by replacing each phenotype term with a less specific term.
 * <p>
 * The decrease of specificity is determined by setting the {@link #nHops} parameter. Each term will be replaced with its
 * parent term if <code>nHops == 1</code>, with grandparent term if <code>nHops == 2</code>, and so on.
 * <p>
 * If the term has multiple parents, a pseudo-random number generator is used to choose a parent term.
 */
public class ReplaceHpoWithParent implements PhenopacketNoise<Phenopacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceHpoWithParent.class);

    // HPO
    private final Ontology hpo;

    /**
     * Number of hops to make when searching for a less specific term.
     */
    private final int nHops;
    private final Random random;

    /**
     * Get a hellion instance seeded by the current epoch second.
     *
     * @param hpo HPO ontology.
     * @param nHops number of hops to apply when searching for a less specific HPO term.
     */
    public ReplaceHpoWithParent(Ontology hpo, int nHops) {
        this(hpo, nHops, Instant.now().getEpochSecond());
    }

    /**
     * Get a hellion instance seeded by the given seed.
     *
     * @param hpo HPO ontology.
     * @param nHops number of hops to apply when searching for a less specific HPO term.
     *
     */
    public ReplaceHpoWithParent(Ontology hpo, int nHops, long randomSeed) {
        this.hpo = Objects.requireNonNull(hpo);
        if (nHops <= 0) {
            throw new IllegalArgumentException("Number of hops must be positive, got %d".formatted(nHops));
        }
        this.nHops = nHops;
        this.random = new Random(randomSeed);
    }

    @Override
    public Phenopacket distort(Phenopacket pp) {
        List<PhenotypicFeature> features = pp.getPhenotypicFeaturesList().stream()
                .map(this::processPhenotypicFeature)
                .flatMap(Optional::stream)
                .toList();

        return pp.toBuilder()
                .clearPhenotypicFeatures()
                .addAllPhenotypicFeatures(features)
                .build();
    }

    private Optional<PhenotypicFeature> processPhenotypicFeature(PhenotypicFeature pf) {
        Optional<TermId> termIdOptional = extractTermIdFromPhenotypicFeature(pf);
        if (termIdOptional.isEmpty())
            return Optional.empty();

        TermId termId = termIdOptional.get();

        if (!OntologyAlgorithm.existsPath(hpo, termId, PHENOTYPIC_ABNORMALITY)) {
            OntologyClass type = pf.getType();
            LOGGER.warn("Dropping phenotype feature {} ({}) that is not a subclass of Phenotypic abnormality ({})",
                    type.getLabel(), type.getId(), PHENOTYPIC_ABNORMALITY.getValue());
            return Optional.empty();
        } else if (termId.equals(PHENOTYPIC_ABNORMALITY)) {
            LOGGER.warn("Phenopacket contains phenotypic abnormality ({}) feature. Presence of this feature often adds nothing to the analysis.",
                    PHENOTYPIC_ABNORMALITY.getValue());
            return Optional.of(pf);
        }

        // Now hop upwards n times.
        TermId current = termId;
        for (int i = 0; i < nHops; i++) {
            List<TermId> parents = OntologyAlgorithm.getParentTerms(hpo, current, false).stream()
                    .filter(t -> !t.equals(PHENOTYPIC_ABNORMALITY))
                    .sorted(Comparator.comparing(TermId::getId))
                    .toList();
            if (parents.isEmpty()) {
                // This happens if we reached Phenotypic abnormality and in that case further hopping has no point.
                current = PHENOTYPIC_ABNORMALITY;
                break;
            }

            int idx = random.nextInt(parents.size());
            current = parents.get(idx);
        }

        Term term = hpo.getTermMap().get(current);
        if (term == null) {
            LOGGER.warn("Missing HPO term for ID {}. Please report the missing term to HPO developers.", current.getValue());
            return Optional.empty();
        }

        return Optional.of(pf.toBuilder()
                .setType(OntologyClass.newBuilder()
                        .setId(current.getValue())
                        .setLabel(term.getName()))
                .build());
    }

    private static Optional<TermId> extractTermIdFromPhenotypicFeature(PhenotypicFeature pf) {
        String id = pf.getType().getId();
        try {
            return Optional.of(TermId.of(id));
        } catch (PhenolRuntimeException e) {
            LOGGER.warn("Dropping phenotype feature due to non-parsable ID: '{}'", id);
            return Optional.empty();
        }
    }
}
