package org.monarchinitiative.hellion.core;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaders;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.IOException;
import java.nio.file.Path;

public class TestBase {

    public static final Path TEST_BASE = Path.of("src/test/resources");
    public static final Path PACKAGE_TEST_BASE = TEST_BASE.resolve("org/monarchinitiative/hellion/core");

    public static final Ontology HPO_TOY = loadToyHpo();

    public static final HpoDiseases DISEASES = loadToyDiseases();

    private static Ontology loadToyHpo() {
        return OntologyLoader.loadOntology(PACKAGE_TEST_BASE.resolve("hpo_toy.json").toFile());
    }

    private static HpoDiseases loadToyDiseases() {
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(TestBase.HPO_TOY, HpoDiseaseLoaderOptions.defaultOptions());
        try {
            return loader.load(PACKAGE_TEST_BASE.resolve("phenotype.excerpt.hpoa"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
