/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when a generic protocol error occurs.
 *
 * <p>
 * Generic protocol errors include for instance missing fields, incorrect or
 * unexpected types, etc. Any error that can happen when communicating with a
 * device that is not covered by another exception is covered by this exception.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ProtocolError extends AtvException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception without a message.
     */
    public ProtocolError() {
    }

    /**
     * Creates a new exception with a message.
     *
     * @param message the detail message
     */
    public ProtocolError(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ProtocolError(String message, Throwable cause) {
        super(message, cause);
    }
}
