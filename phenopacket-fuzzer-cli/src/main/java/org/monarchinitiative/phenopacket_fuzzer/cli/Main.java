package org.monarchinitiative.phenopacket_fuzzer.cli;

import org.monarchinitiative.phenopacket_fuzzer.cli.cmd.DownloadCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "java -jar phenopacket-fuzzer-cli.jar", mixinStandardHelpOptions = true,
        version = "0.1.0-SNAPSHOT",
        description = "A tool for fuzzing phenopackets in tool performance benchmarks.")
public class Main implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }

        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("download", new DownloadCommand());
        cline.setToggleBooleanFlags(false);
        long startTime = System.currentTimeMillis();
        int exitCode = cline.execute(args);
        long stopTime = System.currentTimeMillis();
        reportElapsedTime(startTime, stopTime);
        System.exit(exitCode);
    }


    private static void reportElapsedTime(long startTime, long stopTime) {
        int elapsedTime = (int)((stopTime - startTime)*(1.0)/1000);
        if (elapsedTime > 3599) {
            int elapsedSeconds = elapsedTime % 60;
            int elapsedMinutes = (elapsedTime/60) % 60;
            int elapsedHours = elapsedTime/3600;
            LOGGER.info(String.format("Elapsed time %d:%2d%2d",elapsedHours,elapsedMinutes,elapsedSeconds));
        }
        else if (elapsedTime>59) {
            int elapsedSeconds = elapsedTime % 60;
            int elapsedMinutes = (elapsedTime/60) % 60;
            LOGGER.info(String.format("Elapsed time %d min, %d sec",elapsedMinutes,elapsedSeconds));
        } else {
            LOGGER.info("Elapsed time " + (stopTime - startTime) * (1.0) / 1000 + " seconds.");
        }
    }

    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }

}
