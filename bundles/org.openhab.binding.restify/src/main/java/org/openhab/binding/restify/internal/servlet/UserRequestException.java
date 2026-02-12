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
package org.openhab.binding.restify.internal.servlet;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class UserRequestException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int statusCode;

    public UserRequestException(int statusCode, String message) {
        super(message);
        if (statusCode < 400 || statusCode > 499) {
            throw new IllegalArgumentException("Status code must be between 400 and 499");
        }
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
