module org.monarchinitiative.phenoimp.cli {
    requires org.monarchinitiative.phenoimp.core;
    requires org.monarchinitiative.biodownload;
    requires com.google.protobuf.util;
    requires info.picocli;
    requires org.slf4j;

    opens org.monarchinitiative.phenoimp.cli.cmd to info.picocli;
}