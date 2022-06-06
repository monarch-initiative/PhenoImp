package org.monarchinitiative.hellion.core.noise;

import org.phenopackets.schema.v2.Phenopacket;

/**
 * Implementors add noise to the provided phenopacket.
 */
public interface PhenopacketNoise {

    Phenopacket distort(Phenopacket pp);

}
