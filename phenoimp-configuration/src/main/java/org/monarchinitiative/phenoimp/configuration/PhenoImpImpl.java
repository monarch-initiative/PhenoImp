package org.monarchinitiative.phenoimp.configuration;

import org.monarchinitiative.phenoimp.core.DistortionRunner;
import org.monarchinitiative.phenoimp.core.PhenoImp;
import org.monarchinitiative.phenoimp.core.PhenopacketVersion;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class PhenoImpImpl implements PhenoImp {

    private final Map<PhenopacketVersion, DistortionRunner> runnerMap;

    PhenoImpImpl(Map<PhenopacketVersion, DistortionRunner> runnerMap) {
        this.runnerMap = Objects.requireNonNull(runnerMap);
    }

    @Override
    public Optional<DistortionRunner> forPhenopacket(PhenopacketVersion version) {
        return Optional.ofNullable(runnerMap.get(version));
    }

}
