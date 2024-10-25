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
package org.openhab.binding.fmiweather.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.exception.FMIExceptionReportException;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.xml.sax.SAXException;

/**
 * Test cases for {@link {@link org.openhab.binding.fmiweather.internal.client.Client#parseMultiPointCoverageXml}
 * with an xml response having single place and multiple parameters and timestamps
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingSinglePlaceTest extends AbstractFMIResponseParsingTest {

    private static final String OBSERVATIONS1 = "observations_single_place.xml";

    @NonNullByDefault({})
    private FMIResponse observationsResponse1;
    @NonNullByDefault({})
    private FMIResponse observationsResponse1NaN;
    private Location emasalo = new Location("Porvoo Emäsalo", "101023", new BigDecimal("60.20382"),
            new BigDecimal("25.62546"));

    @BeforeEach
    public void setUp() throws FMIUnexpectedResponseException, FMIExceptionReportException, XPathExpressionException,
            SAXException, IOException {
        observationsResponse1 = client.parseMultiPointCoverageXml(readTestResourceUtf8(OBSERVATIONS1));
        observationsResponse1NaN = client
                .parseMultiPointCoverageXml(readTestResourceUtf8(OBSERVATIONS1).replace("276.0", "NaN"));
        assertNotNull(observationsResponse1);
    }

    @Test
    public void testLocationsSinglePlace() {
        assertThat(observationsResponse1.getLocations().size(), is(1));
        assertThat(observationsResponse1.getLocations().stream().findFirst().get(), deeplyEqualTo(emasalo));
    }

    @Test
    public void testParameters() {
        // parameters
        Optional<Set<String>> parametersOptional = observationsResponse1.getParameters(emasalo);
        Set<String> parameters = parametersOptional.get();
        assertThat(parameters.size(), is(6));
        assertThat(parameters, hasItems("wd_10min", "wg_10min", "rh", "p_sea", "ws_10min", "t2m"));
    }

    @Test
    public void testGetDataWithInvalidArguments() {
        Location loc = observationsResponse1.getLocations().stream().findAny().get();
        // Invalid parameter or location (fmisid)
        assertThat(observationsResponse1.getData(loc, "foobar"), is(Optional.empty()));
        assertThat(observationsResponse1.getData(
                new Location("Porvoo Emäsalo", "9999999", new BigDecimal("60.20382"), new BigDecimal("25.62546")),
                "rh"), is(Optional.empty()));
    }

    @Test
    public void testParseObservations1Data() {
        Data wd10min = observationsResponse1.getData(emasalo, "wd_10min").get();
        assertThat(wd10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", "276.0")));
    }

    @Test
    public void testParseObservations1NaN() {
        // last value is null, due to NaN measurement value
        Data wd10min = observationsResponse1NaN.getData(emasalo, "wd_10min").get();
        assertThat(wd10min, is(deeplyEqualTo(1552215600L, 60, "312.0", "286.0", "295.0", "282.0", "271.0", "262.0",
                "243.0", "252.0", "262.0", null)));
    }
}
