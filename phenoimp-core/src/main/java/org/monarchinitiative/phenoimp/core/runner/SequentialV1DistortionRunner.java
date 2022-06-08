package org.monarchinitiative.phenoimp.core.runner;

import com.google.protobuf.Message;
import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.phenopackets.schema.v1.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Runner for v1 {@link Phenopacket}s.
 */
public class SequentialV1DistortionRunner implements DistortionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialV1DistortionRunner.class);

    private final List<PhenopacketNoise<Phenopacket>> noises;

    public SequentialV1DistortionRunner(List<PhenopacketNoise<Phenopacket>> noises) {
        this.noises = Objects.requireNonNull(noises);
        if (noises.isEmpty())
            LOGGER.warn("No noise will be added!");
    }

    @Override
    public Message run(Message message) {
        if (message instanceof Phenopacket pp) {
            for (PhenopacketNoise<Phenopacket> noise : noises) {
                pp = noise.distort(pp);
            }
            return pp;
        }

        throw new IllegalArgumentException("Provided message does not represent v1 phenopacket!");
    }
}
