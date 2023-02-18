/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Location;

/**
 * Test cases for Client.parseMultiPointCoverageXml with a xml response having multiple places, parameters
 * and timestamps
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingMultiplePlacesTest extends AbstractFMIResponseParsingTest {

    private Path observationsMultiplePlaces = getTestResource("observations_multiple_places.xml");
    private Path forecastsMultiplePlaces = getTestResource("forecast_multiple_places.xml");

    @NonNullByDefault({})
    private FMIResponse observationsMultiplePlacesResponse;
    @NonNullByDefault({})
    private FMIResponse observationsMultiplePlacesNaNResponse;
    @NonNullByDefault({})
    private FMIResponse forecastsMultiplePlacesResponse;

    // observation station points (observations_multiple_places.xml) have fmisid as their id
    private Location emasalo = new Location("Porvoo Emäsalo", "101023", new BigDecimal("60.20382"),
            new BigDecimal("25.62546"));
    private Location kilpilahti = new Location("Porvoo Kilpilahti satama", "100683", new BigDecimal("60.30373"),
            new BigDecimal("25.54916"));
    private Location harabacka = new Location("Porvoo Harabacka", "101028", new BigDecimal("60.39172"),
            new BigDecimal("25.60730"));

    // forecast points (forecast_multiple_places.xml) have latitude,longitude as their id
    private Location maarianhamina = new Location("Mariehamn", "60.09726,19.93481", new BigDecimal("60.09726"),
            new BigDecimal("19.93481"));
    private Location pointWithNoName = new Location("19.9,61.0973", "61.09726,19.90000", new BigDecimal("61.09726"),
            new BigDecimal("19.90000"));

    @BeforeEach
    public void setUp() {
        try {
            observationsMultiplePlacesResponse = parseMultiPointCoverageXml(
                    readTestResourceUtf8(observationsMultiplePlaces));
            observationsMultiplePlacesNaNResponse = parseMultiPointCoverageXml(
                    readTestResourceUtf8(observationsMultiplePlaces).replace("276.0", "NaN"));
            forecastsMultiplePlacesResponse = parseMultiPointCoverageXml(readTestResourceUtf8(forecastsMultiplePlaces));
        } catch (Throwable e) {
            throw new RuntimeException("Test data malformed", e);
        }
    }

    @Test
    public void testLocationsMultiplePlacesObservations() {
        // locations
        assertThat(observationsMultiplePlacesResponse.getLocations().size(), is(3));
        assertThat(observationsMultiplePlacesResponse.getLocations(),
                hasItems(deeplyEqualTo(emasalo), deeplyEqualTo(kilpilahti), deeplyEqualTo(harabacka)));
    }

    @Test
    public void testLocationsMultiplePlacesForecasts() {
        // locations
        assertThat(forecastsMultiplePlacesResponse.getLocations().size(), is(2));
        assertThat(forecastsMultiplePlacesResponse.getLocations(),
                hasItems(deeplyEqualTo(maarianhamina), deeplyEqualTo(pointWithNoName)));
    }

    @Test
    public void testParametersMultipleObservations() {
        for (Location location : new Location[] { emasalo, kilpilahti, harabacka }) {
            Optional<Set<String>> parametersOptional = observationsMultiplePlacesResponse.getParameters(location);
            Set<String> parameters = parametersOptional.get();
            assertThat(parameters.size(), is(6));
            assertThat(parameters, hasItems("wd_10min", "wg_10min", "rh", "p_sea", "ws_10min", "t2m"));
        }
    }

    @Test
    public void testParametersMultipleForecasts() {
        for (Location location : new Location[] { maarianhamina, pointWithNoName }) {
            Optional<Set<String>> parametersOptional = forecastsMultiplePlacesResponse.getParameters(location);
            Set<String> parameters = parametersOptional.get();
            assertThat(parameters.size(), is(2));
            assertThat(parameters, hasItems("Temperature", "Humidity"));
        }
    }

    @Test
    public void testParseObservationsMultipleData() {
        Data wd_10min = observationsMultiplePlacesResponse.getData(emasalo, "wd_10min").get();
        assertThat(wd_10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", "276.0")));
        Data rh = observationsMultiplePlacesResponse.getData(kilpilahti, "rh").get();
        assertThat(rh, is(deeplyEqualTo(1552215600L, 60, "73.0", "65.0", "60.0", "59.0", "57.0", "64.0", "66.0", "65.0",
                "71.0", "77.0")));
    }

    @Test
    public void testParseForecastsMultipleData() {
        long start = 1668340800;
        Data temperature = forecastsMultiplePlacesResponse.getData(maarianhamina, "Temperature").get();
        assertThat(temperature, is(deeplyEqualTo(start, 360, "9.2", "4.8", "7.4", "5.6", "7.7", "7.9", "7.6", null,
                null, null, null, null, null, null)));
        Data temperature2 = forecastsMultiplePlacesResponse.getData(pointWithNoName, "Temperature").get();
        assertThat(temperature2, is(deeplyEqualTo(start, 360, "7.6", "7.6", "8.0", "6.2", "7.6", "7.3", "6.1", null,
                null, null, null, null, null, null)));

        Data humidity = forecastsMultiplePlacesResponse.getData(maarianhamina, "Humidity").get();
        assertThat(humidity, is(deeplyEqualTo(start, 360, "73.9", "98.0", "92.7", "94.9", "91.4", "92.1", "95.0", null,
                null, null, null, null, null, null)));
        Data humidity2 = forecastsMultiplePlacesResponse.getData(pointWithNoName, "Humidity").get();
        assertThat(humidity2, is(deeplyEqualTo(start, 360, "84.8", "89.9", "92.4", "99.3", "88.3", "88.7", "93.9", null,
                null, null, null, null, null, null)));
    }

    @Test
    public void testParseObservations1NaN() {
        // last value is null, due to NaN measurement value
        Data wd_10min = observationsMultiplePlacesNaNResponse.getData(emasalo, "wd_10min").get();
        assertThat(wd_10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", null)));
    }
}
