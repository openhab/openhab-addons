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
