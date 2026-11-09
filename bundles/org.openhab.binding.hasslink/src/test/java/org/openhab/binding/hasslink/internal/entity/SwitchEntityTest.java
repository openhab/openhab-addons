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
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.SwitchEntity;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant switch entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Switch entity fixture contracts")
class SwitchEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("switch.ac"), //
                new Fixture("switch.decorative_lights"));
    }

    private final SwitchEntity entity = new SwitchEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SwitchEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(SwitchEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SwitchEntityTest#fixtures")
        @DisplayName("dispatches switch state to OnOffType")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(SwitchEntityTest.this);
            OnOffType expected = "on".equalsIgnoreCase(state.state()) ? OnOffType.ON : OnOffType.OFF;

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(expected)));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SwitchEntityTest#fixtures")
        @DisplayName("maps ON/OFF commands to turn_on/turn_off service calls")
        void mapsCommands(Fixture fixture) {
            EntityState state = fixture.load(SwitchEntityTest.this);
            String id = fixture.entityId();

            assertService(entity.toServiceCall(id, EntityType.PRIMARY_ATTR, OnOffType.ON, state, context).orElseThrow(),
                    "switch", "turn_on", id, null);
            assertService(
                    entity.toServiceCall(id, EntityType.PRIMARY_ATTR, OnOffType.OFF, state, context).orElseThrow(),
                    "switch", "turn_off", id, null);
        }
    }
}
