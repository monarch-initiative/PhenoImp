package org.monarchinitiative.hellion.core;

import org.phenopackets.schema.v2.Phenopacket;

public interface PhenopacketFuzzer {

    Phenopacket fuzz(Phenopacket pp);

}
