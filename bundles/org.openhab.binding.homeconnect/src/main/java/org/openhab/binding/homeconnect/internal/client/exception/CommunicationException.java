/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client.exception;

import static java.lang.String.format;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * API communication exception
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class CommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }

    public CommunicationException(int code, String message, String body) {
        super(format("Communication error! response code: %d, message: %s, body: %s (Tried at %s)", code, message, body,
                new Date()));
    }
}
