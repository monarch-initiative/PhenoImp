package org.monarchinitiative.hellion.core.runner;

import org.monarchinitiative.hellion.core.HellionRunner;
import org.monarchinitiative.hellion.core.noise.PhenopacketNoise;
import org.phenopackets.schema.v2.Phenopacket;

import java.util.List;

public class SequentialHellionRunner implements HellionRunner {

    private final List<PhenopacketNoise> noises;

    public SequentialHellionRunner(List<PhenopacketNoise> noise) {
        this.noises = noise;
    }

    @Override
    public Phenopacket run(Phenopacket pp) {
        for (PhenopacketNoise noise : noises) {
            pp = noise.distort(pp);
        }
        return pp;
    }
}
