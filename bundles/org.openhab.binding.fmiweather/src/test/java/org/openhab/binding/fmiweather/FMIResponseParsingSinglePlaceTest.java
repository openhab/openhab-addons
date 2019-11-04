/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.fmiweather;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Location;

/**
 * Test cases for Client.parseMultiPointCoverageXml with a xml response having single place and multiple
 * parameters
 * and timestamps
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingSinglePlaceTest extends AbstractFMIResponseParsingTest {

    private Path observations1 = getTestResource("observations_single_place.xml");

    @NonNullByDefault({})
    private FMIResponse observationsResponse1;
    @NonNullByDefault({})
    private FMIResponse observationsResponse1NaN;
    private Location emasalo = new Location("Porvoo Emäsalo", "101023", new BigDecimal("60.20382"),
            new BigDecimal("25.62546"));

    @Before
    public void setUp() throws Throwable {
        observationsResponse1 = parseMultiPointCoverageXml(readTestResourceUtf8(observations1));
        observationsResponse1NaN = parseMultiPointCoverageXml(
                readTestResourceUtf8(observations1).replace("276.0", "NaN"));

        assertNotNull(observationsResponse1);
    }

    @Test
    public void testLocationsSinglePlace() throws Throwable {
        assertThat(observationsResponse1.getLocations().size(), is(1));
        assertThat(observationsResponse1.getLocations().stream().findFirst().get(), deeplyEqualTo(emasalo));
    }

    @Test
    public void testParameters() throws Throwable {
        // parameters
        Optional<@NonNull Set<@NonNull String>> parametersOptional = observationsResponse1.getParameters(emasalo);
        Set<@NonNull String> parameters = parametersOptional.get();
        assertThat(parameters.size(), is(6));
        assertThat(parameters, hasItems("wd_10min", "wg_10min", "rh", "p_sea", "ws_10min", "t2m"));
    }

    @Test
    public void testGetDataWithInvalidArguments() throws Throwable {
        Location loc = observationsResponse1.getLocations().stream().findAny().get();
        // Invalid parameter or location (fmisid)
        assertThat(observationsResponse1.getData(loc, "foobar"), is(Optional.empty()));
        assertThat(observationsResponse1.getData(
                new Location("Porvoo Emäsalo", "9999999", new BigDecimal("60.20382"), new BigDecimal("25.62546")),
                "rh"), is(Optional.empty()));
    }

    @Test
    public void testParseObservations1Data() throws Throwable {
        Data wd_10min = observationsResponse1.getData(emasalo, "wd_10min").get();
        assertThat(wd_10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", "276.0")));
    }

    @Test
    public void testParseObservations1NaN() throws Throwable {
        // last value is null, due to NaN measurement value
        Data wd_10min = observationsResponse1NaN.getData(emasalo, "wd_10min").get();
        assertThat(wd_10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", null)));
    }

}
