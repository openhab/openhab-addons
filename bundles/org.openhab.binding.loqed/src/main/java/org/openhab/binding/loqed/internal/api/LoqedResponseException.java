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
package org.openhab.binding.loqed.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates that LOQED returned an unsuccessful or invalid response.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedResponseException extends LoqedApiException {
    private static final long serialVersionUID = 1L;

    public LoqedResponseException(String message) {
        super(message);
    }

    public LoqedResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoqedResponseException(int statusCode, String responseBody) {
        super("LOQED API returned HTTP " + statusCode + (responseBody.isBlank() ? "" : ": " + responseBody));
    }
}
