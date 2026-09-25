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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.impl.CoverEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant cover entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Cover entity fixture contracts")
class CoverEntityTest extends AbstractEntityTest {

    static Stream<AbstractEntityTest.Fixture> fixtures() {
        return Stream.of( //
                new Fixture("cover.garage_door"), //
                new Fixture("cover.hall_window"), //
                new Fixture("cover.kitchen_window"), //
                new Fixture("cover.living_room_window"), //
                new Fixture("cover.pergola_roof"));
    }

    private final CoverEntity entity = new CoverEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CoverEntityTest#fixtures")
        @DisplayName("creates channels supported by each cover fixture")
        void createsExpectedChannels(AbstractEntityTest.Fixture fixture) {
            EntityState state = fixture.load(CoverEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);

            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            String primaryId = fixture.entityId().replace('.', '-');
            List<String> expectedChannels = new java.util.ArrayList<>(List.of(primaryId));
            if (state.getAttributeAsInt("current_position") != null) {
                expectedChannels.add(primaryId + "#current_position");
            }
            if ((state.getAttributeAsLong("supported_features") & CoverEntity.TILT) != 0) {
                expectedChannels.add(primaryId + "#current_tilt_position");
            }
            assertThat(channels.stream().map(channel -> channel.getUID().getId()).toList(),
                    containsInAnyOrder(expectedChannels.toArray(new String[0])));
            assertThat(channels, hasSize(expectedChannels.size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CoverEntityTest#fixtures")
        @DisplayName("inverts Home Assistant positions into openHAB closed percentages")
        void invertsPositionState(AbstractEntityTest.Fixture fixture) {
            EntityState state = fixture.load(CoverEntityTest.this);
            Map<String, ParsedData> states = entity.parseState(state, context);

            assertThat(((ParsedData.StateData) states.get(EntityType.PRIMARY_ATTR)).state(), is(state.state()));
            assertInvertedPosition(states, state, "current_position");
            assertInvertedPosition(states, state, "current_tilt_position");
        }

        private void assertInvertedPosition(Map<String, ParsedData> states, EntityState state, String attribute) {
            Integer position = state.getAttributeAsInt(attribute);
            if (position != null) {
                assertThat(states,
                        hasEntry(equalTo(attribute), is(new ParsedData.StateData(new PercentType(100 - position)))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CoverEntityTest#fixtures")
        @DisplayName("maps directional and stop commands to cover services")
        void mapsMovementCommands(AbstractEntityTest.Fixture fixture) {
            EntityState state = fixture.load(CoverEntityTest.this);
            assertService(entity.toServiceCall(fixture.entityId(), "current_position", UpDownType.UP, state, context)
                    .orElseThrow(), "cover", "open_cover", fixture.entityId(), null);
            assertService(entity.toServiceCall(fixture.entityId(), "current_position", UpDownType.DOWN, state, context)
                    .orElseThrow(), "cover", "close_cover", fixture.entityId(), null);
            assertService(
                    entity.toServiceCall(fixture.entityId(), "current_position", StopMoveType.STOP, state, context)
                            .orElseThrow(),
                    "cover", "stop_cover", fixture.entityId(), null);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CoverEntityTest#fixtures")
        @DisplayName("inverts openHAB position percentages for Home Assistant")
        void mapsPositionCommand(AbstractEntityTest.Fixture fixture) {
            EntityState state = fixture.load(CoverEntityTest.this);
            Integer position = state.getAttributeAsInt("current_position");
            if (position == null) {
                return;
            }
            ServiceCall call = entity
                    .toServiceCall(fixture.entityId(), "current_position", new PercentType(position), state, context)
                    .orElseThrow();

            assertService(call, "cover", "set_cover_position", fixture.entityId(), Map.of("position", 100 - position));
        }
    }
}
