package org.openhab.binding.sensibo.internal;

import org.eclipse.jdt.annotation.NonNull;

public abstract class SensiboException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SensiboException(String message) {
        super(message);
    }

    public SensiboException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

}
