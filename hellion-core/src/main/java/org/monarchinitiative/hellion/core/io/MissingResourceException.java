package org.monarchinitiative.hellion.core.io;

import org.monarchinitiative.hellion.core.PhenopacketHellionRuntimeException;

/**
 * An exception thrown when a file is missing in application's data directory.
 */
public class MissingResourceException extends PhenopacketHellionRuntimeException {
    public MissingResourceException() {
        super();
    }

    public MissingResourceException(String message) {
        super(message);
    }

    public MissingResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingResourceException(Throwable cause) {
        super(cause);
    }

    protected MissingResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
