package org.monarchinitiative.phenoimp.core;

import org.phenopackets.schema.v2.Phenopacket;

import java.nio.file.Path;

/**
 * Implementors can distort the provided phenopacket.
 */
public interface DistortionRunner {

    static DistortionRunnerBuilder builder(Path dataDirectory) {
        return new DistortionRunnerBuilder(dataDirectory);
    }

    Phenopacket run(Phenopacket pp);

}
