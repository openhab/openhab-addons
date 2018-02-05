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
 * Response for {@link ZoneLabelRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class ZoneLabelResponse extends AbstractResponse {

    private int zoneNo;
    private String zoneName;

    private ZoneLabelResponse(int zoneNo, String zoneName) {
        super(true);
        this.zoneNo = zoneNo;
        this.zoneName = zoneName;
    }

    private ZoneLabelResponse(int zoneNo) {
        super(false);
        this.zoneNo = zoneNo;
    }

    public static ZoneLabelResponse failure(int zoneNo) {
        return new ZoneLabelResponse(zoneNo);
    }

    public static ZoneLabelResponse success(int zoneNo, String zoneName) {
        return new ZoneLabelResponse(zoneNo, zoneName);
    }

    public int getZoneNo() {
        return zoneNo;
    }

    public String getZoneName() {
        return zoneName;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneLabelResponse(this);
    }
}
