package org.monarchinitiative.phenoimp.configuration;

import java.nio.file.Path;

public class TestBase {

    public static final Path TEST_BASE = Path.of("src/test/resources");
    public static final Path PACKAGE_TEST_BASE = TEST_BASE.resolve("org/monarchinitiative/phenoimp/configuration");

}
