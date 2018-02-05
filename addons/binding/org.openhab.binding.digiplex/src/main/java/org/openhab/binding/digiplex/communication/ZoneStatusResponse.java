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
 * Response for {@link ZoneStatusRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class ZoneStatusResponse extends AbstractResponse {

    private int zoneNo;
    private ZoneStatus status;
    private boolean alarm;
    private boolean fireAlarm;
    private boolean supervisionLost;
    private boolean lowBattery;

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
    }

    public static ZoneStatusResponse failure(int zoneNo) {
        return new ZoneStatusResponse(zoneNo);
    }

    public static ZoneStatusResponse success(int zoneNo, ZoneStatus status, boolean alarm, boolean fireAlarm,
            boolean supervisionLost, boolean lowBattery) {
        return new ZoneStatusResponse(zoneNo, status, alarm, fireAlarm, supervisionLost, lowBattery);
    }

    public int getZoneNo() {
        return zoneNo;
    }

    public ZoneStatus getStatus() {
        return status;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public boolean isFireAlarm() {
        return fireAlarm;
    }

    public boolean isSupervisionLost() {
        return supervisionLost;
    }

    public boolean isLowBattery() {
        return lowBattery;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneStatusResponse(this);
    }
}
