/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.Gson;

/**
 * The {@link LocaleTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LocaleTest {
    private static final Gson GSON = new Gson();

    @Test
    public void languageTest() {
        assertTrue("United Kingdom", ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.UK.getCountry()));
        assertTrue("United States", ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.US.getCountry()));
        assertFalse("France", ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.FRANCE.getCountry()));
        assertFalse("Germany", ConnectedDriveConstants.IMPERIAL_COUNTRIES.contains(Locale.GERMAN.getCountry()));
    }

    @Test
    public void testTimeUTCToLocaleTime() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        VehicleStatusContainer status = GSON.fromJson(resource1, VehicleStatusContainer.class);
        VehicleStatus vStatus = status.vehicleStatus;
        assertEquals("Input  DateTime", "2020-08-24T15:55:32", vStatus.internalDataTimeUTC);
        assertEquals("Output DateTime", "24.08.2020 17:55", Converter.getLocalDateTime(vStatus.internalDataTimeUTC));
    }

    @Test
    public void testServiceDatePattern() {
        String pattern = "2021-11-01";
        LocalDate ldt = LocalDate.parse(pattern, Converter.SERVICE_DATE_INPUT_PATTERN);
        assertEquals("Parsed Date", "2021-11-01", ldt.toString());
        assertEquals("Parsed Date", "Nov 2021", ldt.format(Converter.SERVICE_DATE_OUTPUT_PATTERN));
    }

    @Test
    public void testTimeZoneDateTime() {
        String pattern = "2018-07-16T21:47:46+0000";
        assertEquals("ZonedDateTime", "16.07.2018 23:47", Converter.getZonedDateTime(pattern));
    }

    @Test
    public void testLocalTime() {
        String time = "05:23";
        LocalTime t = LocalTime.parse(time);
        assertEquals("Time", "05:23", t.toString());
    }
}
