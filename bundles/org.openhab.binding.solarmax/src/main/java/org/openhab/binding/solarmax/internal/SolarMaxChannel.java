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
package org.openhab.binding.solarmax.internal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarmax.internal.connector.SolarMaxCommandKey;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SolarMaxChannel} Enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public enum SolarMaxChannel {

    CHANNEL_LAST_UPDATED("lastUpdated", null), //
    CHANNEL_STARTUPS(SolarMaxCommandKey.startups.name(), null),
    CHANNEL_AC_PHASE1_CURRENT(SolarMaxCommandKey.acPhase1Current.name(), Units.AMPERE),
    CHANNEL_AC_PHASE2_CURRENT(SolarMaxCommandKey.acPhase2Current.name(), Units.AMPERE),
    CHANNEL_AC_PHASE3_CURRENT(SolarMaxCommandKey.acPhase3Current.name(), Units.AMPERE),
    CHANNEL_ENERGY_GENERATED_TODAY(SolarMaxCommandKey.energyGeneratedToday.name(), Units.WATT_HOUR),
    CHANNEL_ENERGY_GENERATED_TOTAL(SolarMaxCommandKey.energyGeneratedTotal.name(), Units.WATT_HOUR),
    CHANNEL_OPERATING_HOURS(SolarMaxCommandKey.operatingHours.name(), Units.HOUR),
    CHANNEL_ENERGY_GENERATED_YESTERDAY(SolarMaxCommandKey.energyGeneratedYesterday.name(), Units.WATT_HOUR),
    CHANNEL_ENERGY_GENERATED_LAST_MONTH(SolarMaxCommandKey.energyGeneratedLastMonth.name(), Units.WATT_HOUR),
    CHANNEL_ENERGY_GENERATED_LAST_YEAR(SolarMaxCommandKey.energyGeneratedLastYear.name(), Units.WATT_HOUR),
    CHANNEL_ENERGY_GENERATED_THIS_MONTH(SolarMaxCommandKey.energyGeneratedThisMonth.name(), Units.WATT_HOUR),
    CHANNEL_ENERGY_GENERATED_THIS_YEAR(SolarMaxCommandKey.energyGeneratedThisYear.name(), Units.WATT_HOUR),
    CHANNEL_CURRENT_POWER_GENERATED(SolarMaxCommandKey.currentPowerGenerated.name(), Units.WATT),
    CHANNEL_AC_FREQUENCY(SolarMaxCommandKey.acFrequency.name(), Units.HERTZ),
    CHANNEL_AC_PHASE1_VOLTAGE(SolarMaxCommandKey.acPhase1Voltage.name(), Units.VOLT),
    CHANNEL_AC_PHASE2_VOLTAGE(SolarMaxCommandKey.acPhase2Voltage.name(), Units.VOLT),
    CHANNEL_AC_PHASE3_VOLTAGE(SolarMaxCommandKey.acPhase3Voltage.name(), Units.VOLT),
    CHANNEL_HEAT_SINK_TEMPERATUR(SolarMaxCommandKey.heatSinkTemperature.name(), SIUnits.CELSIUS);

    private final String channelId;

    @Nullable
    private Unit<?> unit;

    private SolarMaxChannel(String channelId, @Nullable Unit<?> unit) {
        this.channelId = channelId;
        this.unit = unit;
    }

    public String getChannelId() {
        return channelId;
    }

    @Nullable
    public Unit<?> getUnit() {
        return this.unit;
    }
}
