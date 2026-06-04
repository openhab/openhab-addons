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
import org.openhab.core.library.unit.Units;

/**
 * The {@link TestX1BoostAirMiniHandlerTest} verifies the full flow from raw JSON API data through the
 * {@link org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler} to channel state updates for the
 * X1 Boost / Air / Mini inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX1BoostAirMiniHandlerTest extends AbstractInverterHandlerTest {

    private static final String RAW_DATA = """
            {
                sn:SR***,
                ver:3.006.04,
                type:4,
                Data:[
                    2263,7,128,1519,0,9,0,138,0,5000,
                    2,15569,0,7,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,13,
                    0,4071,0,3456,0,0,0,0,0,0,
                    0,0,0,0,0,23,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
                ],
                Information:[1.500,4,XM3A15IA669518,8,2.27,0.00,1.43,0.00,0.00,1]}
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertChannels() {
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE, 226.3, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT, 0.7, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER, 128, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE, 151.9, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE, 0, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT, 0.9, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT, 0, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER, 138, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER, 0, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY, 50.0, Units.HERTZ);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY, 1556.9, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY, 0.7, Units.KILOWATT_HOUR);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_POWER_USAGE, 346, Units.WATT);
    }
}
