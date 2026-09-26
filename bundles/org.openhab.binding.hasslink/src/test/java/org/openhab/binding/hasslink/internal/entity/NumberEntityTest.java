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
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
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
import org.openhab.binding.hasslink.internal.entity.impl.NumberEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * Tests for {@link NumberEntity} to verify channel discovery, state parsing,
 * state description fragments, and service call mapping contracts across number fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Number entity fixture contracts")
class NumberEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("number.pwm_1"), //
                new Fixture("number.small_range"), //
                new Fixture("number.temperature_setting"), //
                new Fixture("number.volume"), //
                new Fixture("number.large_range"));
    }

    private final NumberEntity entity = new NumberEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.NumberEntityTest#fixtures")
        @DisplayName("discovers primary inferred channel")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(NumberEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            assertThat(channelIds, containsInAnyOrder(primaryId));
            assertThat(channels, hasSize(1));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.NumberEntityTest#fixtures")
        @DisplayName("dispatches primary numeric state correctly")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(NumberEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            String expectedValue = state.state();
            if (state.getUnitOfMeasurement() != null) {
                assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(
                        QuantityType.valueOf(expectedValue + " " + state.getUnitOfMeasurement()))));
            } else {
                assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                        is(new ParsedData.StateData(new DecimalType(expectedValue))));
            }
        }
    }

    @Nested
    @DisplayName("State description fragments")
    class StateDescriptionFragmentTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.NumberEntityTest#fixtures")
        @DisplayName("provides range state description fragment")
        void providesRangeFragment(Fixture fixture) {
            EntityState state = fixture.load(NumberEntityTest.this);
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, EntityType.PRIMARY_ATTR,
                    context);

            assertThat(fragment, is(notNullValue()));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.NumberEntityTest#fixtures")
        @DisplayName("maps primary commands to number.set_value service calls")
        void mapsCommands(Fixture fixture) {
            EntityState state = fixture.load(NumberEntityTest.this);
            String entityId = fixture.entityId();
            String unit = state.getUnitOfMeasurement();

            org.openhab.core.types.Command command = unit != null ? QuantityType.valueOf("50 " + unit)
                    : new DecimalType(50);

            ServiceCall call = entity.toServiceCall(entityId, EntityType.PRIMARY_ATTR, command, state, context)
                    .orElseThrow();

            assertThat(call.domain(), is("number"));
            assertThat(call.service(), is("set_value"));
            assertThat(call.entityId(), is(entityId));
            assertThat(new BigDecimal(String.valueOf(call.serviceData().get("value"))), is(new BigDecimal("50.0")));
        }
    }
}
