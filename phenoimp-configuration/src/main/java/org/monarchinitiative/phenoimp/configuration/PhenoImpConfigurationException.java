package org.monarchinitiative.phenoimp.configuration;

import org.monarchinitiative.phenoimp.core.PhenoImpRuntimeException;

/**
 * An exception thrown when a file is missing in application's data directory.
 */
public class PhenoImpConfigurationException extends PhenoImpRuntimeException {
    public PhenoImpConfigurationException() {
        super();
    }

    public PhenoImpConfigurationException(String message) {
        super(message);
    }

    public PhenoImpConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhenoImpConfigurationException(Throwable cause) {
        super(cause);
    }

    protected PhenoImpConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
