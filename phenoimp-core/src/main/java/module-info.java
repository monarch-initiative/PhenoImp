module org.monarchinitiative.phenoimp.core {
    requires org.phenopackets.schema;
    requires org.phenopackets.phenopackettools.builder;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchinitiative.phenol.io;
    requires org.monarchinitiative.phenol.annotations;
    requires transitive com.google.protobuf;

    requires org.slf4j;

    exports org.monarchinitiative.phenoimp.core;
    exports org.monarchinitiative.phenoimp.core.noise to org.monarchinitiative.phenoimp.configuration;
    exports org.monarchinitiative.phenoimp.core.runner to org.monarchinitiative.phenoimp.configuration;

}