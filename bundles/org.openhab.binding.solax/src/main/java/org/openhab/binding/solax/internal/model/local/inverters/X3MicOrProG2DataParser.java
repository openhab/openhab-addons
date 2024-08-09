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
import org.openhab.binding.solax.internal.model.local.X3MicOrProG2Data;

/**
 * The {@link X3MicOrProG2DataParser} is the implementation that parses raw data into a SinglePhaseInverterData for the
 * X3 Mic / Pro G2 inverter.
 *
 * @author Henrik Tóth - Initial contribution
 *         (based on X1/X3 G4 parser from Konstantin Polihronov)
 */
@NonNullByDefault
public class X3MicOrProG2DataParser implements RawDataParser {

    private static final Set<String> X3_MIC_OR_PRO_G2_SUPPORTED_CHANNELS = Set.of(
            CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1, CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2,
            CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3, CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1,
            CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2, CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3,
            CHANNEL_INVERTER_OUTPUT_POWER_PHASE1, CHANNEL_INVERTER_OUTPUT_POWER_PHASE2,
            CHANNEL_INVERTER_OUTPUT_POWER_PHASE3, CHANNEL_INVERTER_PV1_VOLTAGE, CHANNEL_INVERTER_PV2_VOLTAGE,
            CHANNEL_INVERTER_PV1_CURRENT, CHANNEL_INVERTER_PV2_CURRENT, CHANNEL_INVERTER_PV1_POWER,
            CHANNEL_INVERTER_PV2_POWER, CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1,
            CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2, CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3, CHANNEL_TOTAL_ENERGY,
            CHANNEL_TODAY_ENERGY, CHANNEL_INVERTER_TOTAL_OUTPUT_POWER, CHANNEL_INVERTER_TEMPERATURE1,
            CHANNEL_INVERTER_TEMPERATURE2, CHANNEL_INVERTER_WORKMODE, CHANNEL_RAW_DATA);

    @Override
    public LocalData getData(LocalConnectRawDataBean rawData) {
        return new X3MicOrProG2Data(rawData);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return X3_MIC_OR_PRO_G2_SUPPORTED_CHANNELS;
    }
}
