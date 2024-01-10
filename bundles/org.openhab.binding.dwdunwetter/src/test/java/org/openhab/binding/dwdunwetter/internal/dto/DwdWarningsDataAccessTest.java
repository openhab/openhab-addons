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
package org.openhab.binding.dwdunwetter.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.openhab.binding.dwdunwetter.internal.dto.DwdWarningsDataAccess}
 *
 * @author Leo Siepel - Initial contribution
 */
public class DwdWarningsDataAccessTest {
    private TestDataProvider testDataProvider = new TestDataProvider();

    @BeforeEach
    public void setUp() throws IOException {
        this.testDataProvider = new TestDataProvider();
        loadXmlFromFile();
    }

    @Test
    public void testNullOrBlank() {
        assertEquals(testDataProvider.getDataFromEndpoint(null), "");
        assertEquals(testDataProvider.getDataFromEndpoint(""), "");
    }

    @Test
    public void testInvalidResponse() {
        TestDataProvider testDataProvider = new TestDataProvider();
        testDataProvider.rawData = "Server is not returning xml";
        assertEquals(testDataProvider.getDataFromEndpoint("TestCity"), "");
    }

    private void loadXmlFromFile() throws IOException {
        InputStream stream = getClass().getResourceAsStream("warnings.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String line = null;

        StringWriter stringWriter = new StringWriter();
        while ((line = reader.readLine()) != null) {
            stringWriter.write(line);
        }
        reader.close();
        testDataProvider.rawData = stringWriter.toString();
    }

    private class TestDataProvider extends DwdWarningDataAccess {

        private String rawData = "";

        @Override
        public String getByURL(String url) {
            return rawData;
        }
    }
}
