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
package org.openhab.binding.fenecon.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fenecon.internal.FeneconBindingConstants;

/**
 * Test for {@link BatteryPower}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class BatteryPowerTest {

    @Test
    void testCharging() {
        BatteryPower batteryPower = BatteryPower
                .get(new FeneconResponse(FeneconBindingConstants.ESS_DISCHARGE_POWER_ADDRESS, "comment", "-1777"));
        assertEquals(1777, batteryPower.chargerPower());
        assertEquals(0, batteryPower.dischargerPower());
    }

    @Test
    void testDischarging() {
        BatteryPower batteryPower = BatteryPower
                .get(new FeneconResponse(FeneconBindingConstants.ESS_DISCHARGE_POWER_ADDRESS, "comment", "1777"));
        assertEquals(1777, batteryPower.dischargerPower());
        assertEquals(0, batteryPower.chargerPower());
    }
}
