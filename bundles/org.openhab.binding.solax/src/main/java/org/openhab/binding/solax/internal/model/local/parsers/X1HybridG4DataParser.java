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
package org.openhab.binding.solax.internal.model.local.parsers;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.local.LocalInverterData;
import org.openhab.binding.solax.internal.model.local.X1HybridG4InverterData;

/**
 * The {@link X1HybridG4DataParser} is the implementation that parses raw data into a LocalInverterData for the
 * X1 Hybrid G4 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1HybridG4DataParser implements RawDataParser {

    private static final Set<String> X1_HYBRID_G4_SUPPORTED_CHANNELS = Set.of(CHANNEL_INVERTER_PV1_POWER,
            CHANNEL_INVERTER_PV1_VOLTAGE, CHANNEL_INVERTER_PV1_CURRENT, CHANNEL_INVERTER_PV2_POWER,
            CHANNEL_INVERTER_PV2_VOLTAGE, CHANNEL_INVERTER_PV2_CURRENT, CHANNEL_INVERTER_PV_TOTAL_POWER,
            CHANNEL_INVERTER_PV_TOTAL_CURRENT, CHANNEL_BATTERY_POWER, CHANNEL_BATTERY_VOLTAGE, CHANNEL_BATTERY_CURRENT,
            CHANNEL_BATTERY_TEMPERATURE, CHANNEL_BATTERY_STATE_OF_CHARGE, CHANNEL_FEED_IN_POWER, CHANNEL_TIMESTAMP,
            CHANNEL_RAW_DATA, CHANNEL_INVERTER_OUTPUT_POWER, CHANNEL_INVERTER_OUTPUT_CURRENT,
            CHANNEL_INVERTER_OUTPUT_VOLTAGE, CHANNEL_INVERTER_OUTPUT_FREQUENCY, CHANNEL_INVERTER_WORKMODE);

    @Override
    public LocalInverterData getData(LocalConnectRawDataBean rawData) {
        return new X1HybridG4InverterData(rawData);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return X1_HYBRID_G4_SUPPORTED_CHANNELS;
    }
}
