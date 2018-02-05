/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
