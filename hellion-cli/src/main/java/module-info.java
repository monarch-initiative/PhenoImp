module org.monarchinitiative.hellion.cli {
    requires org.monarchinitiative.hellion.core;
    requires org.monarchinitiative.biodownload;
    requires info.picocli;
    requires org.slf4j;

    opens org.monarchinitiative.hellion.cli.cmd to info.picocli;
}