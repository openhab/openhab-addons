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
package org.openhab.binding.hasslink.internal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.SensorEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests entity utility methods for common prefixes and channel naming.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Entity utility tests")
class EntityUtilsTest {

    private record PrefixCase(List<String> values, char delimiter, String expected) {
        @Override
        public String toString() {
            return values + " -> '" + expected + "'";
        }
    }

    static Stream<PrefixCase> commonPrefixCases() {
        return Stream.of(//
                new PrefixCase(List.of("flashforge_temperature", "flashforge_humidity"), '_', "flashforge"),
                new PrefixCase(List.of("sensor", "sensor_temperature"), '_', "sensor"),
                new PrefixCase(List.of("adventurer_5m_pro_status", "adventurer_5m_pro_temperature"), '_',
                        "adventurer_5m_pro"),
                new PrefixCase(List.of("alpha", "beta"), '_', ""),
                new PrefixCase(List.of("FlashForge_temp", "flashforge_humidity"), '_', ""),
                new PrefixCase(List.of("device.v1.state", "device.v1.status"), '.', "device.v1"),
                new PrefixCase(List.of("unit-2-ready", "unit-2-running"), '-', "unit-2"),
                new PrefixCase(List.of("one_value"), '_', "one_value"), new PrefixCase(List.of(), '_', ""),
                new PrefixCase(null, '_', ""));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commonPrefixCases")
    @DisplayName("calculates token-aligned common prefixes across edge cases")
    void calculatesCommonPrefix(PrefixCase testCase) {
        assertThat(EntityUtils.calculateCommonPrefix(testCase.values(), testCase.delimiter()),
                is(equalTo(testCase.expected())));
    }

    static Stream<Arguments> prefixStrippingCases() {
        return Stream.of(//
                Arguments.of(List.of("sensor.flashforge_temperature", "sensor.flashforge_humidity"), //
                        null, //
                        List.of("sensor-temperature", "sensor-humidity"), //
                        List.of("Temperature", "Humidity")),
                Arguments.of(
                        List.of("sensor.flashforge_temperature", "sensor.flashforge_adventurer_5m_pro_status",
                                "sensor.flashforge_build_volume"),
                        "Flashforge Adventurer 5M PRO",
                        List.of("sensor-temperature", "sensor-status", "sensor-build_volume"),
                        List.of("Temperature", "Status", "Build Volume")),
                Arguments.of(List.of("sensor.adventurer_5m_pro_temperature", "sensor.adventurer_5m_pro_status"), //
                        null, //
                        List.of("sensor-temperature", "sensor-status"), //
                        List.of("Temperature", "Status")));
    }

    @ParameterizedTest(name = "{0} with label {1} produces {2}")
    @MethodSource("prefixStrippingCases")
    @DisplayName("strips generic and model prefixes while building stable channel IDs")
    void stripsPrefixesFromChannelIds(List<String> entityIds, String thingLabel, List<String> expectedChannelIds,
            List<String> expectedLabels) {
        HassLinkBridgeHandler bridge = mock(HassLinkBridgeHandler.class);
        when(bridge.getEntityType(any(EntityState.class))).thenReturn(new SensorEntity());
        List<EntityState> states = entityIds.stream().map(id -> new EntityState(id, "", Map.of())).toList();

        List<Channel> channels = HassLinkChannelFactory.buildChannels(states, new ThingUID("hasslink", "fixture"),
                thingLabel, bridge);

        assertThat(channels, hasSize(expectedChannelIds.size()));
        assertThat(channels.stream().map(channel -> channel.getUID().getId()).toList(),
                containsInAnyOrder(expectedChannelIds.toArray(String[]::new)));
        assertThat(channels.stream().map(channel -> channel.getLabel()).toList(),
                containsInAnyOrder(expectedLabels.toArray(String[]::new)));
    }
}
