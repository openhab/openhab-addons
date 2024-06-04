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
 * The {@link TransientCommunicationIssueException} should be thrown when the endpoint being communicated with
 * does not appear to be a Tap Link Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class TransientCommunicationIssueException extends Exception {
    @Serial
    private static final long serialVersionUID = -7786449325604143287L;

    public TransientCommunicationIssueException() {
        super();
    }

    public TransientCommunicationIssueException(final String message) {
        super(message);
    }

    public TransientCommunicationIssueException(final Throwable cause) {
        super(cause);
    }

    public TransientCommunicationIssueException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static final String HOST_UNREACHABLE = "Could not connect";
    public static final String HOST_NOT_RESOLVED = "Could not resolve IP address";
    public static final String HOST_COMM_TIMEOUT = "Communications Lost";
}
