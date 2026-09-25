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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.DateEntity;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Tests channel discovery, date state mapping, and commands for Home Assistant date entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Date entity fixture contracts")
class DateEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("date.date"));
    }

    private final DateEntity entity = new DateEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DateEntityTest#fixtures")
        @DisplayName("discovers only the primary DateTime channel")
        void discoversPrimaryChannel(Fixture fixture) {
            EntityState state = fixture.load(DateEntityTest.this);
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            ChannelSpec primary = specs.get(0);
            assertThat(primary.attribute(), is(EntityType.PRIMARY_ATTR));
            assertThat(primary.itemType(), is(ItemType.DATETIME));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DateEntityTest#fixtures")
        @DisplayName("parses raw ISO date strings into DateTimeType")
        void parsesDateState(Fixture fixture) {
            EntityState state = fixture.load(DateEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(DateTimeType.valueOf(state.state()))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DateEntityTest#fixtures")
        @DisplayName("maps unavailable or unknown state to UNDEF")
        void handlesUnavailableState(Fixture fixture) {
            EntityState unavailable = new EntityState("date.anniversary", "unavailable", Map.of());
            Map<String, ParsedData> parsed = entity.parseState(unavailable, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(UnDefType.UNDEF)));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DateEntityTest#fixtures")
        @DisplayName("maps DateTimeType and String commands to date set_value service calls")
        void mapsSetDateServiceCall(Fixture fixture) {
            EntityState state = fixture.load(DateEntityTest.this);

            DateTimeType dtCommand = new DateTimeType(ZonedDateTime.parse("2026-09-24T00:00:00Z"));
            assertService(entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, dtCommand, state, context)
                    .orElseThrow(), "date", "set_value", fixture.entityId(), Map.of("value", "2026-09-24"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DateEntityTest#fixtures")
        @DisplayName("rejects commands on unsupported attributes")
        void rejectsUnsupportedAttributes(Fixture fixture) {
            EntityState state = fixture.load(DateEntityTest.this);

            assertThat(entity.toServiceCall(fixture.entityId(), "unknown_attr", new StringType("2026-09-24"), state,
                    context), is(Optional.empty()));
        }
    }
}
