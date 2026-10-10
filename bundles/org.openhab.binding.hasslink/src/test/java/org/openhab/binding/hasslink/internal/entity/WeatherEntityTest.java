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
package org.openhab.binding.hasslink.internal.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.WeatherEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery and state mapping for Home Assistant weather entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Weather entity fixture contracts")
class WeatherEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("weather.demo_weather_north"), //
                new Fixture("weather.demo_weather_south"));
    }

    private final WeatherEntity entity = new WeatherEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WeatherEntityTest#fixtures")
        @DisplayName("creates standard weather channels and only supported forecast channels")
        void createsSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(WeatherEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            List<String> expected = new ArrayList<>(List.of(primaryId));
            for (String attribute : List.of("temperature", "humidity", "pressure", "wind_speed")) {
                if (state.hasAttribute(attribute)) {
                    expected.add(primaryId + "#" + attribute);
                }
            }
            for (long feature : List.of(WeatherEntity.FORECAST_DAILY, WeatherEntity.FORECAST_HOURLY,
                    WeatherEntity.FORECAST_TWICE_DAILY)) {
                if (state.isSupportedFeature(feature)) {
                    String forecastType = switch ((int) feature) {
                        case 1 -> "forecast";
                        case 2 -> "forecast_hourly";
                        default -> "forecast_twice_daily";
                    };
                    expected.addAll(List
                            .of("cloud_coverage", "condition", "humidity", "apparent_temperature", "dew_point",
                                    "precipitation", "pressure", "temperature", "templow", "wind_gust_speed",
                                    "wind_speed", "precipitation_probability", "uv_index", "wind_bearing")
                            .stream().map(attribute -> primaryId + "#" + forecastType + "_" + attribute).toList());
                }
            }

            assertThat(channelIds, containsInAnyOrder(expected.toArray(new String[0])));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WeatherEntityTest#fixtures")
        @DisplayName("parses raw weather states and converts measured attributes to units")
        void parsesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(WeatherEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed, hasEntry(equalTo(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state())))));

            assertQuantity(parsed, state, "temperature", "temperature_unit");
            assertQuantity(parsed, state, "humidity", "%");
            assertQuantity(parsed, state, "pressure", "pressure_unit");
            assertQuantity(parsed, state, "wind_speed", "wind_speed_unit");
        }

        private void assertQuantity(Map<String, ParsedData> parsed, EntityState state, String attribute,
                String unitAttribute) {
            if (state.hasAttribute(attribute)) {
                String unit = "%".equals(unitAttribute) ? unitAttribute : state.getAttributeAsString(unitAttribute);
                assertThat(parsed, hasEntry(equalTo(attribute), is(new ParsedData.StateData(
                        QuantityType.valueOf(state.getAttributeAsString(attribute) + " " + unit)))));
            }
        }
    }
}
