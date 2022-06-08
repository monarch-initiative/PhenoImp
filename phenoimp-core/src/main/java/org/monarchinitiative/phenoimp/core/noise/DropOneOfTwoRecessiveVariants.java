package org.monarchinitiative.phenoimp.core.noise;

import org.monarchinitiative.phenoimp.core.PhenoImpRuntimeException;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.constants.hpo.HpoModeOfInheritanceTermIds;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Disease;
import org.phenopackets.schema.v2.core.Interpretation;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class expects to get a phenopacket with a single disease with AR mode of inheritance and two variant
 * interpretations associated with the disease. One of the variant interpretations is randomly removed.
 * <p>
 * The class returns unchanged/original phenopacket if any of the following conditions is met:
 * <ul>
 *     <li>The number of present diseases is not equal to <code>1</code>.</li>
 *     <li>The variant interpretations have non-unique ID. Note this can also happen if the IDs are unassigned as
 *     protobuf uses <code>""</code> by default for <code>str</code> fields.</li>
 *     <li>The disease is not known/present in among {@link #diseases}.</li>
 *     <li>The number of variant interpretations associated with the disease is not equal to <code>2</code></li>
 * </ul>
 * The unmet condition is logged as a warning.
 */
public class DropOneOfTwoRecessiveVariants implements PhenopacketNoise<Phenopacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropOneOfTwoRecessiveVariants.class);

    private final Ontology hpo;
    private final HpoDiseases diseases;
    private final Random random;

    public DropOneOfTwoRecessiveVariants(Ontology hpo, HpoDiseases diseases) {
        this(hpo, diseases, Instant.now().getEpochSecond());
    }

    public DropOneOfTwoRecessiveVariants(Ontology hpo, HpoDiseases diseases, long randomSeed) {
        this.hpo = Objects.requireNonNull(hpo);
        this.diseases = Objects.requireNonNull(diseases);
        this.random = new Random(randomSeed);
    }

    @Override
    public Phenopacket distort(Phenopacket pp) {
        List<Disease> observedDiseases = pp.getDiseasesList().stream()
                .filter(d -> !d.getExcluded())
                .toList();

        if (observedDiseases.size() != 1) {
            LOGGER.warn("The disease count {}!=1, not removing of one of the recessive variants in {}", observedDiseases.size(), pp.getId());
            return pp;
        }

        // Check the interpretations have IDs unique within the phenopacket.
        Map<String, Long> ids = pp.getInterpretationsList().stream()
                .collect(Collectors.groupingBy(Interpretation::getId, Collectors.counting()));
        boolean hasNonUniqueIds = ids.values().stream().anyMatch(count -> count != 1L);
        if (hasNonUniqueIds) {
            String nonUniqueIds = ids.entrySet().stream()
                    .filter(e -> e.getValue() != 1L)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining("', '", "'", "'"));
            LOGGER.warn("Non-unique interpretation ID(s) {}, not removing one of the recessive variants in {}", nonUniqueIds, pp.getId());
            return pp;
        }

        // Find the disease.
        OntologyClass diseaseIdPp = pp.getDiseases(0).getTerm();
        TermId diseaseId = parseDiseaseId(diseaseIdPp);
        Optional<HpoDisease> diseaseOptional = diseases.diseaseById(diseaseId);
        if (diseaseOptional.isEmpty()) {
            LOGGER.warn("Unknown disease {} ({}), not removing one of the recessive variants in {}", diseaseIdPp.getLabel(), diseaseIdPp.getId(), pp.getId());
            return pp;
        }

        // Process the disease.
        boolean hasAR = diseaseHasArModeOfInheritance(diseaseOptional.get());
        if (hasAR) {
            List<Interpretation> relevantInterpretations = pp.getInterpretationsList().stream()
                    .filter(i -> i.getDiagnosis().getDisease().getId().equals(diseaseIdPp.getId()))
                    .toList();
            if (relevantInterpretations.size() != 2) {
                LOGGER.warn("The number of variant interpretation relevant to {} ({}) {}!=2, not removing one of the recessive variants in {}",
                        diseaseIdPp.getLabel(),
                        diseaseIdPp.getId(),
                        relevantInterpretations.size(),
                        pp.getId());
                return pp;
            }

            String idOfTheInterpretationToBeRemoved = relevantInterpretations.get(random.nextInt(pp.getInterpretationsCount())).getId();
            List<Interpretation> passingInterpretations = pp.getInterpretationsList().stream()
                    .filter(i -> !i.getId().equals(idOfTheInterpretationToBeRemoved))
                    .toList();

            // Remove random interpretation.
            return pp.toBuilder()
                    .clearInterpretations()
                    .addAllInterpretations(passingInterpretations)
                    .build();
        } else
            return pp; // Nothing to be done in non-AR disease.
    }

    private boolean diseaseHasArModeOfInheritance(HpoDisease disease) {
        for (TermId moi : disease.modesOfInheritance()) {
            Set<TermId> ancestors = hpo.getAncestorTermIds(moi);
            if (ancestors.contains(HpoModeOfInheritanceTermIds.AUTOSOMAL_RECESSIVE)) {
                return true;
            }
        }
        return false;
    }

    private static TermId parseDiseaseId(OntologyClass diseaseId) {
        try {
            return TermId.of(diseaseId.getId());
        } catch (PhenolRuntimeException e) {
            throw new PhenoImpRuntimeException(e);
        }
    }
}
