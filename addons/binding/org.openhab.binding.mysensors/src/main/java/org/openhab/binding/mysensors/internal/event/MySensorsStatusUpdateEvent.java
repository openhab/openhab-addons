/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * If a new message from the gateway/bridge is received
 * a MySensorsStatusUpdateEvent is generated containing the MySensors message
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsStatusUpdateEvent {
    private Object data;

    private MySensorsEventType eventType;

    /**
     * Initialization of the StatusUpdateEvent class.
     *
     * @param eventType Type of event received from the gateway.
     * @param data data received with the event.
     */
    public MySensorsStatusUpdateEvent(MySensorsEventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    /**
     * Set event type of the update.
     *
     * @param event type of event of the update.
     */
    public void setEventType(MySensorsEventType event) {
        this.eventType = event;
    }

    /**
     * Get event type of the update.
     *
     * @return type of event of the update.
     */
    public MySensorsEventType getEventType() {
        return eventType;
    }

    /**
     *
     * @return Returns the data that was received with the update.
     */
    public Object getData() {
        return data;
    }

    /**
     * Set the content / data of the update.
     *
     * @param data content / data of the update.
     */
    public void setData(MySensorsMessage data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsStatusUpdateEvent other = (MySensorsStatusUpdateEvent) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (eventType != other.eventType) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsStatusUpdateEvent [data=" + data + ", event=" + eventType + "]";
    }
}
