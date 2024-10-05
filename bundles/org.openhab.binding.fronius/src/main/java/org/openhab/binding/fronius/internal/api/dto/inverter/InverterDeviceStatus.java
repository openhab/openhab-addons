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
package org.openhab.binding.fronius.internal.api.dto.inverter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterDeviceStatus} is responsible for storing
 * the "DeviceStatus" node of the {@link InverterRealtimeBodyData}.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class InverterDeviceStatus {
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

    public int getMgmtTimerRemainingTime() {
        return mgmtTimerRemainingTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getLedColor() {
        return ledColor;
    }

    public int getLedState() {
        return ledState;
    }

    public boolean isStateToReset() {
        return stateToReset;
    }
}
