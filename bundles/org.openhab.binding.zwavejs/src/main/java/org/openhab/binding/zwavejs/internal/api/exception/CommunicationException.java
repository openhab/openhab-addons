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
package org.openhab.binding.zwavejs.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when there is a communication error with the Z-Wave JS Webservice.
 * This exception can be used to indicate various communication issues such as
 * connection failures, timeouts, and protocol errors.
 * 
 * <p>
 * This class provides constructors to include a message, a cause, and options
 * to enable suppression and writable stack trace.
 * 
 * @see Exception
 * @see Throwable
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class CommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    /*
     * Constructs a new CommunicationException with the specified detail message.
     *
     * @param message the detail message
     */
    public CommunicationException(String message) {
        super(message);
    }

    /*
     * Constructs a new CommunicationException with the specified detail message and cause.
     *
     * @param message the detail message
     * 
     * @param cause the cause of the exception
     */
    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /*
     * Constructs a new CommunicationException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public CommunicationException(Throwable cause) {
        super(cause);
    }
}
