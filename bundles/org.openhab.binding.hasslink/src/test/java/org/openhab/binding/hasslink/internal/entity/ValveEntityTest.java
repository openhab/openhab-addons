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
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.ValveEntity;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant valve entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Valve entity fixture contracts")
class ValveEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("valve.back_garden"), //
                new Fixture("valve.front_garden"), //
                new Fixture("valve.orchard"), //
                new Fixture("valve.trees"));
    }

    private final ValveEntity entity = new ValveEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ValveEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(ValveEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ValveEntityTest#fixtures")
        @DisplayName("dispatches valve state, action switches, and position")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(ValveEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));
            assertThat(parsed.get("open"), is(new ParsedData.StateData(OnOffType.OFF)));
            assertThat(parsed.get("close"), is(new ParsedData.StateData(OnOffType.OFF)));
            assertThat(parsed.get("stop"), is(new ParsedData.StateData(OnOffType.OFF)));

            if (state.getAttributeAsInt("current_position") != null) {
                assertThat(parsed.get("current_position"), is(new ParsedData.StateData(
                        new PercentType(Objects.requireNonNull(state.getAttributeAsInt("current_position"))))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ValveEntityTest#fixtures")
        @DisplayName("maps open, close, stop, and set_position commands to services")
        void mapsServices(Fixture fixture) {
            EntityState state = fixture.load(ValveEntityTest.this);
            String id = fixture.entityId();

            assertService(entity.toServiceCall(id, "open", OnOffType.ON, state, context).orElseThrow(), "valve",
                    "open_valve", id, null);
            assertService(entity.toServiceCall(id, "close", OnOffType.ON, state, context).orElseThrow(), "valve",
                    "close_valve", id, null);
            assertService(entity.toServiceCall(id, "stop", OnOffType.ON, state, context).orElseThrow(), "valve",
                    "stop_valve", id, null);
            assertService(
                    entity.toServiceCall(id, "current_position", new PercentType(40), state, context).orElseThrow(),
                    "valve", "set_valve_position", id, Map.of("position", 40));
        }
    }
}
