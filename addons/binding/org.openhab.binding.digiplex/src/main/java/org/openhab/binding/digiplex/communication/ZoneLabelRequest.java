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
 * Command for requesting zone label information from PRT3 device
 *
 * @author Robert Michalak
 */

public class ZoneLabelRequest implements DigiplexRequest {

    private int zoneNo;

    public ZoneLabelRequest(int zoneNo) {
        this.zoneNo = zoneNo;
    }

    @Override
    public String getSerialMessage() {
        return String.format("ZL%03d\r", zoneNo);
    }

}
