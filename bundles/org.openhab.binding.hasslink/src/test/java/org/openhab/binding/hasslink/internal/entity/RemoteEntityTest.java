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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.RemoteEntity;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonPrimitive;

/**
 * Tests channel discovery, state handling, and commands for Home Assistant remote entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Remote entity fixture contracts")
class RemoteEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("remote.remote_one"), //
                new Fixture("remote.remote_two"));
    }

    private final RemoteEntity entity = new RemoteEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.RemoteEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState fixtureState = fixture.load(RemoteEntityTest.this);
            // The fixture's supported_features is 0, but we want to test the discovery of the delete_command channel,
            // which is only available when supported_features=2
            EntityState state = new EntityState(fixtureState.entityId(), fixtureState.state(),
                    Map.of("supported_features", new JsonPrimitive(2)));
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
            Channel deleteCommandChannel = channels.stream()
                    .filter(channel -> "delete_command"
                            .equals(channel.getConfiguration().get(HassLinkChannelFactory.CONFIG_ATTRIBUTE)))
                    .findFirst().orElseThrow();
            assertThat(deleteCommandChannel.getAcceptedItemType(), is(ItemType.STRING.name()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.RemoteEntityTest#fixtures")
        @DisplayName("dispatches remote power state")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(RemoteEntityTest.this);
            OnOffType expected = "on".equalsIgnoreCase(state.state()) ? OnOffType.ON : OnOffType.OFF;

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(expected)));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.RemoteEntityTest#fixtures")
        @DisplayName("maps power and command invocation service calls")
        void mapsCommands(Fixture fixture) {
            EntityState state = fixture.load(RemoteEntityTest.this);
            String id = fixture.entityId();

            assertService(entity.toServiceCall(id, EntityType.PRIMARY_ATTR, OnOffType.ON, state, context).orElseThrow(),
                    "remote", "turn_on", id, null);
            assertService(
                    entity.toServiceCall(id, "send_command", new StringType("power"), state, context).orElseThrow(),
                    "remote", "send_command", id, Map.of("command", "power"));
            assertService(entity.toServiceCall(id, "learn_command", OnOffType.ON, state, context).orElseThrow(),
                    "remote", "learn_command", id, null);
            assertService(
                    entity.toServiceCall(id, "delete_command", new StringType("power"), state, context).orElseThrow(),
                    "remote", "delete_command", id, Map.of("command", "power"));
            assertService(
                    entity.toServiceCall(id, "delete_command",
                            new StringType("{\"command\":[\"power\"],\"device\":\"TV\"}"), state, context)
                            .orElseThrow(),
                    "remote", "delete_command", id, Map.of("command", List.of("power"), "device", "TV"));
            assertThat(entity.toServiceCall(id, "delete_command", OnOffType.ON, state, context).isEmpty(), is(true));
        }
    }
}
