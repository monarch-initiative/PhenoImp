package org.monarchinitiative.phenoimp.core.runner;

import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class SequentialDistortionRunner implements DistortionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialDistortionRunner.class);

    private final List<PhenopacketNoise> noises;

    public SequentialDistortionRunner(List<PhenopacketNoise> noise) {
        this.noises = Objects.requireNonNull(noise);
        if (noise.isEmpty())
            LOGGER.warn("No noise will be added!");
    }

    @Override
    public Phenopacket run(Phenopacket pp) {
        for (PhenopacketNoise noise : noises) {
            pp = noise.distort(pp);
        }
        return pp;
    }
}
