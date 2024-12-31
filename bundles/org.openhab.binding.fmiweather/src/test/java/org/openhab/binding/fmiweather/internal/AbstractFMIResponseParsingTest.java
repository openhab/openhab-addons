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

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.fmiweather.internal.client.Client;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.exception.FMIExceptionReportException;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.xml.sax.SAXException;

/**
 * Base class for response parsing tests
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AbstractFMIResponseParsingTest {

    @NonNullByDefault({})
    protected ClientExposed client;

    @BeforeEach
    public void setUpClient() {
        client = new ClientExposed();
    }

    protected String readTestResourceUtf8(String filename) throws IOException {
        try (InputStream inputStream = AbstractFMIResponseParsingTest.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("Input stream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    protected static TypeSafeMatcher<Location> deeplyEqualTo(Location location) {
        return new ResponseLocationMatcher(location);
    }

    protected static Matcher<Data> deeplyEqualTo(long start, int intervalMinutes, String... values) {
        return new TypeSafeMatcher<>() {

            private TimestampMatcher timestampMatcher = new TimestampMatcher(start, intervalMinutes, values.length);
            private ValuesMatcher valuesMatcher = new ValuesMatcher(values);

            @Override
            public void describeTo(@Nullable Description description) {
                if (description == null) {
                    return;
                }
                description.appendDescriptionOf(timestampMatcher);
                description.appendText(" and ");
                description.appendDescriptionOf(valuesMatcher);
            }

            @Override
            protected boolean matchesSafely(Data dataValues) {
                return timestampMatcher.matches(dataValues.timestampsEpochSecs)
                        && valuesMatcher.matches(dataValues.values);
            }

            @Override
            protected void describeMismatchSafely(Data dataValues, @Nullable Description mismatchDescription) {
                if (mismatchDescription == null) {
                    super.describeMismatchSafely(dataValues, mismatchDescription);
                    return;
                }
                if (!timestampMatcher.matches(dataValues.timestampsEpochSecs)) {
                    mismatchDescription.appendText("timestamps mismatch: ");
                    if (dataValues.timestampsEpochSecs[0] != start) {
                        mismatchDescription.appendText("start mismatch (was ");
                        mismatchDescription.appendValue(dataValues.timestampsEpochSecs[0]);
                        mismatchDescription.appendText(")");
                    } else if (dataValues.timestampsEpochSecs.length != values.length) {
                        mismatchDescription.appendText("length mismatch (was ");
                        mismatchDescription.appendValue(dataValues.timestampsEpochSecs.length);
                        mismatchDescription.appendText(")");
                    } else {
                        mismatchDescription.appendText("interval mismatch (was ");
                        Set<Long> intervals = new HashSet<>();
                        for (int i = 1; i < values.length; i++) {
                            long interval = dataValues.timestampsEpochSecs[i] - dataValues.timestampsEpochSecs[i - 1];
                            intervals.add(interval);
                        }
                        mismatchDescription.appendValue(intervals.toArray());
                        mismatchDescription.appendText(")");
                    }
                }
                mismatchDescription.appendText(", valuesMatch=").appendValue(valuesMatcher.matches(dataValues.values));
            }
        };
    }

    protected class ClientExposed extends Client {
        public ClientExposed() {
            super(mock(HttpClient.class));
        }

        public FMIResponse parseMultiPointCoverageXml(String response) throws FMIUnexpectedResponseException,
                FMIExceptionReportException, SAXException, IOException, XPathExpressionException {
            return super.parseMultiPointCoverageXml(response);
        }

        public Set<Location> parseStations(String response) throws FMIExceptionReportException,
                FMIUnexpectedResponseException, SAXException, IOException, XPathExpressionException {
            return super.parseStations(response);
        }
    }
}
