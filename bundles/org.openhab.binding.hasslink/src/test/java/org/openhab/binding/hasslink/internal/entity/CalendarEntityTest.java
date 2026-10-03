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
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.CalendarEntity;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant calendar entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Calendar entity fixture contracts")
class CalendarEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("calendar.calendar_1"), new Fixture("calendar.calendar_2"));
    }

    private final CalendarEntity entity = new CalendarEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CalendarEntityTest#fixtures")
        @DisplayName("discovers the primary state and present event message channels")
        void discoversExpectedChannels(Fixture fixture) {
            EntityState state = fixture.load(CalendarEntityTest.this);
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(2));
            assertThat(specs.stream().map(ChannelSpec::attribute).toList(),
                    containsInAnyOrder(EntityType.PRIMARY_ATTR, "message"));

            ChannelSpec primary = specs.stream() //
                    .filter(spec -> EntityType.isPrimary(spec.attribute())) //
                    .findFirst() //
                    .orElseThrow();

            assertThat(primary.itemType(), is(ItemType.SWITCH));
            assertThat(primary.kind(), is(ChannelKind.STATE));
            assertThat(primary.autoUpdatePolicy(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CalendarEntityTest#fixtures")
        @DisplayName("maps each raw calendar state and event message")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(CalendarEntityTest.this);
            OnOffType expectedState = "on".equals(state.state()) ? OnOffType.ON : OnOffType.OFF;
            String message = Objects.requireNonNull(state.getAttributeAsString("message"));

            assertThat(entity.parseState(state, context), //
                    is(Map.of( //
                            EntityType.PRIMARY_ATTR, new ParsedData.StateData(expectedState), //
                            "message", new ParsedData.StateData(new StringType(message)))));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CalendarEntityTest#fixtures")
        @DisplayName("rejects commands for read-only calendar channels")
        void rejectsCommands(Fixture fixture) {
            EntityState state = fixture.load(CalendarEntityTest.this);

            assertThat(entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.ON, state, context),
                    is(Optional.empty()));
            assertThat(entity.toServiceCall(fixture.entityId(), "message", new StringType("Updated"), state, context),
                    is(Optional.empty()));
        }
    }
}
