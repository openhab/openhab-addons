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
 * The {@link TestEvChargerHandlerTest} verifies the full flow from raw JSON API data through the
 * {@link org.openhab.binding.solax.internal.handlers.SolaxLocalAccessChargerHandler} to channel state updates for the
 * EV Charger.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestEvChargerHandlerTest extends AbstractChargerHandlerTest {

    private static final String RAW_DATA = """
            {
                "SN":"SQBLABLA",
                "ver":"3.004.11",
                "type":1,
                "Data":[
                    2,2,23914,23991,23895,1517,1513,1519,3654,3657,
                    3656,10968,44,0,346,0,65434,35463,65459,65508,
                    65513,27,402,0,43,0,2,15,0,0,
                    0,0,0,5004,5000,4996,10518,1547,6150,4,
                    0,0,0,0,0,0,0,0,1,100,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    1717,0,3114,1547,6150,0,1,1,1,0,
                    0,121,584,266,0,50,0,0,1,1,0],
                "Information":[11.000,1,"CXXXXXXXXXX",1,1.13,1.01,0.00,0.00,0.00,1],
                "OCPPServer":"",
                "OCPPChargerId":""
            }
            """;

    @Override
    protected String getRawData() {
        return RAW_DATA;
    }

    @Override
    protected void assertChannels() {
        assertStringChannel(SolaxBindingConstants.CHANNEL_CHARGER_STATE, "2");
        assertStringChannel(SolaxBindingConstants.CHANNEL_CHARGER_MODE, "2");

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EQ_SINGLE_SESSION, 4.4, Units.KILOWATT_HOUR);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EQ_TOTAL, 34.6, Units.KILOWATT_HOUR);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE1, 15.17, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE2, 15.13, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE3, 15.19, Units.AMPERE);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE1, 239.14, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE2, 239.91, Units.VOLT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE3, 238.95, Units.VOLT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE1, 3654, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE2, 3657, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE3, 3656, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_TOTAL_OUTPUT_POWER, 10968, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE1, -1.02, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE2, -300.73, Units.AMPERE);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE3, -0.77, Units.AMPERE);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE1, -28, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE2, -23, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE3, 27, Units.WATT);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_TOTAL_EXTERNAL_POWER, 402, Units.WATT);

        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_PLUG_TEMPERATURE, 0, SIUnits.CELSIUS);
        assertQuantityChannel(SolaxBindingConstants.CHANNEL_CHARGER_INTERNAL_TEMPERATURE, 43, SIUnits.CELSIUS);
    }
}
