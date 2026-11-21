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
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.LockEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.CommandOption;

/**
 * Tests for {@link LockEntity} to verify channel discovery, state parsing,
 * command options, and service call mapping contracts across lock fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Lock entity fixture contracts")
class LockEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("lock.kitchen_door"), //
                new Fixture("lock.front_door"), //
                new Fixture("lock.openable_lock"), //
                new Fixture("lock.poorly_installed_door"));
    }

    private final LockEntity entity = new LockEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LockEntityTest#fixtures")
        @DisplayName("discovers primary string channel and locked switch channel")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(LockEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            assertThat(channelIds, containsInAnyOrder(primaryId, primaryId + "#locked"));
            assertThat(channels, hasSize(2));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LockEntityTest#fixtures")
        @DisplayName("dispatches primary string state and locked switch state")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(LockEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));

            boolean expectedLocked = "locked".equalsIgnoreCase(state.state());
            assertThat(parsed.get("locked"), is(new ParsedData.StateData(OnOffType.from(expectedLocked))));
        }
    }

    @Nested
    @DisplayName("Command options extraction")
    class CommandOptionsTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LockEntityTest#fixtures")
        @DisplayName("extracts command options including open feature when supported")
        void extractsCommandOptions(Fixture fixture) {
            EntityState state = fixture.load(LockEntityTest.this);
            List<CommandOption> options = entity.getCommandOptions(state, EntityType.PRIMARY_ATTR, context);

            assertThat(options, is(notNullValue()));
            List<String> optionValues = options.stream().map(CommandOption::getCommand).toList();

            List<String> expected = new ArrayList<>(List.of("lock", "unlock"));
            if (state.isSupportedFeature(LockEntity.OPEN)) {
                expected.add("open");
            }
            assertThat(optionValues, is(expected));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LockEntityTest#fixtures")
        @DisplayName("maps primary commands to lock, unlock, and open services")
        void mapsPrimaryCommands(Fixture fixture) {
            EntityState state = fixture.load(LockEntityTest.this);
            String entityId = fixture.entityId();

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("lock"), state, context)
                            .orElseThrow(),
                    "lock", "lock", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("unlock"), state, context)
                            .orElseThrow(),
                    "lock", "unlock", entityId, null);

            if (state.isSupportedFeature(LockEntity.OPEN)) {
                assertService(
                        entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, new StringType("open"), state, context)
                                .orElseThrow(),
                        "lock", "open", entityId, null);
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LockEntityTest#fixtures")
        @DisplayName("maps locked switch channel commands to lock and unlock services")
        void mapsLockedSwitchCommands(Fixture fixture) {
            EntityState state = fixture.load(LockEntityTest.this);
            String entityId = fixture.entityId();

            assertService(entity.toServiceCall(entityId, "locked", OnOffType.ON, state, context).orElseThrow(), "lock",
                    "lock", entityId, null);

            assertService(entity.toServiceCall(entityId, "locked", OnOffType.OFF, state, context).orElseThrow(), "lock",
                    "unlock", entityId, null);
        }
    }
}
