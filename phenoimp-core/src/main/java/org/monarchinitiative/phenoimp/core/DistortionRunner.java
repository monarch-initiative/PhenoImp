package org.monarchinitiative.phenoimp.core;

import com.google.protobuf.Message;

/**
 * Implementors can distort the provided phenopacket.
 */
public interface DistortionRunner {

    Message run(Message message);

}
