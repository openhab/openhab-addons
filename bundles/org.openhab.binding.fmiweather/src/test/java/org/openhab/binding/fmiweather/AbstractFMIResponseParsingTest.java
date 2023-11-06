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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.fmiweather.internal.client.Client;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Location;

/**
 * Base class for response parsing tests
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AbstractFMIResponseParsingTest {

    @NonNullByDefault({})
    protected Client client;

    @BeforeEach
    public void setUpClient() {
        client = new Client();
    }

    protected Path getTestResource(String filename) {
        try {
            return Paths.get(getClass().getResource(filename).toURI());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
            // Make the compiler happy by throwing here, fails already above
            throw new IllegalStateException();
        }
    }

    protected String readTestResourceUtf8(String filename) {
        return readTestResourceUtf8(getTestResource(filename));
    }

    protected String readTestResourceUtf8(Path path) {
        try {
            BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read = -1;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            return content.toString();
        } catch (IOException e) {
            fail(e.getMessage());
            // Make the compiler happy by throwing here, fails already above
            throw new IllegalStateException();
        }
    }

    protected static TypeSafeMatcher<Location> deeplyEqualTo(Location location) {
        return new ResponseLocationMatcher(location);
    }

    protected static Matcher<Data> deeplyEqualTo(long start, int intervalMinutes, String... values) {
        return new TypeSafeMatcher<Data>() {

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

    /**
     *
     * @param content
     * @return
     * @throws Throwable exception raised by parseMultiPointCoverageXml
     * @throws AssertionError exception raised when parseMultiPointCoverageXml method signature does not match excepted
     *             (test & implementation is out-of-sync)
     */
    protected FMIResponse parseMultiPointCoverageXml(String content) throws Throwable {
        try {
            Method parseMethod = Client.class.getDeclaredMethod("parseMultiPointCoverageXml", String.class);
            parseMethod.setAccessible(true);
            return Objects.requireNonNull((FMIResponse) parseMethod.invoke(client, content));
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            fail(String.format("Unexpected reflection error (code changed?) %s: %s", e.getClass().getName(),
                    e.getMessage()));
            // Make the compiler happy by throwing here, fails already above
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    protected Set<Location> parseStations(String content) {
        try {
            Method parseMethod = Objects.requireNonNull(Client.class.getDeclaredMethod("parseStations", String.class));
            parseMethod.setAccessible(true);
            return Objects.requireNonNull((Set<Location>) parseMethod.invoke(client, content));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (Exception e) {
            fail(String.format("Unexpected reflection error (code changed?) %s: %s", e.getClass().getName(),
                    e.getMessage()));
            // Make the compiler happy by throwing here, fails already above
            throw new IllegalStateException();
        }
    }
}
