package org.monarchinitiative.hellion.core;

public class PhenopacketHellionRuntimeException extends RuntimeException {

    public PhenopacketHellionRuntimeException() {
        super();
    }

    public PhenopacketHellionRuntimeException(String message) {
        super(message);
    }

    public PhenopacketHellionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhenopacketHellionRuntimeException(Throwable cause) {
        super(cause);
    }

    protected PhenopacketHellionRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
