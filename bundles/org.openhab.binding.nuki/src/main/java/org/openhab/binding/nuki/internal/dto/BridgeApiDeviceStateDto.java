/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.dto;

/**
 * Base class for responses with Nuki device state
 *
 * @author Jan Vybíral - Initial contribution
 */
public class BridgeApiDeviceStateDto {
    private int mode;
    private int state;
    private String stateName;
    private boolean batteryCritical;
    private Boolean batteryCharging;
    private Integer batteryChargeState;
    private Boolean keypadBatteryCritical;
    private Integer doorsensorState;
    private String doorsensorStateName;
    private String ringactionTimestamp;
    private Boolean ringactionState;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }

    public Boolean getBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(Boolean batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public Integer getBatteryChargeState() {
        return batteryChargeState;
    }

    public void setBatteryChargeState(Integer batteryChargeState) {
        this.batteryChargeState = batteryChargeState;
    }

    public Boolean getKeypadBatteryCritical() {
        return keypadBatteryCritical;
    }

    public void setKeypadBatteryCritical(Boolean keypadBatteryCritical) {
        this.keypadBatteryCritical = keypadBatteryCritical;
    }

    public Integer getDoorsensorState() {
        return doorsensorState;
    }

    public void setDoorsensorState(Integer doorsensorState) {
        this.doorsensorState = doorsensorState;
    }

    public String getRingactionTimestamp() {
        return ringactionTimestamp;
    }

    public void setRingactionTimestamp(String ringactionTimestamp) {
        this.ringactionTimestamp = ringactionTimestamp;
    }

    public Boolean getRingactionState() {
        return ringactionState;
    }

    public void setRingactionState(Boolean ringactionState) {
        this.ringactionState = ringactionState;
    }

    public String getDoorsensorStateName() {
        return doorsensorStateName;
    }

    public void setDoorsensorStateName(String doorsensorStateName) {
        this.doorsensorStateName = doorsensorStateName;
    }
}
