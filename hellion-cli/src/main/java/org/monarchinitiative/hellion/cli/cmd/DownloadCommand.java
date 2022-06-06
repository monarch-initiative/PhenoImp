package org.monarchinitiative.hellion.cli.cmd;

import org.monarchinitiative.biodownload.BioDownloader;
import org.monarchinitiative.biodownload.FileDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "download",
        mixinStandardHelpOptions = true,
        description = "Download files for Phenopacket hellion")
public class DownloadCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class);

    @CommandLine.Option(names={"-d","--data"},
            description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public Path datadir = Path.of("data");

    @CommandLine.Option(names={"-w","--overwrite"},
            description = "overwrite previously downloaded files (default: ${DEFAULT-VALUE})")
    public boolean overwrite = false;


    @Override
    public Integer call() {
        try {
            LOGGER.info("Downloading data to {}", datadir.toAbsolutePath());
            BioDownloader downloader = BioDownloader.builder(datadir)
                    .overwrite(overwrite)
                    .hpoJson()
                    .build();

            downloader.download();
            LOGGER.info("Done!");
            return 0;
        } catch (FileDownloadException e) {
            LOGGER.error(e.getMessage(), e);
            return 1;
        }
    }
}
