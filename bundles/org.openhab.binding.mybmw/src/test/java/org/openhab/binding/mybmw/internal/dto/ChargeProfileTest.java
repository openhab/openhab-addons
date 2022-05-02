/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeProfile;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;

/**
 * The {@link ChargeProfileTest} is testing locale settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ChargeProfileTest {

    @Test
    public void testWeeklyPlanner() {
        String json = FileReader
                .readFileInString("src/test/resources/responses/chargingprofile/weekly-planner-t2-active.json");
        Vehicle v = Converter.getVehicle(Constants.ANONYMOUS, json);
        ChargeProfile cp = v.status.chargingProfile;
        String cpJson = Converter.getGson().toJson(cp);
        ChargeProfileWrapper cpw = new ChargeProfileWrapper(v.status.chargingProfile);
        assertEquals(cpJson, cpw.getJson(), "JSON comparison");
    }

    @Test
    public void testTwoWeeksPlanner() {
        String json = FileReader.readFileInString("src/test/resources/responses/chargingprofile/two-weeks-timer.json");
        Vehicle v = Converter.getVehicle(Constants.ANONYMOUS, json);
        ChargeProfile cp = v.status.chargingProfile;
        String cpJson = Converter.getGson().toJson(cp);
        ChargeProfileWrapper cpw = new ChargeProfileWrapper(v.status.chargingProfile);
        assertEquals(cpJson, cpw.getJson(), "JSON comparison");
    }
}
