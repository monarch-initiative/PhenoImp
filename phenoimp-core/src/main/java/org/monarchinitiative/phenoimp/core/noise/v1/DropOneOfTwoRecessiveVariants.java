package org.monarchinitiative.phenoimp.core.noise.v1;

import org.monarchinitiative.phenoimp.core.PhenoImpRuntimeException;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.constants.hpo.HpoModeOfInheritanceTermIds;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Disease;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

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
        // There is no such a thing as excluded disease in Phenopacket Schema v1.
        List<Disease> observedDiseases = pp.getDiseasesList();

        if (observedDiseases.size() != 1) {
            LOGGER.warn("The disease count {}!=1, not removing of one of the recessive variants in {}", observedDiseases.size(), pp.getId());
            return pp;
        }

        Disease disease = observedDiseases.get(0);
        TermId diseaseId = parseDiseaseId(disease.getTerm());
        Optional<HpoDisease> diseaseOptional = diseases.diseaseById(diseaseId);


        List<Variant> variants = pp.getVariantsList();
        if (diseaseOptional.isEmpty()) {
            LOGGER.warn("Unknown disease {} ({}), not removing one of the recessive variants in {}", disease.getTerm().getLabel(), disease.getTerm().getId(), pp.getId());
            return pp;
        }


        // Process the disease.
        boolean hasAR = diseaseHasArModeOfInheritance(diseaseOptional.get());
        if (hasAR) {
            if (variants.size() != 2) {
                LOGGER.warn("The variant count {}!=2, not removing one of the recessive variants in {}", variants.size(), pp.getId());
                return pp;
            }

            int idxOfRemoved = random.nextInt(variants.size());
            List<Variant> passingVariants = new ArrayList<>(variants.size() - 1);
            for (int i = 0; i < variants.size(); i++) {
                if (i != idxOfRemoved)
                    passingVariants.add(variants.get(i));
            }


            // Remove random interpretation.
            return pp.toBuilder()
                    .clearVariants()
                    .addAllVariants(passingVariants)
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
