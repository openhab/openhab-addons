/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Checks that the texts of the {@code monitorBufferSize} option say what the option does, in the thing definitions
 * and in the English properties, in the same words.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorBufferSizeTextTest {
    private static final String CLAMPS = "Values above 100000 are reduced to 100000. A negative value counts as 0.";

    private static String resource(String name) throws IOException {
        try (InputStream in = Objects.requireNonNull(MonitorBufferSizeTextTest.class.getResourceAsStream("/" + name))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @ParameterizedTest
    @CsvSource({ "ip", "serial" })
    void bothTextsNameTheLowerAndTheUpperBound(String type) throws IOException {
        Matcher xml = Pattern
                .compile("<parameter name=\"monitorBufferSize\".*?<description>(.*?)</description>", Pattern.DOTALL)
                .matcher(resource("OH-INF/thing/" + type + ".xml"));
        assertTrue(xml.find(), type);
        Matcher properties = Pattern
                .compile("^thing-type\\.config\\.knx\\." + type + "\\.monitorBufferSize\\.description = (.*)$",
                        Pattern.MULTILINE)
                .matcher(resource("OH-INF/i18n/knx.properties"));
        assertTrue(properties.find(), type);

        String fromXml = xml.group(1).replaceAll("\\s+", " ").trim();
        String fromProperties = properties.group(1).trim();

        assertTrue(fromXml.endsWith(CLAMPS), fromXml);
        assertEquals(fromXml, fromProperties);
    }
}
