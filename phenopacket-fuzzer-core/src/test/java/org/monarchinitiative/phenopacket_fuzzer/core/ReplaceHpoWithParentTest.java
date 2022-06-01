package org.monarchinitiative.phenopacket_fuzzer.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.phenopackettools.builder.builders.TimeElements;
import org.phenopackets.phenopackettools.builder.constants.Laterality;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReplaceHpoWithParentTest {

    @ParameterizedTest
    @CsvSource({
            "1, HP:0100807, 'Long fingers'",
            "2, HP:0001167, 'Abnormality of finger'",
            "3, HP:0011297, 'Abnormal digit morphology'",
            "4, HP:0002813, 'Abnormality of limb bone morphology'",
            "5, HP:0011844, 'Abnormal appendicular skeleton morphology'",
            "6, HP:0011842, 'Abnormality of skeletal morphology'",
            "7, HP:0000924, 'Abnormality of the skeletal system'",
            "8, HP:0033127, 'Abnormality of the musculoskeletal system'",
            "9, HP:0000118, 'Phenotypic abnormality'"
    })
    public void fuzz_normal(int nHops, String id, String label) {
        ReplaceHpoWithParent fuzzer = new ReplaceHpoWithParent(TestBase.HPO_TOY, nHops, 42L);
        Phenopacket pp = phenopacketWithASingleFeature("HP:0001166", "Arachnodactyly");

        Phenopacket result = fuzzer.fuzz(pp);

        List<OntologyClass> ids = result.getPhenotypicFeaturesList().stream()
                .map(PhenotypicFeature::getType)
                .toList();

        assertThat(ids, hasSize(1));
        assertThat(ids.get(0).getId(), equalTo(id));
        assertThat(ids.get(0).getLabel(), equalTo(label));
    }

    @ParameterizedTest
    @CsvSource({
            "10, HP:0000118, 'Phenotypic abnormality'",
            "1000, HP:0000118, 'Phenotypic abnormality'"
    })
    public void fuzz_beyondBoundaries(int nHops, String id, String label) {
        ReplaceHpoWithParent fuzzer = new ReplaceHpoWithParent(TestBase.HPO_TOY, nHops, 42L);
        Phenopacket pp = phenopacketWithASingleFeature("HP:0001166", "Arachnodactyly");

        Phenopacket result = fuzzer.fuzz(pp);

        List<OntologyClass> ids = result.getPhenotypicFeaturesList().stream()
                .map(PhenotypicFeature::getType)
                .toList();

        assertThat(ids, hasSize(1));
        assertThat(ids.get(0).getId(), equalTo(id));
        assertThat(ids.get(0).getLabel(), equalTo(label));
    }

    @Test
    public void nonPhenotypicAbnormalityDropsTheFeature() {
        ReplaceHpoWithParent fuzzer = new ReplaceHpoWithParent(TestBase.HPO_TOY, 2, 42L);
        Phenopacket pp = phenopacketWithASingleFeature("HP:0003674", "Onset");

        Phenopacket result = fuzzer.fuzz(pp);
        assertThat(result.getPhenotypicFeaturesCount(), equalTo(0));
    }

    @Test
    public void nonPositiveNumberOfHopsThrowsAnException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new ReplaceHpoWithParent(TestBase.HPO_TOY, 0, 42L));
        assertThat(e.getMessage(), equalTo("Number of hops must be positive, got 0"));
    }

    private static Phenopacket phenopacketWithASingleFeature(String id, String label) {
        return TestCases.PHENOPACKET.toBuilder()
                .clearPhenotypicFeatures()
                .addPhenotypicFeatures(PhenotypicFeatureBuilder.builder(id, label)
                        .addModifier(Laterality.right())
                        .onset(TimeElements.congenitalOnset())
                        .build())
                .build();
    }
}