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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;
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
import org.openhab.binding.hasslink.internal.entity.impl.DeviceTrackerEntity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Tests channel discovery and state mapping for Home Assistant device tracker entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Device tracker entity fixture contracts")
class DeviceTrackerEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("device_tracker.demo_anne_therese"), //
                new Fixture("device_tracker.demo_home_boy"), //
                new Fixture("device_tracker.demo_paulus"));
    }

    private final DeviceTrackerEntity entity = new DeviceTrackerEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attribute -> true, null);

    @Nested
    @DisplayName("Channel discovery tests")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DeviceTrackerEntityTest#fixtures")
        @DisplayName("discovers only channels backed by each fixture")
        void discoversFixtureChannels(Fixture fixture) {
            EntityState state = fixture.load(DeviceTrackerEntityTest.this);
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs.stream().map(ChannelSpec::attribute).toList(), containsInAnyOrder("", "location",
                    "latitude", "longitude", "gps_accuracy", "battery", "source_type"));
            assertThat(specs.stream().filter(spec -> spec.attribute().equals("host_name")).count(), is(0L));
            assertThat(specs.stream().filter(spec -> spec.attribute().equals("mac")).count(), is(0L));
            assertThat(specs.stream().filter(spec -> spec.attribute().equals("ip")).count(), is(0L));
            assertThat(state.getAttributeAsString("battery_level"), is(nullValue()));
            assertThat(state.getAttributeAsString("hostname"), is(nullValue()));
            assertThat(specs.stream().filter(spec -> spec.attribute().equals("battery")).findFirst().orElseThrow()
                    .itemType(), is(equalTo(ItemType.DIMENSIONLESS)));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch tests")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DeviceTrackerEntityTest#fixtures")
        @DisplayName("maps the raw tracker state and GPS attributes")
        void parsesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(DeviceTrackerEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);
            BigDecimal latitude = Objects.requireNonNull(state.getAttributeAsBigDecimal("latitude"));
            BigDecimal longitude = Objects.requireNonNull(state.getAttributeAsBigDecimal("longitude"));
            BigDecimal gpsAccuracy = Objects.requireNonNull(state.getAttributeAsBigDecimal("gps_accuracy"));
            BigDecimal battery = Objects.requireNonNull(state.getAttributeAsBigDecimal("battery"));

            assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(new StringType("not_home"))));
            assertThat(parsed.get("latitude"), is(new ParsedData.StateData(new DecimalType(latitude))));
            assertThat(parsed.get("longitude"), is(new ParsedData.StateData(new DecimalType(longitude))));
            assertThat(parsed.get("location"),
                    is(new ParsedData.StateData(new PointType(new DecimalType(latitude), new DecimalType(longitude)))));
            assertThat(parsed.get("gps_accuracy"),
                    is(new ParsedData.StateData(new QuantityType<>(gpsAccuracy, SIUnits.METRE))));
            assertThat(parsed.get("battery"), is(new ParsedData.StateData(new QuantityType<>(battery, Units.PERCENT))));
            assertThat(parsed.get("source_type"), is(new ParsedData.StateData(new StringType("gps"))));
            assertThat(parsed.containsKey("battery_level"), is(false));
            assertThat(parsed.containsKey("mac"), is(false));
            assertThat(parsed.containsKey("ip"), is(false));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch tests")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.DeviceTrackerEntityTest#fixtures")
        @DisplayName("does not create service calls for tracker state channels")
        void rejectsCommands(Fixture fixture) {
            EntityState state = fixture.load(DeviceTrackerEntityTest.this);
            Optional<?> primary = entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR,
                    new StringType(state.state()), state, context);
            Optional<?> battery = entity.toServiceCall(fixture.entityId(), "battery", new StringType("52"), state,
                    context);

            assertThat(primary, is(Optional.empty()));
            assertThat(battery, is(Optional.empty()));
        }
    }
}
