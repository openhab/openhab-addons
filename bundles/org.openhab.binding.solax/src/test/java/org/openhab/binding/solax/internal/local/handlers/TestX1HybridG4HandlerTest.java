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
 * The {@link TestX1HybridG4HandlerTest} verifies the full flow from raw JSON API data through the
 * {@link org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler} to channel state updates for the
 * X1 Hybrid G4 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX1HybridG4HandlerTest extends AbstractInverterHandlerTest {

    private static final String RAW_DATA = """
            {
                sn:SOME_SERIAL_NUMBER,
                ver:3.008.10,
                type:15,
                Data:[
                    2388,21,460,4998,4483,4483,10,1,487,65,
                    2,59781,0,70,12180,500,605,33,99,12000,
                    0,23159,0,57,100,0,39,4501,0,0,
                    0,0,12,0,13240,0,63348,2,448,43,
                    256,1314,900,0,350,311,279,33,33,279,1,1,652,0,708,1,65077,65535,65386,65535,0,0,0,0,0,0,0,0,0,0,0,0,65068,65535,4500,0,61036,65535,10,0,90,0,0,12,0,116,7,57,0,0,2320,0,110,0,0,0,0,0,0,12544,7440,5896,594,521,9252,0,0,0,0,0,1,1201,0,0,3342,3336,7296,54,21302,14389,18753,12852,16692,12355,13618,21302,14389,18753,12852,16692,12355,13618,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1025,4609,1026,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                Information:[7.500,15,H4752TI1063020,8,1.24,0.00,1.21,1.03,0.00,1]}
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertChannels() {
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE, 238.8, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT, 2.1, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER, 460, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY, 49.98, Units.HERTZ);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE, 448.3, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE, 448.3, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT, 1.0, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT, 0.1, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER, 487, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER, 65, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_VOLTAGE, 121.8, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_CURRENT, 5.0, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_POWER, 605, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_TEMPERATURE, 33, SIUnits.CELSIUS);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_STATE_OF_CHARGE, 99, Units.PERCENT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_FEED_IN_POWER, 12, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_POWER_USAGE, 448, Units.WATT);

        assertStringChannel(SolaxBindingConstants.CHANNEL_INVERTER_WORKMODE, "2");

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY, 7.0, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_PV_ENERGY, 5978.1, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_FEED_IN_ENERGY, 132.4, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_CONSUMPTION, 1944.2, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_FEED_IN_ENERGY, 4.3, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY, 5.7, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_CHARGE_ENERGY, 11.6, Units.KILOWATT_HOUR);
    }
}
