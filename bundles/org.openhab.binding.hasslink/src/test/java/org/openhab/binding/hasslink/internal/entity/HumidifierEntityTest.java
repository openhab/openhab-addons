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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.HumidifierEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests for {@link HumidifierEntity} to verify channel discovery, state parsing,
 * state descriptions, and service call mapping contracts across humidifier fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Humidifier entity fixture contracts")
class HumidifierEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("humidifier.dehumidifier"), //
                new Fixture("humidifier.humidifier"), //
                new Fixture("humidifier.hygrostat"));
    }

    private final HumidifierEntity entity = new HumidifierEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("discovers supported humidifier channels based on feature flags and attributes")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();
            List<String> expected = expectedChannelIds(state, primaryId);

            assertThat(channelIds, containsInAnyOrder(expected.toArray(new String[0])));
            assertThat(channels, hasSize(expected.size()));
        }

        private List<String> expectedChannelIds(EntityState state, String primaryId) {
            List<String> expected = new ArrayList<>(List.of(primaryId));
            if (state.hasAttribute("humidity")) {
                expected.add(primaryId + "#humidity");
            }
            if (state.hasAttribute("current_humidity")) {
                expected.add(primaryId + "#current_humidity");
            }
            if (state.isSupportedFeature(1L) && state.hasAttribute("mode")) {
                expected.add(primaryId + "#mode");
            }
            return expected;
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("exposes min/max range for target humidity and available modes as options")
        void exposesStateDescriptions(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);

            if (state.hasAttribute("humidity")) {
                StateDescriptionFragment range = entity.getStateDescriptionFragment(state, "humidity", context);
                assertThat(range, is(notNullValue()));
                assertThat(Objects.requireNonNull(range).getMinimum(), is(new java.math.BigDecimal("0")));
                assertThat(range.getMaximum(), is(new java.math.BigDecimal("100")));
            }

            List<String> availableModes = state.getAttributeAsStringList("available_modes");
            StateDescriptionFragment modeFragment = entity.getStateDescriptionFragment(state, "mode", context);
            if (!availableModes.isEmpty()) {
                assertThat(modeFragment, is(notNullValue()));
                List<String> options = Objects.requireNonNull(modeFragment).getOptions().stream()
                        .map(StateOption::getValue).toList();
                assertThat(options, containsInAnyOrder(availableModes.toArray(new String[0])));
            } else {
                assertThat(modeFragment, is(nullValue()));
            }
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("dispatches power state, target humidity, current humidity, and mode")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            boolean expectedPower = "on".equalsIgnoreCase(state.state());
            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(OnOffType.from(expectedPower))));

            Double humidity = state.getAttributeAsDouble("humidity");
            if (humidity != null) {
                assertThat(parsed.get("humidity"), is(new ParsedData.StateData(QuantityType.valueOf(humidity + " %"))));
            }

            Double currentHumidity = state.getAttributeAsDouble("current_humidity");
            if (currentHumidity != null) {
                assertThat(parsed.get("current_humidity"),
                        is(new ParsedData.StateData(QuantityType.valueOf(currentHumidity + " %"))));
            }

            String mode = state.getAttributeAsString("mode");
            if (mode != null) {
                assertThat(parsed.get("mode"), is(new ParsedData.StateData(new StringType(mode))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("maps power commands to humidifier turn_on and turn_off services")
        void mapsPowerCommands(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.ON, state, context)
                            .orElseThrow(),
                    "humidifier", "turn_on", fixture.entityId(), null);

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.OFF, state, context)
                            .orElseThrow(),
                    "humidifier", "turn_off", fixture.entityId(), null);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("maps target humidity commands to set_humidity service")
        void mapsHumidityCommand(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);
            if (state.hasAttribute("humidity")) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "humidity", QuantityType.valueOf("55 %"), state,
                                context).orElseThrow(),
                        "humidifier", "set_humidity", fixture.entityId(), Map.of("humidity", 55.0));

                assertService(
                        entity.toServiceCall(fixture.entityId(), "humidity", new DecimalType(55), state, context)
                                .orElseThrow(),
                        "humidifier", "set_humidity", fixture.entityId(), Map.of("humidity", 55.0));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.HumidifierEntityTest#fixtures")
        @DisplayName("maps mode commands to set_mode service")
        void mapsModeCommand(Fixture fixture) {
            EntityState state = fixture.load(HumidifierEntityTest.this);
            if (state.isSupportedFeature(1L) && state.hasAttribute("mode")) {
                String mode = Objects.requireNonNull(state.getAttributeAsString("mode"));
                assertService(entity.toServiceCall(fixture.entityId(), "mode", new StringType(mode), state, context)
                        .orElseThrow(), "humidifier", "set_mode", fixture.entityId(), Map.of("mode", mode));
            }
        }
    }
}
