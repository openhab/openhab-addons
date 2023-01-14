/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event class describing connection status to Satel module.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ConnectionStatusEvent implements SatelEvent {

    private boolean connected;

    private @Nullable String reason;

    /**
     * Constructs event class with given connection status.
     *
     * @param connected value describing connection status
     */
    public ConnectionStatusEvent(boolean connected) {
        this(connected, null);
    }

    /**
     * Constructs event class with given connection status and disconnection reason.
     *
     * @param connected value describing connection status
     * @param reason disconnection reason
     */
    public ConnectionStatusEvent(boolean connected, @Nullable String reason) {
        this.connected = connected;
        this.reason = reason;
    }

    /**
     * Returns status of connection.
     *
     * @return a boolean value describing connection status
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Returns disconnection reason.
     *
     * @return optional text description in case of disconnection
     */
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("%s: connected = %b, reason = %s", this.getClass().getName(), this.connected, this.reason);
    }
}
