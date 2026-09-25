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
import org.openhab.binding.hasslink.internal.entity.impl.UpdateEntity;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery and state mapping for Home Assistant update entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Update entity fixture contracts")
class UpdateEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("update.demo_add_on"), //
                new Fixture("update.demo_living_room_bulb_update"), //
                new Fixture("update.demo_no_update"), //
                new Fixture("update.demo_update_no_install"), //
                new Fixture("update.demo_update_with_decimal_progress"), //
                new Fixture("update.demo_update_with_progress"));
    }

    private final UpdateEntity entity = new UpdateEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.UpdateEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(UpdateEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.UpdateEntityTest#fixtures")
        @DisplayName("dispatches update state and version attributes")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(UpdateEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);
            OnOffType expected = "on".equalsIgnoreCase(state.state()) ? OnOffType.ON : OnOffType.OFF;

            assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(expected)));
            assertThat(parsed.get("installed_version"), is(new ParsedData.StateData(
                    new StringType(Objects.requireNonNull(state.getAttributeAsString("installed_version"))))));
            assertThat(parsed.get("latest_version"), is(new ParsedData.StateData(
                    new StringType(Objects.requireNonNull(state.getAttributeAsString("latest_version"))))));
        }
    }
}
