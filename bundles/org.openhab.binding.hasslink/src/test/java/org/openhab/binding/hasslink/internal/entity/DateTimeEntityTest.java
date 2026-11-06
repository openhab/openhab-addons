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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.DateTimeEntity;
import org.openhab.core.library.types.DateTimeType;

/**
 * Tests channel discovery, date-time state mapping, and commands for Home Assistant entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Date and time entity fixture contracts")
class DateTimeEntityTest extends AbstractEntityTest {

    private static final String FIXTURE_PATH = COMPRESSED_FIXTURE_ROOT + "datetime.date_and_time.json";
    private static final String ENTITY_ID = "datetime.date_and_time";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final DateTimeEntity entity = new DateTimeEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    private EntityState fixtureState() {
        return loadFixtureState(FIXTURE_PATH, ENTITY_ID);
    }

    private DateTimeType command() {
        return DateTimeType.valueOf("2024-05-06T07:08:09+00:00");
    }

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @Test
        @DisplayName("discovers exactly one primary DateTime channel")
        void discoversPrimaryChannel() {
            List<ChannelSpec> specs = entity.getChannelSpecs(fixtureState(), context);

            assertThat(specs, hasSize(1));
            ChannelSpec primary = specs.get(0);
            assertThat(primary.attribute(), is(EntityType.PRIMARY_ATTR));
            assertThat(primary.itemType(), is(ItemType.DATETIME));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @Test
        @DisplayName("parses raw fixture timestamp string into DateTimeType")
        void parsesFixtureState() {
            EntityState state = fixtureState();
            DateTimeType expected = DateTimeType.valueOf(state.state());

            assertThat(state.state(), is("2020-01-01T12:00:00+00:00"));
            assertThat(entity.parseState(state, context),
                    is(Map.of(EntityType.PRIMARY_ATTR, new ParsedData.StateData(expected))));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @Test
        @DisplayName("maps DateTime commands to set_value datetime service calls")
        void mapsDateTimeCommand() {
            DateTimeType command = command();
            String expectedValue = DATETIME_FORMATTER.format(command.getZonedDateTime(ZoneId.systemDefault()));

            assertService(entity.toServiceCall(ENTITY_ID, EntityType.PRIMARY_ATTR, command, fixtureState(), context)
                    .orElseThrow(), "datetime", "set_value", ENTITY_ID, Map.of("value", expectedValue));
        }
    }
}
