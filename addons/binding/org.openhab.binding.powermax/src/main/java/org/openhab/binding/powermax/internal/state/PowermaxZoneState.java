/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

/**
 * A class to store the state of a zone
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxZoneState {

    private Boolean tripped;
    private Long lastTripped;
    private Boolean lowBattery;
    private Boolean bypassed;
    private Boolean armed;

    public PowermaxZoneState() {
        tripped = null;
        lastTripped = null;
        lowBattery = null;
        bypassed = null;
        armed = null;
    }

    public Boolean isTripped() {
        return tripped;
    }

    public void setTripped(Boolean tripped) {
        this.tripped = tripped;
    }

    public Long getLastTripped() {
        return lastTripped;
    }

    public void setLastTripped(Long lastTripped) {
        this.lastTripped = lastTripped;
    }

    public boolean isLastTripBeforeTime(long refTime) {
        return isTripped() == Boolean.TRUE && getLastTripped() != null && getLastTripped() < refTime;
    }

    public Boolean isLowBattery() {
        return lowBattery;
    }

    public void setLowBattery(Boolean lowBattery) {
        this.lowBattery = lowBattery;
    }

    public Boolean isBypassed() {
        return bypassed;
    }

    public void setBypassed(Boolean bypassed) {
        this.bypassed = bypassed;
    }

    public Boolean isArmed() {
        return armed;
    }

    public void setArmed(Boolean armed) {
        this.armed = armed;
    }

}
