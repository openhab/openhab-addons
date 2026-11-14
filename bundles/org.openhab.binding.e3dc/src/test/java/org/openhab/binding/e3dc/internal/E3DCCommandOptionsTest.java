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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.types.CommandOption;

/**
 * Tests the energy choices the reserve and the manual charge offer, which follow the battery the device reports.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCCommandOptionsTest {

    @Test
    void theReserveIsOfferedUpToTheEnergyTheDeviceAccepts() {
        List<CommandOption> options = E3DCCommandOptions.reserve(11813.5078125);

        assertEquals(12, options.size());
        assertEquals("0 kWh", options.get(0).getCommand());
        assertEquals("11 kWh", options.get(11).getCommand());
    }

    @Test
    void aReserveBelowOneKilowattHourOffersOnlyNoReserve() {
        assertEquals(List.of(new CommandOption("0 kWh", "0 kWh")), E3DCCommandOptions.reserve(900));
    }

    @Test
    void theManualChargeIsOfferedUpToTheEnergyTheBatteryHolds() {
        List<CommandOption> options = E3DCCommandOptions.manualCharge(12976.0);

        assertEquals(25, options.size());
        assertEquals("500 Wh", options.get(0).getCommand());
        assertEquals("12500 Wh", options.get(24).getCommand());
    }

    @Test
    void aBatterySmallerThanTheStepOffersNoManualCharge() {
        assertTrue(E3DCCommandOptions.manualCharge(400).isEmpty());
    }

    @Test
    void everyChoiceNamesItself() {
        for (CommandOption option : E3DCCommandOptions.reserve(5000)) {
            assertEquals(option.getCommand(), option.getLabel());
        }
    }
}
