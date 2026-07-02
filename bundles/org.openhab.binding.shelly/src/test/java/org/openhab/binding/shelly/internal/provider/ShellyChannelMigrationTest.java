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
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@NonNullByDefault
public class ShellyChannelMigrationTest {

    @ParameterizedTest
    @CsvSource({ CHANNEL_METER_CURRENTWATTS + "," + CHANNEL_METER_CURRENTPOWER,
            CHANNEL_METER_TOTALKWH + "," + CHANNEL_METER_TOTALENERGY,
            CHANNEL_EMETER_TOTALRET + "," + CHANNEL_EMETER_RETURNEDENERGY,
            CHANNEL_DEVST_ACCUWATTS + "," + CHANNEL_DEVST_ACCUMULATEDPOWER,
            CHANNEL_EMETER_REACTWATTS + "," + CHANNEL_EMETER_REACTPOWER,
            CHANNEL_DEVST_ACCUTOTAL + "," + CHANNEL_DEVST_TOTALENERGY,
            CHANNEL_METER_LASTMIN1 + "," + CHANNEL_METER_LASTENERGY1,
            CHANNEL_NMETER_MTRESHHOLD + "," + CHANNEL_NMETER_THRESHOLD,
            CHANNEL_DEVST_ACCURETURNED + "," + CHANNEL_DEVST_ACCURETURNEDENERGY, })
    void deprecatedChannelHasReplacement(String deprecated, String replacement) {
        assertEquals(replacement, ShellyChannelDefinitions.getReplacementChannelName(deprecated));
    }

    @ParameterizedTest
    @CsvSource({ "meter1," + CHANNEL_METER_CURRENTWATTS + "," + CHANNEL_METER_CURRENTPOWER,
            "meter3," + CHANNEL_METER_TOTALKWH + "," + CHANNEL_METER_TOTALENERGY,
            "device," + CHANNEL_DEVST_ACCUWATTS + "," + CHANNEL_DEVST_ACCUMULATEDPOWER,
            "nmeter," + CHANNEL_NMETER_MTRESHHOLD + "," + CHANNEL_NMETER_THRESHOLD, })
    void replacementChannelIdPreservesGroup(String group, String deprecated, String replacement) {
        assertEquals(group + "#" + replacement,
                ShellyChannelDefinitions.getReplacementChannelId(group + "#" + deprecated));
    }
}
