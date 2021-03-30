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
    CHANNEL_LAST_UPDATED("lastUpdated"), //
    // CHANNEL_DEVICE_ADDRESS(SolarMaxCommandKey.DeviceAddress.name()),
    CHANNEL_SOFTWARE_VERSION(SolarMaxCommandKey.softwareVersion.name()),
    CHANNEL_BUILD_NUMBER(SolarMaxCommandKey.buildNumber.name()),
    CHANNEL_STARTUPS(SolarMaxCommandKey.startups.name()),
    CHANNEL_AC_PHASE1_CURRENT(SolarMaxCommandKey.acPhase1Current.name()),
    CHANNEL_AC_PHASE2_CURRENT(SolarMaxCommandKey.acPhase2Current.name()),
    CHANNEL_AC_PHASE3_CURRENT(SolarMaxCommandKey.acPhase3Current.name()),
    CHANNEL_ENERGY_GENERATED_TODAY(SolarMaxCommandKey.energyGeneratedToday.name()),
    CHANNEL_ENERGY_GENERATED_TOTAL(SolarMaxCommandKey.energyGeneratedTotal.name()),
    CHANNEL_OPERATING_HOURS(SolarMaxCommandKey.operatingHours.name()),
    CHANNEL_ENERGY_GENERATED_YESTERDAY(SolarMaxCommandKey.energyGeneratedYesterday.name()),
    CHANNEL_ENERGY_GENERATED_LAST_MONTH(SolarMaxCommandKey.energyGeneratedLastMonth.name()),
    CHANNEL_ENERGY_GENERATED_LAST_YEAR(SolarMaxCommandKey.energyGeneratedLastYear.name()),
    CHANNEL_ENERGY_GENERATED_THIS_MONTH(SolarMaxCommandKey.energyGeneratedThisMonth.name()),
    CHANNEL_ENERGY_GENERATED_THIS_YEAR(SolarMaxCommandKey.energyGeneratedThisYear.name()),
    CHANNEL_CURRENT_POWER_GENERATED(SolarMaxCommandKey.currentPowerGenerated.name()),
    CHANNEL_AC_FREQUENCY(SolarMaxCommandKey.acFrequency.name()),
    CHANNEL_AC_PHASE1_VOLTAGE(SolarMaxCommandKey.acPhase1Voltage.name()),
    CHANNEL_AC_PHASE2_VOLTAGE(SolarMaxCommandKey.acPhase2Voltage.name()),
    CHANNEL_AC_PHASE3_VOLTAGE(SolarMaxCommandKey.acPhase3Voltage.name()),
    CHANNEL_HEAT_SINK_TEMPERATUR(SolarMaxCommandKey.heatSinkTemperature.name())

    ;

    private final String channelId;

    private SolarMaxChannel(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
