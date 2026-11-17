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
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.knx.internal.config.BridgeConfiguration;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;

/**
 * Checks the bridge option that sets how many telegrams the group monitor keeps.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorBufferSizeTest {

    private static int applied(int configured) {
        IPBridgeThingHandler handler = new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class));
        handler.applyMonitorBufferSize(configured);
        return handler.getTelegramMonitor().status().bufferSize();
    }

    @Test
    void theDefaultIs2000() {
        assertEquals(2000, new BridgeConfiguration().getMonitorBufferSize());
        assertEquals(2000, TelegramMonitor.DEFAULT_BUFFER_SIZE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "OH-INF/thing/ip.xml", "OH-INF/thing/serial.xml" })
    void bothBridgeTypesOfferTheOptionWithThatDefault(String file) throws IOException {
        String xml;
        try (InputStream in = Objects.requireNonNull(MonitorBufferSizeTest.class.getResourceAsStream("/" + file))) {
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(Pattern.compile("<parameter name=\"monitorBufferSize\" type=\"integer\">.*?<default>2000</default>",
                Pattern.DOTALL).matcher(xml).find(), file);
    }

    @ParameterizedTest
    @ValueSource(strings = { "OH-INF/thing/ip.xml", "OH-INF/thing/serial.xml" })
    void sendingFromThePageIsOffByDefaultInBothBridgeTypes(String file) throws IOException {
        String xml;
        try (InputStream in = Objects.requireNonNull(MonitorBufferSizeTest.class.getResourceAsStream("/" + file))) {
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        Matcher parameter = Pattern
                .compile("<parameter name=\"groupMonitorSend\"([^>]*)>(.*?)</parameter>", Pattern.DOTALL).matcher(xml);
        assertTrue(parameter.find(), file);
        assertEquals(" type=\"boolean\"", parameter.group(1), file);
        assertEquals(List.of("false"), tagValues(parameter.group(2), "default"), file);
        assertEquals(List.of("true"), tagValues(parameter.group(2), "advanced"), file);
        assertFalse(parameter.find(), file);
        assertFalse(new BridgeConfiguration().isGroupMonitorSend());
    }

    @Test
    void noOtherThingTypeHasTheSendSetting() throws IOException {
        for (String file : List.of("OH-INF/thing/device.xml", "OH-INF/config/channelConfig.xml",
                "OH-INF/addon/addon.xml")) {
            try (InputStream in = Objects.requireNonNull(MonitorBufferSizeTest.class.getResourceAsStream("/" + file))) {
                assertFalse(new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("groupMonitorSend"), file);
            }
        }
    }

    private static List<String> tagValues(String xml, String tag) {
        return Pattern.compile("<" + tag + ">\\s*(.*?)\\s*</" + tag + ">", Pattern.DOTALL).matcher(xml).results()
                .map(found -> found.group(1)).toList();
    }

    @Test
    void zeroKeepsNoRows() {
        assertEquals(0, applied(0));
    }

    @Test
    void aSizeInRangeIsTaken() {
        assertEquals(500, applied(500));
        assertEquals(100_000, applied(100_000));
    }

    @Test
    void aSizeAboveTheBoundIsClamped() {
        assertEquals(100_000, applied(100_001));
        assertEquals(100_000, applied(Integer.MAX_VALUE));
    }

    @Test
    void aNegativeSizeIsClampedToZero() {
        assertEquals(0, applied(-1));
    }
}
