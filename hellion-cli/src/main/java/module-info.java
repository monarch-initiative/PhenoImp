module org.monarchinitiative.hellion.cli {
    requires org.monarchinitiative.hellion.core;
    requires org.monarchinitiative.biodownload;
    requires com.google.protobuf.util;
    requires info.picocli;
    requires org.slf4j;

    opens org.monarchinitiative.hellion.cli.cmd to info.picocli;
}