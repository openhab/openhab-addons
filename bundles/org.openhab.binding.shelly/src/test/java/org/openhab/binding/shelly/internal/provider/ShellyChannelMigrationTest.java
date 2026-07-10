/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@NonNullByDefault
public class ShellyChannelMigrationTest {

    @BeforeAll
    static void initChannelDefinitions() {
        // CHANNEL_DEFINITIONS is populated once at (OSGi) construction time
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    @ParameterizedTest
    @CsvSource({ CHANNEL_METER_CURRENTWATTS + "," + CHANNEL_METER_CURRENTPOWER,
            CHANNEL_METER_TOTALKWH + "," + CHANNEL_METER_TOTALENERGY,
            CHANNEL_EMETER_TOTALRET + "," + CHANNEL_EMETER_RETURNEDENERGY,
            CHANNEL_DEVST_ACCUWATTS + "," + CHANNEL_DEVST_ACCUMULATEDPOWER,
            CHANNEL_EMETER_REACTWATTS + "," + CHANNEL_EMETER_REACTPOWER,
            CHANNEL_DEVST_ACCUTOTAL + "," + CHANNEL_DEVST_TOTALENERGY,
            CHANNEL_NMETER_MTRESHHOLD + "," + CHANNEL_NMETER_THRESHOLD,
            CHANNEL_DEVST_ACCURETURNED + "," + CHANNEL_DEVST_ACCURETURNEDENERGY, })
    void deprecatedChannelHasReplacement(String deprecated, String replacement) {
        assertEquals(replacement, ShellyChannelDefinitions.getReplacementChannelName(deprecated));
    }

    @ParameterizedTest
    @CsvSource({ "meter1," + CHANNEL_METER_CURRENTWATTS + "," + CHANNEL_METER_CURRENTPOWER,
            "meter3," + CHANNEL_METER_TOTALKWH + "," + CHANNEL_METER_TOTALENERGY,
            "device," + CHANNEL_DEVST_TOTALKWH + "," + CHANNEL_DEVST_TOTALENERGY,
            "device," + CHANNEL_DEVST_ACCUWATTS + "," + CHANNEL_DEVST_ACCUMULATEDPOWER,
            "nmeter," + CHANNEL_NMETER_MTRESHHOLD + "," + CHANNEL_NMETER_THRESHOLD, })
    void replacementChannelIdPreservesGroup(String group, String deprecated, String replacement) {
        assertEquals(group + "#" + replacement,
                ShellyChannelDefinitions.getReplacementChannelId(group + "#" + deprecated));
    }

    @ParameterizedTest
    @CsvSource({ CHANNEL_METER_ENERGYAVG1MIN, CHANNEL_METER_TOTALENERGY })
    void newChannelNamesHaveNoReplacement(String channel) {
        assertNull(ShellyChannelDefinitions.getReplacementChannelName(channel));
    }

    @Test
    void lastPower1HasNoDualWriteMapping() {
        // lastPower1 (W) must NOT forward to energyAvg1Min (Wh): the units are incompatible and
        // the dual-write would post W states to Wh items on every poll. Write sites post both
        // channels explicitly; only the migration rule links the pair for channel creation.
        assertNull(ShellyChannelDefinitions.getReplacementChannelName(CHANNEL_METER_LASTMIN1));
    }

    @Test
    void deviceGroupTotalEnergyDefinitionResolvable() {
        // The group-qualified migration rule device#totalKWH → device#totalEnergy relies on
        // createChannel() resolving the device-group definition — not the meter-group one.
        assertNotNull(ShellyChannelDefinitions.getDefinition("device#" + CHANNEL_DEVST_TOTALENERGY));
        // device#totalKWH itself has no definition anymore: existing Things carry the channel
        // instance from the previous binding version; the rule only creates the replacement.
        assertThrows(IllegalArgumentException.class,
                () -> ShellyChannelDefinitions.getDefinition("device#" + CHANNEL_DEVST_TOTALKWH));
    }

    @Test
    void meterGroupMinuteEnergyDefinitionsResolvableForAllIndexedGroups() {
        // meter1..meterN map to the meter-group definitions
        assertNotNull(ShellyChannelDefinitions.getDefinition("meter#" + CHANNEL_METER_ENERGYAVG1MIN));
        assertNotNull(ShellyChannelDefinitions.getDefinition("meter1#" + CHANNEL_METER_ENERGYAVG1MIN));
        assertNotNull(ShellyChannelDefinitions.getDefinition("meter3#" + CHANNEL_METER_ENERGYAVG1MIN));
    }

    @Test
    void minuteEnergyChannelsNotDefinedInDeviceGroup() {
        assertThrows(IllegalArgumentException.class,
                () -> ShellyChannelDefinitions.getDefinition("device#" + CHANNEL_METER_ENERGYAVG1MIN));
    }
}
