package org.monarchinitiative.phenoimp.core;

import java.util.Optional;

public interface PhenoImp {

    Optional<DistortionRunner> forPhenopacket(PhenopacketVersion version);

}
