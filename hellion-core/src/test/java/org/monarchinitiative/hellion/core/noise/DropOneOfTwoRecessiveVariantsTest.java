package org.monarchinitiative.hellion.core.noise;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hellion.core.TestBase;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.*;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Interpretation;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DropOneOfTwoRecessiveVariantsTest {

    @Test
    public void fuzz() {
        DropOneOfTwoRecessiveVariants fuzzer = new DropOneOfTwoRecessiveVariants(TestBase.HPO_TOY, TestBase.DISEASES, 42L);

        Phenopacket pp = phenopacketWithRecessiveDiseaseAndTwoRecessiveVariants();
        assertThat(pp.getInterpretationsCount(), equalTo(2));

        Phenopacket result = fuzzer.distort(pp);

        List<Interpretation> interpretations = result.getInterpretationsList();
        assertThat(interpretations.size(), equalTo(1));

        assertThat(interpretations.stream().anyMatch(i -> i.getId().equals("first-interpretation-id")), equalTo(true));
    }

    private static Phenopacket phenopacketWithRecessiveDiseaseAndTwoRecessiveVariants() {
        String individualId = "individual-id";
        String diseaseId = "OMIM:143890";
        String diseaseName = "Hypercholesterolemia, Familial, 1";
        return PhenopacketBuilder.create("abc", MetaDataBuilder.builder("HPO:walterwhite").build())
                .individual(IndividualBuilder.of(individualId))
                .addDisease(DiseaseBuilder.of(diseaseId, diseaseName))
                .addInterpretation(InterpretationBuilder.builder("first-interpretation-id")
                        .solved(DiagnosisBuilder.builder(OntologyClassBuilder.ontologyClass(diseaseId, diseaseName))
                                .addGenomicInterpretation(GenomicInterpretationBuilder.builder(individualId)
                                        .variantInterpretation(VariantInterpretationBuilder.builder(VariationDescriptorBuilder.builder("VCV000251009")
                                                        .vcfHg38("chr19", 11_100_236, "C", "A")
                                                        .heterozygous()
                                                        .build())
                                                .pathogenic()
                                                .build())
                                        .causative()
                                        .build())
                                .build()))
                .addInterpretation(InterpretationBuilder.builder("second-interpretation-id")
                        .solved(DiagnosisBuilder.builder(OntologyClassBuilder.ontologyClass(diseaseId, diseaseName))
                                .addGenomicInterpretation(GenomicInterpretationBuilder.builder(individualId)
                                        .variantInterpretation(VariantInterpretationBuilder.builder(VariationDescriptorBuilder.builder("VCV000251032")
                                                        .vcfHg38("chr19", 11_100_293, "C", "A")
                                                        .heterozygous()
                                                        .build())
                                                .pathogenic()
                                                .build())
                                        .causative()
                                        .build())
                                .build()))
                .build();
    }
}