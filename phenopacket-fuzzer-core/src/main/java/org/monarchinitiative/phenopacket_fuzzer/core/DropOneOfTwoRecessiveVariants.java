package org.monarchinitiative.phenopacket_fuzzer.core;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.constants.hpo.HpoModeOfInheritanceTermIds;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Disease;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class DropOneOfTwoRecessiveVariants implements PhenopacketFuzzer {

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
    public Phenopacket fuzz(Phenopacket pp) {
        List<Disease> observedDiseases = pp.getDiseasesList().stream()
                .filter(d -> !d.getExcluded())
                .toList();

        if (observedDiseases.size() != 1) {
            LOGGER.info("Not removing of one of the recessive variants in {} as the disease count {}!=1", pp.getId(), observedDiseases.size());
            return pp;
        }


        OntologyClass diseaseIdPp = pp.getDiseases(0).getTerm();
        TermId diseaseId = parseDiseaseId(diseaseIdPp);
        Optional<HpoDisease> diseaseOptional = diseases.diseaseById(diseaseId);
        if (diseaseOptional.isEmpty()) {
            LOGGER.warn("Unknown disease {} ({}), not removing one of the recessive variants in {}", diseaseIdPp.getLabel(), diseaseIdPp.getId(), pp.getId());
            return pp;
        }

        boolean hasAR = diseaseHasArModeOfInheritance(diseaseOptional.get());
        if (hasAR) {
            if (pp.getInterpretationsCount() != 2) {
                LOGGER.info("Variant interpretation count {}!=2, not removing one of the recessive variants in {}", pp.getInterpretationsCount(), pp.getId());
                return pp;
            }

            // Remove random interpretation.
            return pp.toBuilder()
                    .removeInterpretations(random.nextInt(pp.getInterpretationsCount()))
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
            throw new PhenopacketFuzzerRuntimeException(e);
        }
    }
}
