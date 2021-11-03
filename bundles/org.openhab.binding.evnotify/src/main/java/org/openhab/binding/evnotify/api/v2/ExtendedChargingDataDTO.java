/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api.v2;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the extended data that is returned by evnotify v2 API.
 *
 * e.g.
 *
 * {
 * "soh": 100,
 * "charging": 1,
 * "rapid_charge_port": 0,
 * "normal_charge_port": 1,
 * "slow_charge_port": null,
 * "aux_battery_voltage": 14.5,
 * "dc_battery_voltage": 362.1,
 * "dc_battery_current": -8.7,
 * "dc_battery_power": -3.15027,
 * "cumulative_energy_charged": 3881.5,
 * "cumulative_energy_discharged": 3738.8,
 * "battery_min_temperature": 25,
 * "battery_max_temperature": 26,
 * "battery_inlet_temperature": 24,
 * "external_temperature": null,
 * "odo": null,
 * "last_extended": 1631220014
 * }
 *
 * @author Michael Schmidt - Initial contribution
 */
public class ExtendedChargingDataDTO {

    @SerializedName("charging")
    public Integer charging;

    @SerializedName("rapid_charge_port")
    public Integer rapidChargePort;

    @SerializedName("normal_charge_port")
    public Integer normalChargePort;

    @SerializedName("slow_charge_port")
    public Integer slowChargePort;

    @SerializedName("soh")
    public Float stateOfHealth;

    @SerializedName("aux_battery_voltage")
    public Float auxBatteryVoltage;

    @SerializedName("dc_battery_voltage")
    public Float dcBatteryVoltage;

    @SerializedName("dc_battery_current")
    public Float dcBatteryCurrent;

    @SerializedName("cumulative_energy_charged")
    public Float cumulativeEnergyCharged;

    @SerializedName("cumulative_energy_discharged")
    public Float cumulativeEnergyDischarged;

    @SerializedName("battery_min_temperature")
    public Float batteryMinTemperature;

    @SerializedName("battery_max_temperature")
    public Float batteryMaxTemperature;

    @SerializedName("battery_inlet_temperature")
    public Float batteryInletTemperature;

    @SerializedName("external_temperature")
    public Float externalTemperature;

    @SerializedName("last_extended")
    public Integer lastExtended;

    @SerializedName("dc_battery_power")
    public Float dcBatteryPower;

    public Float getCumulativeEnergyCharged() {
        return cumulativeEnergyCharged;
    }

    public Float getCumulativeEnergyDischarged() {
        return cumulativeEnergyDischarged;
    }

    public Float getBatteryMinTemperature() {
        return batteryMinTemperature;
    }

    public Float getBatteryMaxTemperature() {
        return batteryMaxTemperature;
    }

    public Float getBatteryInletTemperature() {
        return batteryInletTemperature;
    }

    public Float getExternalTemperature() {
        return externalTemperature;
    }

    public OffsetDateTime getLastExtended() {
        return lastExtended == null ? null
                : OffsetDateTime.from(Instant.ofEpochMilli(lastExtended).atZone(ZoneId.of("Europe/Berlin")));
    }

    public Float getAuxBatteryVoltage() {
        return auxBatteryVoltage;
    }

    public Float getDcBatteryVoltage() {
        return dcBatteryVoltage;
    }

    public Float getDcBatteryCurrent() {
        return dcBatteryCurrent;
    }

    public Float getDcBatteryPower() {
        return dcBatteryPower;
    }

    public Boolean isCharging() {
        return charging != null && charging == 1;
    }

    public Boolean isRapidChargePort() {
        return rapidChargePort != null && rapidChargePort == 1;
    }

    public Boolean isNormalChargePort() {
        return normalChargePort != null && normalChargePort == 1;
    }

    public Boolean isSlowChargePort() {
        return slowChargePort != null && slowChargePort == 1;
    }

    public Float getStateOfHealth() {
        return stateOfHealth;
    }
}
