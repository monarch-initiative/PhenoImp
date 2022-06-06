package org.monarchinitiative.hellion.core.noise;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hellion.core.TestBase;
import org.monarchinitiative.hellion.core.TestCases;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class AddNRandomPhenotypeTermsTest {

    @Test
    public void fuzz() {
        int numberOfTermsToAdd = 2;
        AddNRandomPhenotypeTerms fuzzer = new AddNRandomPhenotypeTerms(TestBase.HPO_TOY, numberOfTermsToAdd, 42L);
        Phenopacket phenopacket = TestCases.PHENOPACKET;

        Phenopacket result = fuzzer.distort(phenopacket);

        List<String> actual = result.getPhenotypicFeaturesList().stream()
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .toList();

        assertThat("Original terms must be present!", actual, hasItems(
                "HP:0030084", // Clinodactyly
                "HP:0000555", // Leukocoria
                "HP:0000486", // Strabismus
                "HP:0000541" // Retinal detachment
        ));
        assertThat("Exactly %d terms should have been added".formatted(numberOfTermsToAdd),
                actual.size() - phenopacket.getPhenotypicFeaturesCount(), is(numberOfTermsToAdd));

        Set<String> originalTerms = phenopacket.getPhenotypicFeaturesList().stream()
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .collect(Collectors.toSet());
        assertThat("A previously existing term must not be selected!",
                actual.stream().filter(t -> !originalTerms.contains(t)).count(), equalTo((long) numberOfTermsToAdd));
    }

}