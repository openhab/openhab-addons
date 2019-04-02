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
package org.openhab.binding.digiplex.communication;

/**
 * Response for {@link ZoneStatusRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class ZoneStatusResponse extends AbstractResponse {

    public final int zoneNo;
    public final ZoneStatus status;
    public final boolean alarm;
    public final boolean fireAlarm;
    public final boolean supervisionLost;
    public final boolean lowBattery;

    private ZoneStatusResponse(int zoneNo, ZoneStatus status, boolean alarm, boolean fireAlarm, boolean supervisionLost,
            boolean lowBattery) {
        super(true);
        this.zoneNo = zoneNo;
        this.status = status;
        this.alarm = alarm;
        this.fireAlarm = fireAlarm;
        this.supervisionLost = supervisionLost;
        this.lowBattery = lowBattery;
    }

    private ZoneStatusResponse(int zoneNo) {
        super(false);
        this.zoneNo = zoneNo;
        this.status = null;
        this.alarm = false;
        this.fireAlarm = false;
        this.supervisionLost = false;
        this.lowBattery = false;
    }

    /**
     * Builds a response for a given zoneNo. Indicates that request failed.
     */
    public static ZoneStatusResponse failure(int zoneNo) {
        return new ZoneStatusResponse(zoneNo);
    }

    /**
     * Builds a response for a given zoneNo. Indicates that request was successful.
     */
    public static ZoneStatusResponse success(int zoneNo, ZoneStatus status, boolean alarm, boolean fireAlarm,
            boolean supervisionLost, boolean lowBattery) {
        return new ZoneStatusResponse(zoneNo, status, alarm, fireAlarm, supervisionLost, lowBattery);
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneStatusResponse(this);
    }
}
