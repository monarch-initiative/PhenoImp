module org.monarchinitiative.hellion.core {
    requires transitive org.phenopackets.schema;
    requires org.phenopackets.phenopackettools.builder;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchinitiative.phenol.annotations;
    requires org.slf4j;

    exports org.monarchinitiative.hellion.core;

}