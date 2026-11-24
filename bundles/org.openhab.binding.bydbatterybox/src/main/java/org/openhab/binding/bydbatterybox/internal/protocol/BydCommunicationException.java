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
package org.openhab.binding.bydbatterybox.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Signals a failed exchange with the battery. The {@link Reason} classifies the failure so that callers can react
 * without inspecting the message.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydCommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public enum Reason {
        /** The TCP connection could not be established (refused, unreachable, connect timeout). */
        UNREACHABLE,
        /**
         * The connection was accepted but closed before the first reply byte. The battery serves only one client
         * and closes further connections this way while another client is connected.
         */
        BUSY,
        /** No complete reply within the timeout. */
        TIMEOUT,
        /** The battery answered with Modbus exception 0x04 or did not finish a measurement in time. */
        NOT_READY,
        /** Malformed reply, CRC error, unexpected exception code or other protocol violation. */
        PROTOCOL
    }

    private final Reason reason;

    public BydCommunicationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public BydCommunicationException(Reason reason, String message, @Nullable Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
