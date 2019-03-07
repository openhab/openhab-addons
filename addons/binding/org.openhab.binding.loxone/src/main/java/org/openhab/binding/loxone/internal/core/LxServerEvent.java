/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.core;

import org.openhab.binding.loxone.internal.core.LxWsClient.LxWebSocket;

/**
 * Event used to communicate between websocket client ({@link LxWebSocket}) and thing handler
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxServerEvent {
    /**
     * Type of {@link LxServerEvent} event
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    public enum EventType {
    /**
     * Unspecified event
     */
    NONE,
    /**
     * Miniserver is online - websocket connection ready to pass commands and receive controls state updates
     */
    SERVER_ONLINE,
    /**
     * Miniserver is offline - websocket connection is closed. There is a reason parameter associated.
     */
    SERVER_OFFLINE,
    /**
     * Received configuration of Miniserver. There is a {@link LxConfig} object associated.
     */
    RECEIVED_CONFIG,
    /**
     * Received control's state value or text update from Miniserver. There is a {@link LxWsStateUpdateEvent} object
     * associated.
     */
    STATE_UPDATE,
    /**
     * Received request to shutdown thread from thing handler object.
     */
    CLIENT_CLOSING
    }

    private final EventType event;
    private final LxErrorCode reason;
    private final Object object;

    public LxServerEvent(EventType event, LxErrorCode reason, Object object) {
        this.event = event;
        this.reason = reason;
        this.object = object;
    }

    /**
     * Get type of event
     *
     * @return
     *         type of event
     */
    public EventType getEvent() {
        return event;
    }

    /**
     * Get reason for server going offline
     *
     * @return
     *         reason for going offline
     */
    public LxErrorCode getOfflineReason() {
        return reason;
    }

    /**
     * Get object associated with the event
     *
     * @return
     *         object associated with event
     */
    public Object getObject() {
        return object;
    }
}
