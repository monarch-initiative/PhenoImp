package org.monarchinitiative.phenoimp.core;

public class PhenoImpRuntimeException extends RuntimeException {

    public PhenoImpRuntimeException() {
        super();
    }

    public PhenoImpRuntimeException(String message) {
        super(message);
    }

    public PhenoImpRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhenoImpRuntimeException(Throwable cause) {
        super(cause);
    }

    protected PhenoImpRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
