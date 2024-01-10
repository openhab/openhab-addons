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
package org.openhab.binding.juicenet.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@link JuiceNetApiDeviceStatus } implements DTO for Device Status
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiDeviceStatus {
    @SerializedName("ID")
    public String id = "";
    @SerializedName("info_timestamp")
    public Long infoTimestamp = (long) 0;
    @SerializedName("show_override")
    public boolean showOverride;
    public String state = "";
    public JuiceNetApiDeviceChargingStatus charging = new JuiceNetApiDeviceChargingStatus();
    public JuiceNetApiDeviceLifetimeStatus lifetime = new JuiceNetApiDeviceLifetimeStatus();
    @SerializedName("charging_time_left")
    public int chargingTimeLeft;
    @SerializedName("plug_unplug_time")
    public Long plugUnplugTime = (long) 0;
    @SerializedName("target_time")
    public Long targetTime = (long) 0;
    @SerializedName("unit_time")
    public Long unitTime = (long) 0;
    @SerializedName("utc_time")
    public Long utcTime = (long) 0;
    @SerializedName("default_target_time")
    public long defaultTargetTime = 0;
    @SerializedName("car_id")
    public int carId;
    public int temperature;
    public String message = "";
}
