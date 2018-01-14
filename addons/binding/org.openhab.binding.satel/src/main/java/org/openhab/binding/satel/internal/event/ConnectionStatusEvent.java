/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.event;

/**
 * Event class describing connection status to Satel module.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public class ConnectionStatusEvent implements SatelEvent {

    private boolean connected;

    private String reason;

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
    public ConnectionStatusEvent(boolean connected, String reason) {
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
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("%s: connected = %b, reason = %s", this.getClass().getName(), this.connected, this.reason);
    }
}
