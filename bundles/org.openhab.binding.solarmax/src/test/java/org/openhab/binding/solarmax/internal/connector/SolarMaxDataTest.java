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
package org.openhab.binding.solarmax.internal.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;

/**
 * The {@link SolarMaxDataTest} class is used to test the {@link SolaMaxData} class.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxDataTest {

    @Test
    public void dataDateTimeGetterSetterTest() throws Exception {
        // dataDateTime shouldn't be a problem, but check it anyway
        ZonedDateTime dateTimeOriginal = ZonedDateTime.now();
        ZonedDateTime dateTimeUpdated = dateTimeOriginal.plusDays(2);

        SolarMaxData solarMaxData = new SolarMaxData();

        solarMaxData.setDataDateTime(dateTimeOriginal);
        assertEquals(new DateTimeType(dateTimeOriginal), solarMaxData.getDataDateTime());

        solarMaxData.setDataDateTime(dateTimeUpdated);
        assertEquals(new DateTimeType(dateTimeUpdated), solarMaxData.getDataDateTime());
    }

    @Test
    public void valueGetterSetterTest() throws Exception {
        String startupsOriginal = "3B8B"; // 15243 in hex
        String startupsUpdated = "3B8C"; // 15244 in hex

        SolarMaxData solarMaxData = new SolarMaxData();

        Map<SolarMaxCommandKey, @Nullable String> dataOrig = new HashMap<>();
        dataOrig.put(SolarMaxCommandKey.startups, startupsOriginal);
        solarMaxData.setData(dataOrig);

        @Nullable
        Number origVersion = solarMaxData.get(SolarMaxCommandKey.startups);

        assertNotNull(origVersion);
        assertEquals(Integer.parseInt(startupsOriginal, 16), origVersion.intValue());

        Map<SolarMaxCommandKey, @Nullable String> dataUpdated = new HashMap<>();
        dataUpdated.put(SolarMaxCommandKey.startups, startupsUpdated);
        solarMaxData.setData(dataUpdated);
        Number updatedVersion = solarMaxData.get(SolarMaxCommandKey.startups);
        assertNotEquals(origVersion, updatedVersion);
    }
}
