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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.dto.PINRequest;
import org.openhab.binding.mercedesme.internal.server.Utils;

/**
 * The {@link ConfigurationTest} Test configuration settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ConfigurationTest {

    @Test
    void testRound() {
        int socValue = 66;
        double batteryCapacity = 66.5;
        float chargedValue = Math.round(socValue * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(43.89, chargedValue, 0.01);
        float unchargedValue = Math.round((100 - socValue) * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(22.61, unchargedValue, 0.01);
        assertEquals(batteryCapacity, chargedValue + unchargedValue, 0.01);
    }

    @Test
    void testLocale() {
        Locale l = Locale.GERMANY;
        System.out.println(l.getCountry());
        System.out.println(l.toString());
    }

    @Test
    void testRegion() {
        System.out.println(Utils.getLoginAppId(Constants.REGION_EUROPE));
    }

    @Test
    void testGSON() {
        PINRequest pr = new PINRequest("a", "b");
        System.out.println(Utils.GSON.toJson(pr));
    }
}
