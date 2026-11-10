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
package org.openhab.binding.tailwind.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handles sending unsupported command error messages.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindUnsupportedCommandTypeException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Method to send error when an unsupported command is encountered
     */
    public TailwindUnsupportedCommandTypeException() {
        super();
    }

    /**
     * Method to send error when an unsupported command is encountered with additional diagnostic information
     *
     * @param message - include diagnostic information for troubleshooting errors
     */
    public TailwindUnsupportedCommandTypeException(String message) {
        super(message);
    }
}
