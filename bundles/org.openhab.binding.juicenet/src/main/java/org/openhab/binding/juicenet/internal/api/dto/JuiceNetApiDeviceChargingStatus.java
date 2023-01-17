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
package org.openhab.binding.juicenet.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@link JuiceNetDeviceChargingStatus } implements DTO for device charging status
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiDeviceChargingStatus {
    @SerializedName("amps_limit")
    public int ampsLimit;
    @SerializedName("amps_current")
    public float ampsCurrent;
    public int voltage;
    @SerializedName("wh_energy")
    public int whEnergy;
    public int savings;
    @SerializedName("watt_power")
    public int wattPower;
    @SerializedName("seconds_charging")
    public int secondsCharging;
    @SerializedName("wh_energy_at_plugin")
    public int whEnergyAtPlugin;
    @SerializedName("wh_energy_to_add")
    public int whEnergyToAdd;
    public int flags;
}
