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
import org.openhab.binding.solax.internal.model.local.X1BoostAirMiniInverterData;

/**
 * The {@link X1BoostAirMiniDataParser} is the implementation that parses raw data into a LocalInverterData for the
 * X1 Mini / X1 Air Mini or X1 Boost Mini inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1BoostAirMiniDataParser implements RawDataParser {

    private static final Set<String> X1_BOOST_AIR_MINI_SUPPORTED_CHANNELS = Set.of(CHANNEL_INVERTER_PV1_POWER,
            CHANNEL_INVERTER_PV1_VOLTAGE, CHANNEL_INVERTER_PV1_CURRENT, CHANNEL_INVERTER_PV2_POWER,
            CHANNEL_INVERTER_PV2_VOLTAGE, CHANNEL_INVERTER_PV2_CURRENT, CHANNEL_INVERTER_PV_TOTAL_POWER,
            CHANNEL_INVERTER_PV_TOTAL_CURRENT, CHANNEL_TIMESTAMP, CHANNEL_RAW_DATA, CHANNEL_INVERTER_OUTPUT_POWER,
            CHANNEL_INVERTER_OUTPUT_CURRENT, CHANNEL_INVERTER_OUTPUT_VOLTAGE, CHANNEL_INVERTER_OUTPUT_FREQUENCY,
            CHANNEL_TOTAL_ENERGY, CHANNEL_TODAY_ENERGY, CHANNEL_POWER_USAGE);

    @Override
    public LocalInverterData getData(LocalConnectRawDataBean bean) {
        return new X1BoostAirMiniInverterData(bean);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return X1_BOOST_AIR_MINI_SUPPORTED_CHANNELS;
    }
}
