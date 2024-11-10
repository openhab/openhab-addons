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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.exception.FMIExceptionReportException;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.xml.sax.SAXException;

/**
 * Test cases for {@link org.openhab.binding.fmiweather.internal.client.Client#parseMultiPointCoverageXml}
 * with an "empty" (no data) XML response
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingEmptyTest extends AbstractFMIResponseParsingTest {

    private static final String OBSERVATIONS = "observations_empty.xml";

    @NonNullByDefault({})
    private FMIResponse observationsResponse;

    @BeforeEach
    public void setUp() throws FMIUnexpectedResponseException, FMIExceptionReportException, XPathExpressionException,
            SAXException, IOException {
        client = new ClientExposed();
        observationsResponse = client.parseMultiPointCoverageXml(readTestResourceUtf8(OBSERVATIONS));
        assertNotNull(observationsResponse);
    }

    @Test
    public void testLocationsSinglePlace() {
        assertThat(observationsResponse.getLocations().size(), is(0));
    }
}
