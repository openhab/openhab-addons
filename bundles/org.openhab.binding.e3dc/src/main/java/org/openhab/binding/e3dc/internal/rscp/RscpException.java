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
package org.openhab.binding.e3dc.internal.rscp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The exception the RSCP layer throws when a request or a frame fails.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RscpException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message.
     */
    public RscpException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     */
    public RscpException(String message, Throwable cause) {
        super(message, cause);
    }
}
