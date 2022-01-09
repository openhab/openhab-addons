/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digiplex.internal.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexResponse;
import org.openhab.binding.digiplex.internal.communication.ZoneStatus;

/**
 * Message indicating zone status.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
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
