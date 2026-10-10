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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.EventEntity;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Tests channel discovery, event state handling, and commands for Home Assistant event entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Event entity fixture contracts")
class EventEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("event.push_button_press"), //
                new Fixture("event.backup_automatic_backup"));
    }

    private final EventEntity entity = new EventEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.EventEntityTest#fixtures")
        @DisplayName("discovers only the primary String trigger channel")
        void discoversPrimaryTriggerChannel(Fixture fixture) {
            EntityState state = fixture.load(EventEntityTest.this);
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            ChannelSpec primary = specs.get(0);
            assertThat(primary.attribute(), is(EntityType.PRIMARY_ATTR));
            assertThat(primary.itemType(), is(ItemType.STRING));
            assertThat(primary.kind(), is(ChannelKind.TRIGGER));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.EventEntityTest#fixtures")
        @DisplayName("dispatches event state prioritizing event_type attribute over raw state")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(EventEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            String eventType = state.getAttributeAsString("event_type");
            String expected = eventType != null ? eventType : state.state();

            assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(new StringType(expected))));
        }
    }
}
