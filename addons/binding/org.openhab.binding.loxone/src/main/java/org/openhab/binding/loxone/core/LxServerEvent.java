/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import org.openhab.binding.loxone.core.LxWsClient.LxWebSocket;

/**
 * Event used to communicate between websocket client ({@link LxWebSocket}) and object representing a Miniserver
 * ({@link LxServer})
 *
 * @author Pawel Pieczul - initial commit
 *
 */
class LxServerEvent {
    /**
     * Type of {@link LxServerEvent} event
     *
     * @author Pawel Pieczul - initial commit
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
         * Received configuration of Miniserver. There is a {@link LxJsonApp3} object associated.
         */
        RECEIVED_CONFIG,
        /**
         * Received control's state value update from Miniserver. There is a {@link LxWsStateUpdateEvent} object
         * associated.
         */
        STATE_VALUE_UPDATE,
        /**
         * Received request to shutdown thread from {@link LxServer} object.
         */
        CLIENT_CLOSING
    }

    private EventType event;
    private LxServer.OfflineReason reason;
    private Object object;

    LxServerEvent(EventType event, LxServer.OfflineReason reason, Object object) {
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
    EventType getEvent() {
        return event;
    }

    /**
     * Get reason for server going offline
     *
     * @return
     *         reason for going offline
     */
    LxServer.OfflineReason getOfflineReason() {
        return reason;
    }

    /**
     * Get object associated with the event
     * 
     * @return
     *         object associated with event
     */
    Object getObject() {
        return object;
    }
}
