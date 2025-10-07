/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
public class NotTapLinkGatewayException extends I18Exception {
    @Serial
    private static final long serialVersionUID = -7786449325604153487L;

    public NotTapLinkGatewayException() {
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

    public NotTapLinkGatewayException(final String message, final String i18key) {
        super(message);
        this.i18Key = i18key;
    }

    public NotTapLinkGatewayException(final NotTapLinkGatewapExecptionDefinitions definition) {
        this(definition.description, definition.i18Key);
    }

    public enum NotTapLinkGatewapExecptionDefinitions {

        /**
         * HEADERS_MISSING
         */
        HEADERS_MISSING("Missing header markers", "exception.not-gw.missing-headers"),

        /**
         * MISSING_API_TITLE
         */
        MISSING_API_TITLE("Not a LinkTap API response", "exception.not-gw.missing-api-title"),

        /**
         * MISSING_SERVER_TITLE
         */
        MISSING_SERVER_TITLE("Not a LinkTap response", "exception.not-gw.missing-server-title"),

        /**
         * UNEXPECTED_STATUS_CODE
         */
        UNEXPECTED_STATUS_CODE("Unexpected status code response", "exception.not-gw.unexpected-status-code"),

        /**
         * UNEXPECTED_HTTPS
         */
        UNEXPECTED_HTTPS("Unexpected protocol", "exception.not-gw.unexpected-protocol");

        private final String description;
        private final String i18Key;

        private NotTapLinkGatewapExecptionDefinitions(final String description, final String i18key) {
            this.description = description;
            this.i18Key = i18key;
        }

        public String getI18Key() {
            return i18Key;
        }

        public String getDesc() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public String getI18Key() {
        return getI18Key("exception.not-tap-link-gw");
    }
}
