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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceStatus} is responsible for storing
 * the "devicestatus" node
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class DeviceStatus {
    @SerializedName("StatusCode")
    private int statusCode;
    @SerializedName("MgmtTimerRemainingTime")
    private int mgmtTimerRemainingTime;
    @SerializedName("ErrorCode")
    private int errorCode;
    @SerializedName("LEDColor")
    private int ledColor;
    @SerializedName("LEDState")
    private int ledState;
    @SerializedName("StateToReset")
    private boolean stateToReset;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getMgmtTimerRemainingTime() {
        return mgmtTimerRemainingTime;
    }

    public void setMgmtTimerRemainingTime(int mgmtTimerRemainingTime) {
        this.mgmtTimerRemainingTime = mgmtTimerRemainingTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getLedColor() {
        return ledColor;
    }

    public void setLedColor(int ledColor) {
        this.ledColor = ledColor;
    }

    public int getLedState() {
        return ledState;
    }

    public void setLedState(int ledState) {
        this.ledState = ledState;
    }

    public boolean isStateToReset() {
        return stateToReset;
    }

    public void setStateToReset(boolean stateToReset) {
        this.stateToReset = stateToReset;
    }
}
