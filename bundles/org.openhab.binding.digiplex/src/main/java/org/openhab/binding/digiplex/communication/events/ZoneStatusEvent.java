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

import org.openhab.binding.digiplex.communication.DigiplexResponse;
import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.communication.ZoneStatus;

/**
 * Message indicating zone status.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class ZoneStatusEvent extends AbstractEvent implements DigiplexResponse {

    private int zoneNo;
    private ZoneStatus state;

    public ZoneStatusEvent(int zoneNo, ZoneStatus state, int areaNo) {
        super(areaNo);
        this.zoneNo = zoneNo;
        this.state = state;
    }

    public int getZoneNo() {
        return zoneNo;
    }

    public ZoneStatus getStatus() {
        return state;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneStatusEvent(this);

    }
}
