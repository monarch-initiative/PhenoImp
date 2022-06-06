package org.monarchinitiative.hellion.core.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class for resolving paths to file paths to data directory.
 */
public class PhenopacketHellionDataResolver {

    private final Path dataDirectory;

    public PhenopacketHellionDataResolver(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory);

        List<String> errors = new LinkedList<>();
        List<Path> paths = List.of(hpJsonPath(), hpoAnnotationPath());
        for (Path path : paths) {
            if (!(Files.isRegularFile(path) && Files.isReadable(path))) {
                errors.add(path.toFile().getName());
            }
        }

        if (!errors.isEmpty()) {
            String missing = errors.stream().collect(Collectors.joining("', '", "'", "'"));
            String message = String.format("The following files are missing in the data directory: %s.", missing);
            throw new MissingResourceException(message);
        }
    }

    public Path hpJsonPath() {
        return dataDirectory.resolve("hp.json");
    }

    public Path hpoAnnotationPath() {
        return dataDirectory.resolve("phenotype.hpoa");
    }


}
