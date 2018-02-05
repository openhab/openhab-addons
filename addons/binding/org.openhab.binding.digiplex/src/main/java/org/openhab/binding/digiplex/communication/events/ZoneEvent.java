/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication.events;

import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;

/**
 * Message providing miscellaneous zone informations
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class ZoneEvent extends AbstractEvent {

    private int zoneNo;
    private ZoneEventType type;

    public ZoneEvent(int zoneNo, ZoneEventType type, int areaNo) {
        super(areaNo);
        this.zoneNo = zoneNo;
        this.type = type;
    }

    public int getZoneNo() {
        return zoneNo;
    }

    public ZoneEventType getType() {
        return type;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneEvent(this);
    }
}
