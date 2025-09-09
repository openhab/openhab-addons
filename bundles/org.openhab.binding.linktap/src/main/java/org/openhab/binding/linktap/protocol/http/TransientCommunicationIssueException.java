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
 * The {@link TransientCommunicationIssueException} should be thrown when the endpoint being communicated with
 * does not appear to be a Tap Link Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class TransientCommunicationIssueException extends I18Exception {
    @Serial
    private static final long serialVersionUID = -7786449325604143287L;

    public TransientCommunicationIssueException() {
    }

    public TransientCommunicationIssueException(final String message, final String i18key) {
        super(message);
        this.i18Key = i18key;
    }

    public TransientCommunicationIssueException(final TransientExecptionDefinitions definition) {
        this(definition.description, definition.i18Key);
    }

    public TransientCommunicationIssueException(final Throwable cause) {
        super(cause);
    }

    public TransientCommunicationIssueException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public enum TransientExecptionDefinitions {

        /**
         * HOST_UNREACHABLE
         */
        HOST_UNREACHABLE("Could not connect", "exception.could-not-connect"),

        /**
         * HOST_NOT_RESOLVED
         */
        HOST_NOT_RESOLVED("Could not resolve IP address", "exception.could-not-resolve"),

        /**
         * COMMUNICATIONS_LOST
         */
        COMMUNICATIONS_LOST("Communications Lost", "exception.communications-lost"),

        /**
         * GATEWAY_BUSY
         */
        GATEWAY_BUSY("Gateway Busy", "exception.gateway-busy");

        private final String description;
        private final String i18Key;

        private TransientExecptionDefinitions(final String description, final String i18key) {
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
        return getI18Key("exception.gw-id-exception");
    }
}
