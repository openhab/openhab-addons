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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.AbstractEntityTest.Fixture;
import org.openhab.binding.hasslink.internal.entity.impl.MediaPlayerEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * Tests for {@link MediaPlayerEntity} to verify channel discovery, state parsing,
 * state description fragments, and service call mapping contracts across media player fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Media player entity fixture contracts")
class MediaPlayerEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("media_player.living_room"), //
                new Fixture("media_player.bedroom"), //
                new Fixture("media_player.browse"), //
                new Fixture("media_player.group"), //
                new Fixture("media_player.kitchen"), //
                new Fixture("media_player.lounge_room"), //
                new Fixture("media_player.search"), //
                new Fixture("media_player.walkman"));
    }

    private final MediaPlayerEntity entity = new MediaPlayerEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.MediaPlayerEntityTest#fixtures")
        @DisplayName("discovers channels based on supported features and attributes")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(MediaPlayerEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);
            assertThat(specs, is(notNullValue()));
            assertThat(channels, hasSize(specs.size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.MediaPlayerEntityTest#fixtures")
        @DisplayName("dispatches primary and attribute states correctly")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(MediaPlayerEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));

            boolean expectedPower = !"off".equalsIgnoreCase(state.state());
            assertThat(parsed.get("power"), is(new ParsedData.StateData(OnOffType.from(expectedPower))));
            assertThat(parsed.get("stop"), is(new ParsedData.StateData(OnOffType.OFF)));

            if (state.attributes().containsKey("sound_mode")) {
                assertThat(parsed.get("sound_mode"),
                        is(new ParsedData.StateData(new StringType(getAttributeString(state, "sound_mode")))));
            }
            if (state.attributes().containsKey("shuffle")) {
                assertThat(parsed.get("shuffle"),
                        is(new ParsedData.StateData(OnOffType.from(getAttributeBoolean(state, "shuffle")))));
            }
            if (state.attributes().containsKey("repeat")) {
                assertThat(parsed.get("repeat"),
                        is(new ParsedData.StateData(new StringType(getAttributeString(state, "repeat")))));
            }
        }
    }

    @Nested
    @DisplayName("State description fragments")
    class StateDescriptionFragmentTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.MediaPlayerEntityTest#fixtures")
        @DisplayName("provides source and sound mode state description fragments when list attributes are present")
        void providesOptionsFragments(Fixture fixture) {
            EntityState state = fixture.load(MediaPlayerEntityTest.this);

            StateDescriptionFragment sourceFragment = entity.getStateDescriptionFragment(state, "source", context);
            if (state.attributes().containsKey("source_list")) {
                assertThat(sourceFragment, is(notNullValue()));
            } else {
                assertThat(sourceFragment, is(nullValue()));
            }

            StateDescriptionFragment soundModeFragment = entity.getStateDescriptionFragment(state, "sound_mode",
                    context);
            if (state.attributes().containsKey("sound_mode_list")) {
                assertThat(soundModeFragment, is(notNullValue()));
            } else {
                assertThat(soundModeFragment, is(nullValue()));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.MediaPlayerEntityTest#fixtures")
        @DisplayName("maps player and control commands to corresponding media player services")
        void mapsCommands(Fixture fixture) {
            EntityState state = fixture.load(MediaPlayerEntityTest.this);
            String entityId = fixture.entityId();

            assertService(entity.toServiceCall(entityId, "player", PlayPauseType.PLAY, state, context).orElseThrow(),
                    "media_player", "media_play", entityId, null);

            assertService(entity.toServiceCall(entityId, "player", PlayPauseType.PAUSE, state, context).orElseThrow(),
                    "media_player", "media_pause", entityId, null);

            assertService(entity.toServiceCall(entityId, "player", NextPreviousType.NEXT, state, context).orElseThrow(),
                    "media_player", "media_next_track", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, "player", NextPreviousType.PREVIOUS, state, context).orElseThrow(),
                    "media_player", "media_previous_track", entityId, null);

            assertService(entity.toServiceCall(entityId, "stop", OnOffType.ON, state, context).orElseThrow(),
                    "media_player", "media_stop", entityId, null);

            assertService(entity.toServiceCall(entityId, "power", OnOffType.ON, state, context).orElseThrow(),
                    "media_player", "turn_on", entityId, null);

            assertService(
                    entity.toServiceCall(entityId, "volume_level", new PercentType(50), state, context).orElseThrow(),
                    "media_player", "volume_set", entityId, Map.of("volume_level", 0.5));

            assertService(
                    entity.toServiceCall(entityId, "sound_mode", new StringType("Movie"), state, context).orElseThrow(),
                    "media_player", "select_sound_mode", entityId, Map.of("sound_mode", "Movie"));

            assertService(entity.toServiceCall(entityId, "shuffle", OnOffType.ON, state, context).orElseThrow(),
                    "media_player", "shuffle_set", entityId, Map.of("shuffle", true));

            assertService(entity.toServiceCall(entityId, "repeat", new StringType("all"), state, context).orElseThrow(),
                    "media_player", "repeat_set", entityId, Map.of("repeat", "all"));
        }
    }

    private String getAttributeString(EntityState state, String key) {
        Object val = state.attributes().get(key);
        if (val instanceof com.google.gson.JsonPrimitive jp && jp.isString()) {
            return jp.getAsString();
        }
        return val != null ? val.toString() : "";
    }

    private boolean getAttributeBoolean(EntityState state, String key) {
        Object val = state.attributes().get(key);
        if (val instanceof com.google.gson.JsonPrimitive jp && jp.isBoolean()) {
            return jp.getAsBoolean();
        }
        if (val instanceof Boolean b) {
            return b;
        }
        return val != null && Boolean.parseBoolean(val.toString());
    }
}
