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
 * The {@link TailwindException} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dave J. Schoepel - Initial contribution
 */

@NonNullByDefault
public class TailwindException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     *
     * Method to report errors detected by the binding
     *
     * @param message - diagnostic information about the error that was detected
     */
    public TailwindException(String message) {
        super(message);
    }

    /**
     * Method to report java errors detected by the binding
     *
     * @param message - diagnostic information about the error that was detected
     * @param cause - java error details captured when error occurred
     */
    public TailwindException(String message, Throwable cause) {
        super(message, cause);
    }
}
