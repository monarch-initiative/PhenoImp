package org.monarchinitiative.hellion.core;

public class PhenopacketFuzzerRuntimeException extends RuntimeException {

    public PhenopacketFuzzerRuntimeException() {
        super();
    }

    public PhenopacketFuzzerRuntimeException(String message) {
        super(message);
    }

    public PhenopacketFuzzerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhenopacketFuzzerRuntimeException(Throwable cause) {
        super(cause);
    }

    protected PhenopacketFuzzerRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
