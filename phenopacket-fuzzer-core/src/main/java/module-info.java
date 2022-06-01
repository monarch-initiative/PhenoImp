module org.monarchinitiative.phenopacket_fuzzer.core {
    requires transitive org.phenopackets.schema;
    requires org.phenopackets.phenopackettools.builder;
    requires org.monarchinitiative.phenol.core;
    requires org.slf4j;

    exports org.monarchinitiative.phenopacket_fuzzer.core;

}