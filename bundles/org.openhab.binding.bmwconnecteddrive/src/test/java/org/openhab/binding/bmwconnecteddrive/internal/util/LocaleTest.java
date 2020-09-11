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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.library.types.DateTimeType;

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

        String inputTime = vStatus.internalDataTimeUTC;
        String localeTime = Converter.getLocalDateTime(inputTime);
        String dateTimeType = DateTimeType.valueOf(localeTime).toString();
        assertEquals("Input DateTime", "2020-08-24T15:55:32", inputTime);
        assertEquals("Output DateTime", "2020-08-24T17:55:32", localeTime);
        assertEquals("DateTimeType Value", "2020-08-24T17:55:32", dateTimeType);

        inputTime = vStatus.updateTime;
        localeTime = Converter.getLocalDateTime(inputTime);
        dateTimeType = DateTimeType.valueOf(localeTime).toString();
        assertEquals("Input DateTime", "2020-08-24T15:55:32+0000", inputTime);
        assertEquals("Output DateTime", "2020-08-24T17:55:32", localeTime);
        assertEquals("DateTimeType Value", "2020-08-24T17:55:32", dateTimeType);
    }
}
