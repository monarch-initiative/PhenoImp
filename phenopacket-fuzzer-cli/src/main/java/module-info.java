module org.monarchinitiative.phenopacket_fuzzer.cli {
    requires org.monarchinitiative.phenopacket_fuzzer.core;
    requires org.monarchinitiative.biodownload;
    requires info.picocli;
    requires org.slf4j;

    opens org.monarchinitiative.phenopacket_fuzzer.cli.cmd to info.picocli;
}