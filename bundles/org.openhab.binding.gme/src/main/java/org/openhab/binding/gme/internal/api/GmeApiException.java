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
package org.openhab.binding.gme.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception returned for HTTP errors from the GME API.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeApiException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public GmeApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isAuthenticationError() {
        return statusCode == 401 || statusCode == 403;
    }
}
