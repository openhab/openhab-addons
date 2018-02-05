/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

/**
 * Command for requesting zone status information from PRT3 device
 *
 * @author Robert Michalak - Initial contribution
 */

public class ZoneStatusRequest implements DigiplexRequest {

    private int zoneNo;

    public ZoneStatusRequest(int zoneNo) {
        this.zoneNo = zoneNo;
    }

    @Override
    public String getSerialMessage() {
        return String.format("RZ%03d\r", zoneNo);
    }

}
