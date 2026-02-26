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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class UserRequestException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String messageKey;
    private final Object[] messageArguments;

    public UserRequestException(int statusCode, String messageKey, Object... messageArguments) {
        super(messageKey);
        if (statusCode < 400 || statusCode > 499) {
            throw new IllegalArgumentException("Status code must be between 400 and 499");
        }
        this.statusCode = statusCode;
        this.messageKey = messageKey;
        this.messageArguments = Arrays.copyOf(messageArguments, messageArguments.length);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArguments() {
        return Arrays.copyOf(messageArguments, messageArguments.length);
    }
}
