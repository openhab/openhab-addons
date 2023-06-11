/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the room json structure
 * 
 * @author Arne Seime - Initial contribution
 */
public class RoomDTO {
    public long roomId;
    @SerializedName("roomName")
    public String name;
    public int comfortTemp;
    public int sleepTemp;
    public int awayTemp;
    @SerializedName("avgTemp")
    public double currentTemp;
    public String roomProgram;
    public int currentMode = 0;
    public boolean heatStatus = false;
    @SerializedName("onLineDeviceNum")
    public int onlineDeviceCount = 0;
    @SerializedName("offLineDeviceNum")
    public int offLineDeviceCount = 0;
    @SerializedName("total")
    public int totalCount = 0;
    public int independentCount = 0;
    @SerializedName("isOffline")
    public boolean offline = true;
    public String controlSource;
}
