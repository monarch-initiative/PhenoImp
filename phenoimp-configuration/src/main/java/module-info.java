module org.monarchinitiative.phenoimp.configuration {
    requires transitive org.monarchinitiative.phenoimp.core;
    requires org.monarchinitiative.phenol.io;
    requires org.monarchinitiative.phenol.annotations;
    requires org.phenopackets.schema;
    requires org.slf4j;

    exports org.monarchinitiative.phenoimp.configuration;
}