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
import org.openhab.binding.hasslink.internal.entity.impl.VacuumEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant vacuum entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Vacuum entity fixture contracts")
class VacuumEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("vacuum.demo_vacuum_0_ground_floor"), //
                new Fixture("vacuum.demo_vacuum_1_first_floor"), //
                new Fixture("vacuum.demo_vacuum_2_second_floor"), //
                new Fixture("vacuum.demo_vacuum_3_third_floor"), //
                new Fixture("vacuum.demo_vacuum_4_fourth_floor"));
    }

    private final VacuumEntity entity = new VacuumEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("discovers only channels supported by each vacuum fixture")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
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
            if (state.isSupportedFeature(32L) && state.hasAttribute("fan_speed")) {
                expected.add(primaryId + "#fan_speed");
            }
            if (state.hasAttribute("cleaned_area")) {
                expected.add(primaryId + "#cleaned_area");
            }
            return expected;
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("exposes each fixture fan speed list as state options")
        void exposesFanSpeedOptions(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, "fan_speed", context);

            if (state.hasAttribute("fan_speed_list")) {
                assertThat(fragment, is(notNullValue()));
                List<StateOption> options = Objects.requireNonNull(fragment).getOptions();
                assertThat(options.stream().map(StateOption::getValue).toList(),
                        containsInAnyOrder(state.getAttributeAsStringList("fan_speed_list").toArray(new String[0])));
            } else {
                assertThat(fragment, is(nullValue()));
            }
        }
    }

    @Nested
    @DisplayName("Channel command options")
    class ChannelCommandOptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("extracts primary command options in expected bitmask order")
        void extractsPrimaryCommandOptions(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
            List<CommandOption> options = entity.getCommandOptions(state, EntityType.PRIMARY_ATTR, context);

            assertThat(options, is(notNullValue()));
            List<String> actualCommands = Objects.requireNonNull(options).stream().map(CommandOption::getCommand)
                    .toList();

            List<String> expectedCommands = new ArrayList<>();
            if (state.isSupportedFeature(8192L))
                expectedCommands.add("start");
            if (state.isSupportedFeature(8L))
                expectedCommands.add("stop");
            if (state.isSupportedFeature(4L))
                expectedCommands.add("pause");
            if (state.isSupportedFeature(16L))
                expectedCommands.add("dock");
            if (state.isSupportedFeature(1024L))
                expectedCommands.add("spot");
            if (state.isSupportedFeature(512L))
                expectedCommands.add("locate");

            assertThat(actualCommands, is(expectedCommands));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("dispatches every raw fixture state and numeric cleaned area")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));
            assertThat(parsed.get("cleaned_area"), is(new ParsedData.StateData(new DecimalType("0"))));
            if (state.hasAttribute("fan_speed")) {
                String fanSpeed = state.getAttributeAsString("fan_speed");
                assertThat(parsed.get("fan_speed"), is(new ParsedData.StateData(new StringType(fanSpeed))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("maps vacuum lifecycle commands to Home Assistant services")
        void mapsLifecycleCommands(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
            Map<String, String> services = Map.of( //
                    "start", "start", //
                    "stop", "stop", //
                    "pause", "pause", //
                    "return_to_base", "return_to_base", //
                    "locate", "locate");

            for (Map.Entry<String, String> service : services.entrySet()) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR,
                                new StringType(service.getKey()), state, context).orElseThrow(),
                        "vacuum", service.getValue(), fixture.entityId(), null);
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.VacuumEntityTest#fixtures")
        @DisplayName("maps fan speed commands to set fan speed")
        void mapsFanSpeedCommand(Fixture fixture) {
            EntityState state = fixture.load(VacuumEntityTest.this);
            if (state.hasAttribute("fan_speed")) {
                String speed = Objects.requireNonNull(state.getAttributeAsString("fan_speed"));
                assertService(
                        entity.toServiceCall(fixture.entityId(), "fan_speed", new StringType(speed), state, context)
                                .orElseThrow(),
                        "vacuum", "set_fan_speed", fixture.entityId(), Map.of("fan_speed", speed));
            }
        }
    }
}
