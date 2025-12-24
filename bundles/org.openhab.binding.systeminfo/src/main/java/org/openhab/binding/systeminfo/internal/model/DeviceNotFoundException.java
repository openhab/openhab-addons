/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.systeminfo.internal.model;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link DeviceNotFoundException} is used to indicate that device can not be found on this hardware configuration, most
 * probably because the device is not installed.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class DeviceNotFoundException extends IOException {
    private static final long serialVersionUID = -707507777792259512L;

    /**
     * Constructs a {@code DeviceNotFoundException} with {@code null}
     * as its error detail message.
     */
    public DeviceNotFoundException() {
    }

    /**
     * Constructs a {@code DeviceNotFoundException} with the specified detail message.
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
     * Constructs a {@code DeviceNotFoundException} with the specified detail message
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
