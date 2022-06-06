package org.monarchinitiative.hellion.core;

import org.phenopackets.schema.v2.Phenopacket;

import java.nio.file.Path;

/**
 * Implementors can distort the provided phenopacket.
 */
public interface HellionRunner {

    static HellionRunnerBuilder builder(Path dataDirectory) {
        return new HellionRunnerBuilder(dataDirectory);
    }

    Phenopacket run(Phenopacket pp);

}
