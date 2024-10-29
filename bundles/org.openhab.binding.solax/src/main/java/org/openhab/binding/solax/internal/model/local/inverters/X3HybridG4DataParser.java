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
package org.openhab.binding.solax.internal.model.local.inverters;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.local.LocalData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;
import org.openhab.binding.solax.internal.model.local.X3HybridG4Data;

/**
 * The {@link X3HybridG4DataParser} is the implementation that parses raw data into a {@link LocalData} for the
 * X3 Hybrid G4 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X3HybridG4DataParser implements RawDataParser {

    private static final Set<String> X3_HYBRID_G4_SUPPORTED_CHANNELS = Set.of(CHANNEL_INVERTER_PV1_POWER,
            CHANNEL_INVERTER_PV1_VOLTAGE, CHANNEL_INVERTER_PV1_CURRENT, CHANNEL_INVERTER_PV2_POWER,
            CHANNEL_INVERTER_PV2_VOLTAGE, CHANNEL_INVERTER_PV2_CURRENT, CHANNEL_INVERTER_PV_TOTAL_POWER,
            CHANNEL_INVERTER_PV_TOTAL_CURRENT, CHANNEL_BATTERY_POWER, CHANNEL_BATTERY_VOLTAGE, CHANNEL_BATTERY_CURRENT,
            CHANNEL_BATTERY_TEMPERATURE, CHANNEL_BATTERY_STATE_OF_CHARGE, CHANNEL_FEED_IN_POWER, CHANNEL_TIMESTAMP,
            CHANNEL_RAW_DATA, CHANNEL_INVERTER_OUTPUT_POWER_PHASE1, CHANNEL_INVERTER_OUTPUT_POWER_PHASE2,
            CHANNEL_INVERTER_OUTPUT_POWER_PHASE3, CHANNEL_INVERTER_TOTAL_OUTPUT_POWER,
            CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1, CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2,
            CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3, CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1,
            CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2, CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3,
            CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1, CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2,
            CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3, CHANNEL_POWER_USAGE, CHANNEL_TOTAL_ENERGY,
            CHANNEL_TOTAL_BATTERY_CHARGE_ENERGY, CHANNEL_TOTAL_PV_ENERGY, CHANNEL_TOTAL_CONSUMPTION,
            CHANNEL_TODAY_ENERGY, CHANNEL_TODAY_FEED_IN_ENERGY, CHANNEL_TODAY_CONSUMPTION,
            CHANNEL_TODAY_BATTERY_CHARGE_ENERGY, CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY, CHANNEL_INVERTER_WORKMODE,
            CHANNEL_TOTAL_FEED_IN_ENERGY, CHANNEL_TOTAL_BATTERY_DISCHARGE_ENERGY);

    @Override
    public LocalData getData(LocalConnectRawDataBean rawData) {
        return new X3HybridG4Data(rawData);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return X3_HYBRID_G4_SUPPORTED_CHANNELS;
    }
}
