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
 * The {@link TestX3HybridG4HandlerTest} verifies the full flow from raw JSON API data through the
 * {@link org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler} to channel state updates for the
 * X3 Hybrid G4 inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX3HybridG4HandlerTest extends AbstractInverterHandlerTest {

    private static final String RAW_DATA = """
            {
                sn:XYZ,
                ver:3.005.01,
                type:14,
                Data:[
                    2316,2329,2315,18,18,18,372,363,365,1100,
                    12,23,34,45,56,67,4996,4996,4996,2,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,1,65494,65535,0,0,0,31330,
                    320,1034,3078,1,44,1100,256,1294,0,0,
                    7445,5895,100,0,38,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,588,1,
                    396,0,0,0,102,0,142,0,62,110,
                    570,0,463,0,0,0,1925,0,369,0,
                    506,1925,304,309,0,0,0,0,0,0,
                    0,0,0,45,1,59,1,34,54,256,
                    3504,2400,300,300,295,276,33,33,2,1620,779,15163,15163,14906,0,0,0,3270,3264,45581,0,20564,12339,18753,12353,18742,12356,13625,20564,12339,18754,12866,18743,14151,13104,20564,12339,18754,12866,18743,14151,12592,20564,12339,18754,12865,18738,12871,13620,0,0,0,0,0,0,0,1025,8195,769,259,0,31460,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                Information:[12.000,14,XY,8,1.23,0.00,1.24,1.09,0.00,1]
             }
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertChannels() {
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1, 231.6, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2, 232.9, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3, 231.5, Units.VOLT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1, 1.8, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2, 1.8, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3, 1.8, Units.AMPERE);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE1, 372, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE2, 363, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE3, 365, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_TOTAL_OUTPUT_POWER, 1100, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE, 1.2, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE, 2.3, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT, 3.4, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT, 4.5, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER, 56, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER, 67, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1, 49.96, Units.HERTZ);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2, 49.96, Units.HERTZ);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3, 49.96, Units.HERTZ);

        assertStringChannel(SolaxBindingConstants.CHANNEL_INVERTER_WORKMODE, "2");

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_FEED_IN_POWER, -42, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_VOLTAGE, 2.59, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_CURRENT, 3.2, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_POWER, 1034, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_STATE_OF_CHARGE, 45, Units.PERCENT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_BATTERY_TEMPERATURE, 59, SIUnits.CELSIUS);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_POWER_USAGE, 1294, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY, 6612.4, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_DISCHARGE_ENERGY, 10.2, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_CHARGE_ENERGY, 14.2, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_PV_ENERGY, 57, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_FEED_IN_ENERGY, 19.25, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TOTAL_CONSUMPTION, 3.69, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY, 39.6, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_FEED_IN_ENERGY, 1261573.06, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_CONSUMPTION, 202509.28, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY, 6.2, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_CHARGE_ENERGY, 11, Units.KILOWATT_HOUR);
    }
}
