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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceDTO} class represents a heater device
 *
 * @author Arne Seime - Initial contribution
 */
public class DeviceDTO {
    public boolean heaterFlag;
    public int subDomainId;
    public int controlType;
    public double currentTemp;
    public boolean canChangeTemp;
    public long deviceId;
    public String deviceName;
    @SerializedName("mac")
    public String macAddress;
    public int deviceStatus;
    public int holidayTemp;
    public boolean fanStatus;
    @SerializedName("open")
    public boolean openWindow;
    public boolean powerStatus;
    @SerializedName("isHoliday")
    public boolean holiday;
}
