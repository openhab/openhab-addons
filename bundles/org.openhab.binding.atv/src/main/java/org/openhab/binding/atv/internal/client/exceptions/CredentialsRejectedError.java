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
package org.openhab.binding.atv.internal.client.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Thrown when no protocol could connect because the device rejected the stored credentials of one of
 * them, e.g. after the pairing was removed on the device. Retrying will not help; the protocol has to be
 * paired again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class CredentialsRejectedError extends AuthenticationError {

    private static final long serialVersionUID = 1L;

    private final Protocol protocol;

    /**
     * Creates a new exception for the protocol whose credentials were rejected.
     *
     * @param protocol the protocol whose credentials were rejected
     * @param cause the failure reported by that protocol
     */
    public CredentialsRejectedError(Protocol protocol, Throwable cause) {
        super(protocol + " credentials were rejected by the device", cause);
        this.protocol = protocol;
    }

    /**
     * Returns the protocol whose credentials were rejected.
     *
     * @return the protocol
     */
    public Protocol protocol() {
        return protocol;
    }
}
