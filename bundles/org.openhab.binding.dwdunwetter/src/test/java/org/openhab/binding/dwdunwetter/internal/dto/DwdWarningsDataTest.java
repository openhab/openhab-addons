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
package org.openhab.binding.dwdunwetter.internal.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link org.openhab.binding.dwdunwetter.internal.dto.DwdWarningsData}
 *
 * <p>
 * Uses the warnings.xml from the resources directory instead of accessing the api endpoint.
 *
 * <p>
 * The XML has 2 Entries:
 *
 * <ol>
 * <li>Amtliche WARNUNG vor WINDBÖEN, MINOR
 * <li>Amtliche WARNUNG vor STURMBÖEN, MODERATE
 * </ol>
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningsDataTest {

    private TestDataProvider testDataProvider;
    private DwdWarningsData warningsData;

    @BeforeEach
    public void setUp() throws IOException {
        this.testDataProvider = new TestDataProvider();
        loadXmlFromFile();
        warningsData = new DwdWarningsData("");
        warningsData.setDataAccess(testDataProvider);
        warningsData.refresh();
    }

    @Test
    public void testGetHeadline() {
        assertThat(warningsData.getHeadline(0), is("Amtliche WARNUNG vor STURMBÖEN"));
        assertThat(warningsData.getHeadline(1), is("Amtliche WARNUNG vor WINDBÖEN"));
        assertThat(warningsData.getHeadline(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetSeverity() {
        assertThat(warningsData.getSeverity(0), is("Moderate"));
        assertThat(warningsData.getSeverity(1), is("Minor"));
        assertThat(warningsData.getSeverity(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetEvent() {
        assertThat(warningsData.getEvent(0), is("STURMBÖEN"));
        assertThat(warningsData.getEvent(1), is("WINDBÖEN"));
        assertThat(warningsData.getEvent(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetDescription() {
        assertThat(warningsData.getDescription(0), is(
                "Es treten Sturmböen mit Geschwindigkeiten zwischen 60 km/h (17m/s, 33kn, Bft 7) und 80 km/h (22m/s, 44kn, Bft 9) anfangs aus südwestlicher, später aus westlicher Richtung auf. In Schauernähe sowie in exponierten Lagen muss mit schweren Sturmböen um 90 km/h (25m/s, 48kn, Bft 10) gerechnet werden."));
        assertThat(warningsData.getDescription(1), is(
                "Es treten Windböen mit Geschwindigkeiten bis 60 km/h (17m/s, 33kn, Bft 7) aus westlicher Richtung auf. In Schauernähe sowie in exponierten Lagen muss mit Sturmböen bis 80 km/h (22m/s, 44kn, Bft 9) gerechnet werden."));
        assertThat(warningsData.getDescription(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetAltitude() {
        assertThat(warningsData.getAltitude(0).format("%.0f ft"), is("0 ft"));
        assertThat(warningsData.getAltitude(1).format("%.0f ft"), is("0 ft"));
        assertThat(warningsData.getAltitude(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetCeiling() {
        assertThat(warningsData.getCeiling(0).format("%.0f ft"), is("9843 ft"));
        assertThat(warningsData.getCeiling(1).format("%.0f ft"), is("9843 ft"));
        assertThat(warningsData.getCeiling(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetExpires() {
        // Conversion is needed as getExpires returns the Date with the System Default Zone
        assertThat(((DateTimeType) warningsData.getExpires(0)).getZonedDateTime().withZoneSameInstant(ZoneId.of("UTC"))
                .toString(), is("2018-12-22T18:00Z[UTC]"));
        assertThat(((DateTimeType) warningsData.getExpires(1)).getZonedDateTime().withZoneSameInstant(ZoneId.of("UTC"))
                .toString(), is("2018-12-23T01:00Z[UTC]"));
        assertThat(warningsData.getExpires(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetOnset() {
        // Conversion is needed as getOnset returns the Date with the System Default Zone
        assertThat(((DateTimeType) warningsData.getOnset(0)).getZonedDateTime().withZoneSameInstant(ZoneId.of("UTC"))
                .toString(), is("2018-12-21T10:00Z[UTC]"));
        assertThat(((DateTimeType) warningsData.getOnset(1)).getZonedDateTime().withZoneSameInstant(ZoneId.of("UTC"))
                .toString(), is("2018-12-22T18:00Z[UTC]"));
        assertThat(warningsData.getOnset(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetEffective() {
        // Conversion is needed as getEffective returns the Date with the System Default Zone
        assertThat(((DateTimeType) warningsData.getEffective(0)).getZonedDateTime()
                .withZoneSameInstant(ZoneId.of("UTC")).toString(), is("2018-12-22T03:02Z[UTC]"));
        assertThat(((DateTimeType) warningsData.getEffective(1)).getZonedDateTime()
                .withZoneSameInstant(ZoneId.of("UTC")).toString(), is("2018-12-22T10:15Z[UTC]"));
        assertThat(warningsData.getEffective(2), is(UnDefType.UNDEF));
    }

    @Test
    public void testGetWarning() {
        assertThat(warningsData.getWarning(0), is(OnOffType.ON));
        assertThat(warningsData.getWarning(1), is(OnOffType.ON));
        assertThat(warningsData.getWarning(2), is(OnOffType.OFF));
    }

    @Test
    public void testisNew() {
        assertThat(warningsData.isNew(0), is(true));
        assertThat(warningsData.isNew(1), is(true));
        assertThat(warningsData.isNew(2), is(false));
        // No longer new
        assertThat(warningsData.isNew(0), is(false));
        assertThat(warningsData.isNew(1), is(false));
        assertThat(warningsData.isNew(2), is(false));
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
