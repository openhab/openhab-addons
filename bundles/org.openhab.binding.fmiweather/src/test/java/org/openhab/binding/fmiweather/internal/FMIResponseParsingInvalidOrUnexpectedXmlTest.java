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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.exception.FMIResponseException;
import org.xml.sax.SAXParseException;

/**
 * Test cases for {@link AbstractWeatherHandler}.
 * The tests provide mocks for supporting entities using Mockito.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingInvalidOrUnexpectedXmlTest extends AbstractFMIResponseParsingTest {

    private static final String OBSERVATIONS1 = "observations_single_place.xml";

    @Test
    public void testInvalidXml() {
        assertThrows(SAXParseException.class,
                () -> client.parseMultiPointCoverageXml(readTestResourceUtf8(OBSERVATIONS1).replace("276.0", "<<")));
    }

    @Test
    public void testUnexpectedXml() {
        assertThrows(FMIResponseException.class, () -> client
                .parseMultiPointCoverageXml(readTestResourceUtf8(OBSERVATIONS1).replace("276.0", "<foo>4</foo>")));
    }
}
