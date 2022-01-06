/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.connection;

import java.security.PrivilegedActionException;

/**
 * @author Wolfgang Klimt - initial contribution
 */
public class aWATTarConfigurationException extends IllegalArgumentException {
    /**
     * Constructs an <code>aWATTarConfigurationException</code> with no
     * detail message.
     */
    public aWATTarConfigurationException() {
        super();
    }

    /**
     * Constructs an <code>aWATTarConfigurationException</code> with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public aWATTarConfigurationException(String s) {
        super(s);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     *            by the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A {@code null} value
     *            is permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.5
     */
    public aWATTarConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A {@code null} value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @since 1.5
     */
    public aWATTarConfigurationException(Throwable cause) {
        super(cause);
    }
}
