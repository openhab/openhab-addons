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
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;

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

        String softwareVersionOriginal = "3B8B"; // 15243 in hex
        String softwareVersionUpdated = "3B8C"; // 15244 in hex

        SolarMaxData solarMaxData = new SolarMaxData();

        Map<SolarMaxCommandKey, String> dataOrig = new HashMap<>();
        dataOrig.put(SolarMaxCommandKey.softwareVersion, softwareVersionOriginal);
        solarMaxData.setData(dataOrig);
        DecimalType origVersion = solarMaxData.get(SolarMaxCommandKey.softwareVersion).as(DecimalType.class);
        assertNotNull(origVersion);
        assertEquals(Integer.parseInt(softwareVersionOriginal, 16), origVersion.intValue());

        Map<SolarMaxCommandKey, String> dataUpdated = new HashMap<>();
        dataUpdated.put(SolarMaxCommandKey.softwareVersion, softwareVersionUpdated);
        solarMaxData.setData(dataUpdated);
        DecimalType updatedVersion = solarMaxData.get(SolarMaxCommandKey.softwareVersion).as(DecimalType.class);
        assertNotEquals(origVersion, updatedVersion);
    }
}
