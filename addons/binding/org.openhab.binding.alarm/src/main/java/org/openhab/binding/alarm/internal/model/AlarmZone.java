/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal.model;

import org.openhab.binding.alarm.internal.AlarmException;
import org.openhab.binding.alarm.internal.config.AlarmZoneConfig;

/**
 * Represents an alarm zone.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class AlarmZone {
    private String id;
    private boolean closed = true;
    private AlarmZoneType type = AlarmZoneType.ACTIVE;

    /**
     * Only for testing.
     */
    public AlarmZone(String id, AlarmZoneType type) {
        this.id = id;
        this.type = type;
    }

    public AlarmZone(String id, AlarmZoneConfig config) throws AlarmException {
        this.id = id;
        if (config != null && config.getType() != null) {
            this.type = AlarmZoneType.parse(config.getType());
        }
    }

    /**
     * Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns true, if the alarm zone is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Sets the closed status of the alarm zone.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Returns the alarm zone type.
     */
    public AlarmZoneType getType() {
        return type;
    }

    /**
     * Sets the alarm zone type.
     */
    public void setType(AlarmZoneType type) {
        this.type = type;
    }
}
