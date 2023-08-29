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
package org.openhab.binding.solax.internal.model.parsers;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.SinglePhaseInverterData;
import org.openhab.binding.solax.internal.model.impl.X1HybridG4InverterData;

/**
 * The {@link SinglePhaseDataParser} is the implementation that parses raw data into a SinglePhaseInverterData for the
 * X1 Hybrid G4 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1HybridG4DataParser implements SinglePhaseDataParser {

    private static final Set<String> X1_HYBRID_G4_SUPPORTED_CHANNELS = Set.of(INVERTER_PV1_POWER, INVERTER_PV1_VOLTAGE,
            INVERTER_PV1_CURRENT, INVERTER_PV2_POWER, INVERTER_PV2_VOLTAGE, INVERTER_PV2_CURRENT,
            INVERTER_PV_TOTAL_POWER, INVERTER_PV_TOTAL_CURRENT, BATTERY_POWER, BATTERY_VOLTAGE, BATTERY_CURRENT,
            BATTERY_TEMPERATURE, BATTERY_STATE_OF_CHARGE, FEED_IN_POWER, TIMESTAMP, RAW_DATA, INVERTER_OUTPUT_POWER,
            INVERTER_OUTPUT_CURRENT, INVERTER_OUTPUT_VOLTAGE, INVERTER_OUTPUT_FREQUENCY);

    @Override
    public SinglePhaseInverterData getData(LocalConnectRawDataBean rawData) {
        return new X1HybridG4InverterData(rawData);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return X1_HYBRID_G4_SUPPORTED_CHANNELS;
    }
}
