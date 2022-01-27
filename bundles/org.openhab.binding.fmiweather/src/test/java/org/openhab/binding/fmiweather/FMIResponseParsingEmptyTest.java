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
package org.openhab.binding.fmiweather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.Client;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;

/**
 * Test cases for {@link Client.parseMultiPointCoverageXml} with an "empty" (no data) XML response
 *
 * @author Sami Salonen - Initial contribution
 */
public class FMIResponseParsingEmptyTest extends AbstractFMIResponseParsingTest {

    private Path observations = getTestResource("observations_empty.xml");

    private FMIResponse observationsResponse;

    @BeforeEach
    public void setUp() {
        client = new Client();
        try {
            observationsResponse = parseMultiPointCoverageXml(readTestResourceUtf8(observations));
        } catch (Throwable e) {
            throw new RuntimeException("Test data malformed", e);
        }
        assertNotNull(observationsResponse);
    }

    @Test
    public void testLocationsSinglePlace() throws Throwable {
        assertThat(observationsResponse.getLocations().size(), is(0));
    }
}
