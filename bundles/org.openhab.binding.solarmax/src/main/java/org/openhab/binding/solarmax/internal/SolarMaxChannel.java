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
package org.openhab.binding.solarmax.internal;

import org.openhab.binding.solarmax.internal.connector.SolarMaxCommandKey;

/**
 * The {@link SolarMaxChannel} Enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Jamie Townsend - Initial contribution
 */
public enum SolarMaxChannel {

    // CHANNEL_UPDATE_VALUES_FROM_DEVICE("UpdateValuesFromDevice"),
    CHANNEL_LAST_UPDATED("LastUpdated"), //
    // CHANNEL_DEVICE_ADDRESS(SolarMaxCommandKey.DeviceAddress.name()),
    CHANNEL_SOFTWARE_VERSION(SolarMaxCommandKey.SoftwareVersion.name()),
    CHANNEL_BUILD_NUMBER(SolarMaxCommandKey.BuildNumber.name()),
    CHANNEL_STARTUPS(SolarMaxCommandKey.Startups.name()),
    CHANNEL_AC_PHASE1_CURRENT(SolarMaxCommandKey.AcPhase1Current.name()),
    CHANNEL_AC_PHASE2_CURRENT(SolarMaxCommandKey.AcPhase2Current.name()),
    CHANNEL_AC_PHASE3_CURRENT(SolarMaxCommandKey.AcPhase3Current.name()),
    CHANNEL_ENERGY_GENERATED_TODAY(SolarMaxCommandKey.EnergyGeneratedToday.name()),
    CHANNEL_ENERGY_GENERATED_TOTAL(SolarMaxCommandKey.EnergyGeneratedTotal.name()),
    CHANNEL_OPERATING_HOURS(SolarMaxCommandKey.OperatingHours.name()),
    CHANNEL_ENERGY_GENERATED_YESTERDAY(SolarMaxCommandKey.EnergyGeneratedYesterday.name()),
    CHANNEL_ENERGY_GENERATED_LAST_MONTH(SolarMaxCommandKey.EnergyGeneratedLastMonth.name()),
    CHANNEL_ENERGY_GENERATED_LAST_YEAR(SolarMaxCommandKey.EnergyGeneratedLastYear.name()),
    CHANNEL_ENERGY_GENERATED_THIS_MONTH(SolarMaxCommandKey.EnergyGeneratedThisMonth.name()),
    CHANNEL_ENERGY_GENERATED_THIS_YEAR(SolarMaxCommandKey.EnergyGeneratedThisYear.name()),
    CHANNEL_CURRENT_POWER_GENERATED(SolarMaxCommandKey.CurrentPowerGenerated.name()),
    CHANNEL_AC_FREQUENCY(SolarMaxCommandKey.AcFrequency.name()),
    CHANNEL_AC_PHASE1_VOLTAGE(SolarMaxCommandKey.AcPhase1Voltage.name()),
    CHANNEL_AC_PHASE2_VOLTAGE(SolarMaxCommandKey.AcPhase2Voltage.name()),
    CHANNEL_AC_PHASE3_VOLTAGE(SolarMaxCommandKey.AcPhase3Voltage.name()),
    CHANNEL_HEAT_SINK_TEMPERATUR(SolarMaxCommandKey.HeatSinkTemperature.name())

    ;

    private final String channelId;

    private SolarMaxChannel(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
