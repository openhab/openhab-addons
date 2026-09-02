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
package org.openhab.binding.solax.internal.local.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link TestX3MicOrProG2HandlerTest} verifies the full flow from raw JSON API data through the
 * {@link org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler} to channel state updates for the
 * X3 Mic / Pro G2 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX3MicOrProG2HandlerTest extends AbstractInverterHandlerTest {

    private static final String RAW_DATA = """
            {
                sn:XYZ,
                ver:3.003.02,
                type:16,Data:[
                    2515,2449,2484,5,5,9,54,44,20,4080,4340,
                    0,1,2,0,67,102,0,4999,4999,4999,2,19035,
                    0,50,8000,5,9,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,
                    0,0,0,0,0,0,0,0,120,40,1,6,5,0,6772,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0
                    ],
                Information:[8.000,16,XY,8,1.15,0.00,1.11,1.01,0.00,1]
             }
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertChannels() {
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1, 251.5, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2, 244.9, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3, 248.4, Units.VOLT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1, 0.5, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2, 0.5, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3, 0.9, Units.AMPERE);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE1, 54, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE2, 44, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE3, 20, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE, 408.0, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE, 434.0, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT, 0.1, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT, 0.2, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER, 67, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER, 102, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1, 49.99, Units.HERTZ);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2, 49.99, Units.HERTZ);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3, 49.99, Units.HERTZ);

        assertStringChannel(SolaxBindingConstants.CHANNEL_INVERTER_WORKMODE, "2");

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_TOTAL_OUTPUT_POWER, 120, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY, 1903.5, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY, 5.0, Units.KILOWATT_HOUR);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_TEMPERATURE1, 5, SIUnits.CELSIUS);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_TEMPERATURE2, 9, SIUnits.CELSIUS);
    }
}
