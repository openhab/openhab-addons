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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.AbstractEntityTest.Fixture;
import org.openhab.binding.hasslink.internal.entity.impl.LawnMowerEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.CommandOption;

/**
 * Tests for {@link LawnMowerEntity} to verify channel discovery, state parsing,
 * command options, and service call mapping contracts across lawn mower fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Lawn mower entity fixture contracts")
class LawnMowerEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(
                new Fixture("lawn_mower.navimow_i210awd", "../custom_fixtures/lawn_mower.navimow_i210awd.json"),
                new Fixture("lawn_mower.yuka_back_yard", "../custom_fixtures/lawn_mower.yuka_back_yard.json"));
    }

    private final LawnMowerEntity entity = new LawnMowerEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LawnMowerEntityTest#fixtures")
        @DisplayName("discovers single primary string channel for lawn mowers")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(LawnMowerEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            assertThat(channelIds, containsInAnyOrder(primaryId));
            assertThat(channels, hasSize(1));

            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);
            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.STRING));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LawnMowerEntityTest#fixtures")
        @DisplayName("dispatches primary string state")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(LawnMowerEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));
        }
    }

    @Nested
    @DisplayName("Command options extraction")
    class CommandOptionsTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LawnMowerEntityTest#fixtures")
        @DisplayName("extracts command options in correct order based on supported features bitmask")
        void extractsCommandOptions(Fixture fixture) {
            EntityState state = fixture.load(LawnMowerEntityTest.this);
            List<CommandOption> options = entity.getCommandOptions(state, EntityType.PRIMARY_ATTR, context);

            assertThat(options, is(notNullValue()));
            List<String> optionValues = options.stream().map(CommandOption::getCommand).toList();

            List<String> expected = new ArrayList<>();
            if (state.isSupportedFeature(1L)) {
                expected.add("start");
            }
            if (state.isSupportedFeature(2L)) {
                expected.add("pause");
            }
            if (state.isSupportedFeature(4L)) {
                expected.add("dock");
            }
            if (state.isSupportedFeature(8L)) {
                expected.add("stop");
            }

            assertThat(optionValues, is(expected));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LawnMowerEntityTest#fixtures")
        @DisplayName("maps command actions to respective lawn mower service calls")
        void mapsCommandActions(Fixture fixture) {
            EntityState state = fixture.load(LawnMowerEntityTest.this);
            String entityId = fixture.entityId();

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("start"), state, context)
                            .orElseThrow(),
                    "lawn_mower", "start_mowing", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("pause"), state, context)
                            .orElseThrow(),
                    "lawn_mower", "pause", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("stop"), state, context)
                            .orElseThrow(),
                    "lawn_mower", "stop", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("dock"), state, context)
                            .orElseThrow(),
                    "lawn_mower", "dock", entityId, null);
        }
    }
}
