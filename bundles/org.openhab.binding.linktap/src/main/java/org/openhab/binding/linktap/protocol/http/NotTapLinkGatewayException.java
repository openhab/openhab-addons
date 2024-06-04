/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.http;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NotTapLinkGatewayException} should be thrown when the endpoint being communicated with
 * does not appear to be a Tap Link Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class NotTapLinkGatewayException extends Exception {
    @Serial
    private static final long serialVersionUID = -7786449325604153487L;

    public NotTapLinkGatewayException() {
        super();
    }

    public NotTapLinkGatewayException(final String message) {
        super(message);
    }

    public NotTapLinkGatewayException(final Throwable cause) {
        super(cause);
    }

    public NotTapLinkGatewayException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static final String HEADERS_MISSING = "Missing header markers";
    public static final String MISSING_API_TITLE = "Not a LinkTap API response";
    public static final String MISSING_SERVER_TITLE = "Not a LinkTap response";
    public static final String UNEXPECTED_STATUS_CODE = "Unexpected status code response";
    public static final String UNEXPECTED_HTTPS = "Unexpected protocol";
}
