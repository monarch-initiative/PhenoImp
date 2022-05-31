module org.monarchinitiative.phenopacket_fuzzer.core {
    requires transitive phenopacket.schema;
    requires org.phenopacket.tools.builder;
    requires org.monarchinitiative.phenol.core;
    requires org.slf4j;

    exports org.monarchinitiative.phenopacket_fuzzer.core;

}