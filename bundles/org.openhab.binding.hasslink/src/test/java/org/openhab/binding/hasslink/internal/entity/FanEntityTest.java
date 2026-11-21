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
import org.openhab.binding.hasslink.internal.entity.impl.FanEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant fan entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Fan entity fixture contracts")
class FanEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("fan.ceiling_fan"), //
                new Fixture("fan.living_room_fan"), //
                new Fixture("fan.percentage_full_fan"), //
                new Fixture("fan.percentage_limited_fan"), //
                new Fixture("fan.preset_only_limited_fan"));
    }

    private final FanEntity entity = new FanEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("discovers supported fan channels based on feature flags")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
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
            if (state.isSupportedFeature(1L)) {
                expected.add(primaryId + "#percentage");
            }
            if (state.isSupportedFeature(2L)) {
                expected.add(primaryId + "#oscillating");
            }
            if (state.isSupportedFeature(4L)) {
                expected.add(primaryId + "#direction");
            }
            if (state.isSupportedFeature(8L)) {
                expected.add(primaryId + "#preset_mode");
            }
            return expected;
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("exposes preset modes as state options when supported")
        void exposesPresetModeOptions(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            List<String> presetModes = state.getAttributeAsStringList("preset_modes");
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, "preset_mode", context);

            if (!presetModes.isEmpty()) {
                assertThat(fragment, is(notNullValue()));
                List<StateOption> options = Objects.requireNonNull(fragment).getOptions();
                assertThat(options.stream().map(StateOption::getValue).toList(),
                        containsInAnyOrder(presetModes.toArray(new String[0])));
            } else {
                assertThat(fragment, is(nullValue()));
            }
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("dispatches primary power and active attribute states")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            boolean expectedPower = "on".equalsIgnoreCase(state.state());
            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(OnOffType.from(expectedPower))));

            Double percentage = state.getAttributeAsDouble("percentage");
            if (percentage != null) {
                assertThat(parsed.get("percentage"),
                        is(new ParsedData.StateData(new PercentType(percentage.intValue()))));
            }

            Boolean oscillating = state.getAttributeAsBoolean("oscillating");
            if (oscillating != null) {
                assertThat(parsed.get("oscillating"), is(new ParsedData.StateData(OnOffType.from(oscillating))));
            }

            String direction = state.getAttributeAsString("direction");
            if (direction != null) {
                assertThat(parsed.get("direction"), is(new ParsedData.StateData(new StringType(direction))));
            }

            String presetMode = state.getAttributeAsString("preset_mode");
            if (presetMode != null) {
                assertThat(parsed.get("preset_mode"), is(new ParsedData.StateData(new StringType(presetMode))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("maps power commands to fan turn_on and turn_off services")
        void mapsPowerCommands(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.ON, state, context)
                            .orElseThrow(),
                    "fan", "turn_on", fixture.entityId(), null);

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.OFF, state, context)
                            .orElseThrow(),
                    "fan", "turn_off", fixture.entityId(), null);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("maps speed percentage command to set_percentage service")
        void mapsPercentageCommand(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            if (state.isSupportedFeature(1L)) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "percentage", new PercentType(50), state, context)
                                .orElseThrow(),
                        "fan", "set_percentage", fixture.entityId(), Map.of("percentage", 50));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("maps oscillation commands to oscillate service")
        void mapsOscillationCommand(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            if (state.isSupportedFeature(2L)) {
                assertService(entity.toServiceCall(fixture.entityId(), "oscillating", OnOffType.ON, state, context)
                        .orElseThrow(), "fan", "oscillate", fixture.entityId(), Map.of("oscillating", true));

                assertService(entity.toServiceCall(fixture.entityId(), "oscillating", OnOffType.OFF, state, context)
                        .orElseThrow(), "fan", "oscillate", fixture.entityId(), Map.of("oscillating", false));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("maps direction command to set_direction service")
        void mapsDirectionCommand(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            if (state.isSupportedFeature(4L)) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "direction", new StringType("forward"), state, context)
                                .orElseThrow(),
                        "fan", "set_direction", fixture.entityId(), Map.of("direction", "forward"));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.FanEntityTest#fixtures")
        @DisplayName("maps preset mode command to set_preset_mode service")
        void mapsPresetModeCommand(Fixture fixture) {
            EntityState state = fixture.load(FanEntityTest.this);
            if (state.isSupportedFeature(8L)) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "preset_mode", new StringType("auto"), state, context)
                                .orElseThrow(),
                        "fan", "set_preset_mode", fixture.entityId(), Map.of("preset_mode", "auto"));
            }
        }
    }
}
