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
 * Response for {@link AreaStatusRequest}
 *
 * @author Robert Michalak - Initial contribution
 */
public class AreaStatusResponse extends AbstractResponse {

    private int areaNo;
    private AreaStatus status;
    private boolean zoneInMemory;
    private boolean trouble;
    private boolean ready;
    private boolean inProgramming;
    private boolean alarm;
    private boolean strobe;

    private AreaStatusResponse(int areaNo, AreaStatus status, boolean zoneInMemory, boolean trouble, boolean ready,
            boolean inProgramming, boolean alarm, boolean strobe) {
        this.areaNo = areaNo;
        this.status = status;
        this.zoneInMemory = zoneInMemory;
        this.trouble = trouble;
        this.ready = ready;
        this.inProgramming = inProgramming;
        this.alarm = alarm;
        this.strobe = strobe;
    }

    private AreaStatusResponse(int areaNo) {
        super(false);
    }

    public static AreaStatusResponse failure(int areaNo) {
        return new AreaStatusResponse(areaNo);
    }

    public static AreaStatusResponse success(int areaNo, AreaStatus status, boolean zoneInMemory, boolean trouble,
            boolean ready, boolean inProgramming, boolean alarm, boolean strobe) {
        return new AreaStatusResponse(areaNo, status, zoneInMemory, trouble, ready, inProgramming, alarm, strobe);
    }

    public int getAreaNo() {
        return areaNo;
    }

    public AreaStatus getStatus() {
        return status;
    }

    public boolean isZoneInMemory() {
        return zoneInMemory;
    }

    public boolean isTrouble() {
        return trouble;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isInProgramming() {
        return inProgramming;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public boolean isStrobe() {
        return strobe;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleAreaStatusResponse(this);
    }

}
