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
package org.openhab.binding.matter.internal.client.dto.ws;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * NodeState
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum NodeState {
    /** Node is connected, but may not be fully initialized / subscribed. */
    CONNECTED("Connected"),
    /**
     * Node is disconnected. Data are stale and interactions will most likely return an error. If controller
     * instance
     * is still active then the device will be reconnected once it is available again.
     */
    DISCONNECTED("Disconnected"),

    /** Node is reconnecting. Data are stale. It is yet unknown if the reconnection is successful. */
    RECONNECTING("Reconnecting"),

    /**
     * The node could not be connected and the controller is now waiting for a MDNS announcement and tries every 10
     * minutes to reconnect.
     */
    WAITINGFORDEVICEDISCOVERY("WaitingForDeviceDiscovery"),

    /**
     * Node structure has changed (Endpoints got added or also removed). Data are up-to-date.
     * This State information will only be fired when the subscribeAllAttributesAndEvents option is set to true.
     */
    STRUCTURECHANGED("StructureChanged"),

    /**
     * The node was just Decommissioned.
     */
    DECOMMISSIONED("Decommissioned");

    private String state = "";

    NodeState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
