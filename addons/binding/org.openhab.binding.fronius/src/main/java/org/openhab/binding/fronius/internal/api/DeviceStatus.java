/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
