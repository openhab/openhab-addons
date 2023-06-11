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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.exception.FMIResponseException;
import org.xml.sax.SAXParseException;

/**
 * Test cases for AbstractWeatherHandler. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIResponseParsingInvalidOrUnexpectedXmlTest extends AbstractFMIResponseParsingTest {

    private Path observations1 = getTestResource("observations_single_place.xml");

    @Test
    public void testInvalidXml() {
        assertThrows(SAXParseException.class,
                () -> parseMultiPointCoverageXml(readTestResourceUtf8(observations1).replace("276.0", "<<")));
    }

    @Test
    public void testUnexpectedXml() {
        assertThrows(FMIResponseException.class,
                () -> parseMultiPointCoverageXml(readTestResourceUtf8(observations1).replace("276.0", "<foo>4</foo>")));
    }
}
