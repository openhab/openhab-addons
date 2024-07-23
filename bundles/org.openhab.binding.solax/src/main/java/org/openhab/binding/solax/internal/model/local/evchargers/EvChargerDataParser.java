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
package org.openhab.binding.solax.internal.model.local.evchargers;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.local.EvChargerData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;

/**
 * The {@link EvChargerDataParser} is the implementation that parses raw data into a EvChargerData for the EV charger.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class EvChargerDataParser implements RawDataParser {

    private static final Set<String> EV_CHARGER_SUPPORTED_CHANNELS = Set.of(CHANNEL_CHARGER_MODE, CHANNEL_CHARGER_STATE,
            CHANNEL_CHARGER_EQ_SINGLE_SESSION, CHANNEL_CHARGER_EQ_TOTAL, CHANNEL_CHARGER_OUTPUT_POWER_PHASE1,
            CHANNEL_CHARGER_OUTPUT_POWER_PHASE2, CHANNEL_CHARGER_OUTPUT_POWER_PHASE3,
            CHANNEL_CHARGER_TOTAL_OUTPUT_POWER, CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE1,
            CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE2, CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE3,
            CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE1, CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE2,
            CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE3, CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE1,
            CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE2, CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE3,
            CHANNEL_CHARGER_EXTERNAL_POWER_PHASE1, CHANNEL_CHARGER_EXTERNAL_POWER_PHASE2,
            CHANNEL_CHARGER_EXTERNAL_POWER_PHASE3, CHANNEL_CHARGER_TOTAL_EXTERNAL_POWER,
            CHANNEL_CHARGER_PLUG_TEMPERATURE, CHANNEL_CHARGER_INTERNAL_TEMPERATURE);

    @Override
    public EvChargerData getData(LocalConnectRawDataBean bean) {
        return new EvChargerData(bean);
    }

    @Override
    public Set<String> getSupportedChannels() {
        return EV_CHARGER_SUPPORTED_CHANNELS;
    }
}
