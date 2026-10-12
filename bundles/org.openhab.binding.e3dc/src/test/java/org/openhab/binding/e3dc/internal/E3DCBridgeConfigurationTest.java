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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Guards the poll interval.
 *
 * <p>
 * The bounds in the thing metadata constrain the user interface only, so the value the scheduler uses has
 * to be limited by the binding itself. Both have to describe the same range.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCBridgeConfigurationTest {

    private static final Path BRIDGE_METADATA = Path.of("src", "main", "resources", "OH-INF", "thing",
            "thing-bridge.xml");

    @Test
    void anIntervalBelowTheMinimumIsRaisedToTheMinimum() {
        E3DCBridgeConfiguration config = new E3DCBridgeConfiguration();

        config.refreshInterval = 0;
        assertEquals(E3DCBridgeConfiguration.MIN_REFRESH_INTERVAL_SECONDS, config.refreshIntervalSeconds(),
                "an interval of zero would poll without pause");
        config.refreshInterval = -30;
        assertEquals(E3DCBridgeConfiguration.MIN_REFRESH_INTERVAL_SECONDS, config.refreshIntervalSeconds(),
                "a negative interval must not reach the scheduler");
    }

    @Test
    void anIntervalAboveTheMaximumIsLoweredToTheMaximum() {
        E3DCBridgeConfiguration config = new E3DCBridgeConfiguration();

        config.refreshInterval = 3600;
        assertEquals(E3DCBridgeConfiguration.MAX_REFRESH_INTERVAL_SECONDS, config.refreshIntervalSeconds());
    }

    @Test
    void anIntervalInsideTheRangeIsUsedUnchanged() {
        E3DCBridgeConfiguration config = new E3DCBridgeConfiguration();

        config.refreshInterval = 1;
        assertEquals(1, config.refreshIntervalSeconds());
        config.refreshInterval = 300;
        assertEquals(300, config.refreshIntervalSeconds());
    }

    @Test
    void theDefaultIntervalIsInsideTheRange() {
        E3DCBridgeConfiguration config = new E3DCBridgeConfiguration();

        assertEquals(60, config.refreshIntervalSeconds(), "the documented default is 60 seconds");
    }

    /**
     * The metadata is what the user interface offers and constrains, so its bounds have to be the bounds the
     * binding applies itself.
     */
    @Test
    void theMetadataDeclaresTheSameRange() throws IOException {
        assertEquals(E3DCBridgeConfiguration.MIN_REFRESH_INTERVAL_SECONDS, declaredBound("min"));
        assertEquals(E3DCBridgeConfiguration.MAX_REFRESH_INTERVAL_SECONDS, declaredBound("max"));
    }

    /** The value of one bound of the refresh interval parameter, as the thing metadata declares it. */
    private static int declaredBound(String attribute) throws IOException {
        Matcher parameter = Pattern.compile("<parameter name=\"refreshInterval\"([^>]*)>")
                .matcher(Files.readString(BRIDGE_METADATA));
        assertTrue(parameter.find(), "the bridge metadata declares no refreshInterval parameter");
        Matcher bound = Pattern.compile(attribute + "=\"(\\d+)\"").matcher(parameter.group(1));
        assertTrue(bound.find(), "the refreshInterval parameter declares no " + attribute);
        return Integer.parseInt(bound.group(1));
    }
}
