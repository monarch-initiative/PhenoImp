package org.monarchinitiative.phenoimp.core.io;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenoimp.core.TestBase;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PhenoImpDataResolverTest {

    @Test
    public void test() {
        Path dataDir = TestBase.TEST_BASE.resolve("data");
        PhenoImpDataResolver resolver = new PhenoImpDataResolver(dataDir);

        assertThat(Files.isRegularFile(resolver.hpJsonPath()), equalTo(true));
        assertThat(Files.isRegularFile(resolver.hpoAnnotationPath()), equalTo(true));
    }

    @Test
    public void error() {
        MissingResourceException e = assertThrows(MissingResourceException.class, () -> new PhenoImpDataResolver(Path.of("")));
        assertThat(e.getMessage(), equalTo("The following files are missing in the data directory: 'hp.json', 'phenotype.hpoa'."));
    }

}