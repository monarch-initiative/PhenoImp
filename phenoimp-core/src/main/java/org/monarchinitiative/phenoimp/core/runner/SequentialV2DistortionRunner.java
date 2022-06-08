package org.monarchinitiative.phenoimp.core.runner;

import com.google.protobuf.Message;
import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class SequentialV2DistortionRunner implements DistortionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialV2DistortionRunner.class);

    private final List<PhenopacketNoise> noises;

    public SequentialV2DistortionRunner(List<PhenopacketNoise> noise) {
        this.noises = Objects.requireNonNull(noise);
        if (noise.isEmpty())
            LOGGER.warn("No noise will be added!");
    }

    @Override
    public Message run(Message message) {
        if (message instanceof Phenopacket pp) {
            for (PhenopacketNoise noise : noises) {
                pp = noise.distort(pp);
            }
            return pp;
        }

        throw new IllegalArgumentException("Provided message does not represent v2 phenopacket!");
    }
}
