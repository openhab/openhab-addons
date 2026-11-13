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
 * Thrown when invalid DMAP data is parsed.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class InvalidDmapDataError extends AtvException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception without a message.
     */
    public InvalidDmapDataError() {
    }

    /**
     * Creates a new exception with a message.
     *
     * @param message the detail message
     */
    public InvalidDmapDataError(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidDmapDataError(String message, Throwable cause) {
        super(message, cause);
    }
}
