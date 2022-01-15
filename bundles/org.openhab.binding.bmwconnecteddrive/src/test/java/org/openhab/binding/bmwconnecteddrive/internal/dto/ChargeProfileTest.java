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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargeProfile;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link ChargeProfileTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ChargeProfileTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testChargeProfile() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/charging-profile.json");
        ChargeProfile cp = GSON.fromJson(resource1, ChargeProfile.class);
        assertNotNull(cp.weeklyPlanner);
        assertNotNull(cp.weeklyPlanner.timer1);
        assertFalse(cp.weeklyPlanner.timer1.timerEnabled);
        assertNotNull(cp.weeklyPlanner.timer1.weekdays);
        assertEquals(5, cp.weeklyPlanner.timer1.weekdays.size(), "Days");
    }
}
