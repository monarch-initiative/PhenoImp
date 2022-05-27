package org.monarchinitiative.phenopacket_fuzzer.core;

public enum FuzzStrategy {

    ADD_N_RANDOM_HPO_TERMS,
    REPLACE_HPO_WITH_PARENT,
    REPLACE_HPO_WITH_GRANDPARENT,
    DROP_ONE_CAUSATIVE_VARIANT

}
