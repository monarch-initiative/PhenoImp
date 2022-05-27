package org.monarchinitiative.phenopacket_fuzzer.core;

import org.phenopackets.schema.v2.Phenopacket;

public interface PhenopacketFuzzer {

    Phenopacket fuzz(Phenopacket value);

}
