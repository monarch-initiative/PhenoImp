package org.monarchinitiative.phenopacket_fuzzer.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class AddNRandomPhenotypeTermsTest {

    private static Ontology ONTOLOGY;

    private AddNRandomPhenotypeTerms fuzzer;

    @BeforeAll
    public static void beforeAll() {
        ONTOLOGY = OntologyLoader.loadOntology(TestBase.TEST_BASE.resolve("hpo_toy.json").toFile());
    }

    @BeforeEach
    public void setUp() {
        fuzzer = new AddNRandomPhenotypeTerms(ONTOLOGY,2, 42L);
    }

    @Test
    public void fuzz() {
        Phenopacket result = fuzzer.fuzz(TestCases.PHENOPACKET);

        List<String> actual = result.getPhenotypicFeaturesList().stream()
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .toList();

        assertThat(actual, containsInAnyOrder(
                "HP:0030084", // Clinodactyly
                "HP:0000555", // Leukocoria
                "HP:0000486", // Strabismus
                "HP:0000541", // Retinal detachment
                "HP:0011842", // Abnormal skeletal morphology
                "HP:0002813" // Abnormality of limb bone morphology
        ));
    }

}