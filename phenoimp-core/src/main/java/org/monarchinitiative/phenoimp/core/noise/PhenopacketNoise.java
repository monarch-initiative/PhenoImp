package org.monarchinitiative.phenoimp.core.noise;

import com.google.protobuf.Message;

/**
 * Implementors add noise to the provided phenopacket.
 */
public interface PhenopacketNoise<T extends Message> {

    T distort(T pp);

}
