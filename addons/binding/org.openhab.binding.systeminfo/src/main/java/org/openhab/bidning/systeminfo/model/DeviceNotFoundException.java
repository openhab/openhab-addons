package org.openhab.bidning.systeminfo.model;

import java.io.IOException;

//TODO javadoc
public class DeviceNotFoundException extends IOException {
    private static final long serialVersionUID = -707507777792259512L;

    /**
     * Constructs an {@code DeviceNotFoundException} with {@code null}
     * as its error detail message.
     */
    public DeviceNotFoundException() {
        super();
    }

    /**
     * Constructs an {@code DeviceNotFoundException} with the specified detail message.
     * This Exceptions is used to indicate that the {@link SysteminfoInterface}} can not find a device with an index
     *
     * @param message
     *            The detail message (which is saved for later retrieval
     *            by the {@link #getMessage()} method)
     */
    public DeviceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code DeviceNotFoundException} with the specified detail message
     * and cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message
     *            The detail message (which is saved for later retrieval
     *            by the {@link #getMessage()} method)
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A null value is permitted,
     *            and indicates that the cause is nonexistent or unknown.)
     *
     * @since 1.6
     */
    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
