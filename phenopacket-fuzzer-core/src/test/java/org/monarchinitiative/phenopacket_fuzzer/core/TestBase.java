package org.monarchinitiative.phenopacket_fuzzer.core;

import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.nio.file.Path;

public class TestBase {

    public static final Path TEST_BASE = Path.of("src/test/resources/org/monarchinitiative/phenopacket_fuzzer/core");

    public static final Ontology HPO_TOY = loadToyHpo();

    private static Ontology loadToyHpo() {
        return OntologyLoader.loadOntology(TEST_BASE.resolve("hpo_toy.json").toFile());
    }

}
