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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.BinarySensorEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;

import com.google.gson.Gson;

/**
 * Tests channel, state, and command behavior for Home Assistant binary sensor entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Binary sensor entity fixture contracts")
class BinarySensorEntityTest extends AbstractEntityTest {

    private record Fixture(String entityId, String fileName, String deviceClass) {
        public Fixture(String entityId, String deviceClass) {
            this(entityId, entityId + ".json", deviceClass);
        }

        EntityState load(BinarySensorEntityTest test) {
            return test.loadFixtureState(COMPRESSED_FIXTURE_ROOT + fileName, entityId);
        }

        @Override
        public String toString() {
            return entityId;
        }
    }

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("binary_sensor.movement_backyard", "motion"),
                new Fixture("binary_sensor.outside_temperature_battery_charging", "battery_charging"),
                new Fixture("binary_sensor.basement_floor_wet", "moisture"));
    }

    private final BinarySensorEntity entity = new BinarySensorEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.BinarySensorEntityTest#fixtures")
        @DisplayName("discovers only the primary switch channel for each fixture")
        void discoversOnlyPrimarySwitchChannel(Fixture fixture) {
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            EntityState state = fixture.load(BinarySensorEntityTest.this);

            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            assertThat(channels, hasSize(1));
            assertThat(channels.get(0).getUID().getId(), is(equalTo(fixture.entityId().replace('.', '-'))));
            assertThat(channels.get(0).getAcceptedItemType(), is(equalTo(ItemType.SWITCH.name())));
        }

        @Test
        @DisplayName("discovers one primary channel for all binary sensor fixtures")
        void discoversAllFixturesWithoutOptionalChannels() {
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Fixture> fixtures = BinarySensorEntityTest.fixtures().toList();
            List<EntityState> states = fixtures.stream().map(fixture -> fixture.load(BinarySensorEntityTest.this))
                    .toList();

            List<Channel> channels = HassLinkChannelFactory.buildChannels(states, new ThingUID("hasslink", "fixture"),
                    "Fixture", bridge);

            assertThat(channels, hasSize(fixtures.size()));
            assertThat(channels.stream().map(channel -> channel.getUID().getId()).toList(), containsInAnyOrder(
                    fixtures.stream().map(fixture -> fixture.entityId().replace('.', '-')).toArray()));
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.BinarySensorEntityTest#fixtures")
        @DisplayName("marks the primary channel read-only")
        void describesPrimaryChannel(Fixture fixture) {
            EntityState state = fixture.load(BinarySensorEntityTest.this);
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, EntityType.PRIMARY_ATTR,
                    context);

            assertThat(fragment, is(notNullValue()));
            assertThat(Objects.requireNonNull(fragment).isReadOnly(), is(true));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.BinarySensorEntityTest#fixtures")
        @DisplayName("maps every raw on or off fixture state to an openHAB switch state")
        void mapsRawFixtureState(Fixture fixture) {
            EntityState state = fixture.load(BinarySensorEntityTest.this);
            ParsedData parsed = entity.parseState(state, context).get(EntityType.PRIMARY_ATTR);
            OnOffType expected = "on".equals(state.state()) ? OnOffType.ON : OnOffType.OFF;

            assertThat(parsed, is(new ParsedData.StateData(expected)));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.BinarySensorEntityTest#fixtures")
        @DisplayName("does not create service calls for read-only binary sensors")
        void doesNotCreateServiceCall(Fixture fixture) {
            EntityState state = fixture.load(BinarySensorEntityTest.this);

            assertThat(entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.ON, state, context)
                    .isEmpty(), is(true));
        }
    }

    @Nested
    @DisplayName("Device class and item type mapping")
    class DeviceClassMappingTests {

        private static Stream<Arguments> deviceClassMappings() {
            return Stream.of( //
                    Arguments.of("door", ItemType.CONTACT), //
                    Arguments.of("window", ItemType.CONTACT), //
                    Arguments.of("garage_door", ItemType.CONTACT), //
                    Arguments.of("opening", ItemType.CONTACT), //
                    Arguments.of("motion", ItemType.SWITCH), //
                    Arguments.of("occupancy", ItemType.SWITCH), //
                    Arguments.of("smoke", ItemType.SWITCH), //
                    Arguments.of(null, ItemType.SWITCH) //
            );
        }

        @ParameterizedTest(name = "device_class '{0}' maps to item type {1}")
        @MethodSource("deviceClassMappings")
        void mapsDeviceClassToCorrectItemType(String deviceClass, ItemType expectedItemType) {
            Gson gson = new Gson();
            Map<String, com.google.gson.JsonElement> attributes = deviceClass != null //
                    ? Map.of("device_class", gson.toJsonTree(deviceClass)) //
                    : Map.of();

            EntityState state = new EntityState("binary_sensor.test", "off", attributes);

            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            assertThat(channels, hasSize(1));
            assertThat(channels.get(0).getAcceptedItemType(), is(equalTo(expectedItemType.name())));
        }
    }
}
