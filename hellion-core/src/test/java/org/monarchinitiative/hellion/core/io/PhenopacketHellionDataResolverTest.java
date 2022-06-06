package org.monarchinitiative.hellion.core.io;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hellion.core.TestBase;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PhenopacketHellionDataResolverTest {

    @Test
    public void test() {
        Path dataDir = TestBase.TEST_BASE.resolve("data");
        PhenopacketHellionDataResolver resolver = new PhenopacketHellionDataResolver(dataDir);

        assertThat(Files.isRegularFile(resolver.hpJsonPath()), equalTo(true));
        assertThat(Files.isRegularFile(resolver.hpoAnnotationPath()), equalTo(true));
    }

    @Test
    public void error() {
        MissingResourceException e = assertThrows(MissingResourceException.class, () -> new PhenopacketHellionDataResolver(Path.of("")));
        assertThat(e.getMessage(), equalTo("The following files are missing in the data directory: 'hp.json', 'phenotype.hpoa'."));
    }

}