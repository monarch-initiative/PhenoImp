module org.monarchinitiative.phenoimp.cli {
    requires org.monarchinitiative.phenoimp.core;
    requires org.monarchinitiative.phenoimp.configuration;
    requires org.monarchinitiative.biodownload;
    requires org.phenopackets.schema;
    requires com.google.protobuf.util;
    requires info.picocli;
    requires org.slf4j;

    opens org.monarchinitiative.phenoimp.cli.cmd to info.picocli;
}