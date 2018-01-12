/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.internal.model;

import java.io.IOException;

/**
 * {@link DeviceNotFoundException} is used to indicate that device can not be found on this hardware configuration, most
 * probably because the device is not installed.
 *
 * @author Svilen Valkanov
 *
 */
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
     *
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
     */
    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
