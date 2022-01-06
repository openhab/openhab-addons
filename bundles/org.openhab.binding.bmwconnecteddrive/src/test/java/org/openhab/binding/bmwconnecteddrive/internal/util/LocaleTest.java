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
package org.openhab.binding.bmwconnecteddrive.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.library.types.DateTimeType;

import com.google.gson.Gson;

/**
 * The {@link LocaleTest} is testing locale settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class LocaleTest {
    private static final Gson GSON = new Gson();

    @Test
    public void languageTest() {
        assertTrue(ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.UK.getCountry()), "United Kingdom");
        assertTrue(ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.US.getCountry()), "United States");
        assertFalse(ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.FRANCE.getCountry()), "France");
        assertFalse(ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.GERMAN.getCountry()), "Germany");
    }

    public void testTimeUTCToLocaleTime() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        VehicleStatusContainer status = GSON.fromJson(resource1, VehicleStatusContainer.class);
        VehicleStatus vStatus = status.vehicleStatus;

        String inputTime = vStatus.internalDataTimeUTC;
        String localeTime = Converter.getLocalDateTime(inputTime);
        String dateTimeType = DateTimeType.valueOf(localeTime).toString();
        assertEquals("2020-08-24T15:55:32", inputTime, "Input DateTime");
        assertEquals("2020-08-24T17:55:32", localeTime, "Output DateTime");
        assertEquals("2020-08-24T17:55:32.000+0200", dateTimeType, "DateTimeType Value");

        inputTime = vStatus.updateTime;
        localeTime = Converter.getLocalDateTime(inputTime);
        dateTimeType = DateTimeType.valueOf(localeTime).toString();
        assertEquals("2020-08-24T15:55:32+0000", inputTime, "Input DateTime");
        assertEquals("2020-08-24T17:55:32", localeTime, "Output DateTime");
        assertEquals("2020-08-24T17:55:32.000+0200", dateTimeType, "DateTimeType Value");

        inputTime = vStatus.updateTime;
        localeTime = Converter.getLocalDateTimeWithoutOffest(inputTime);
        dateTimeType = DateTimeType.valueOf(localeTime).toString();
        assertEquals("2020-08-24T15:55:32+0000", inputTime, "Input DateTime");
        assertEquals("2020-08-24T15:55:32", localeTime, "Output DateTime");
        assertEquals("2020-08-24T15:55:32.000+0200", dateTimeType, "DateTimeType Value");
    }

    @Test
    public void testDistance() {
        double lat = 45.678;
        double lon = 8.765;
        double distance = 0.005;
        double dist = Converter.measureDistance(lat, lon, lat + distance, lon + distance);
        assertTrue(dist < 1, "Distance below 1 km");
    }
}
