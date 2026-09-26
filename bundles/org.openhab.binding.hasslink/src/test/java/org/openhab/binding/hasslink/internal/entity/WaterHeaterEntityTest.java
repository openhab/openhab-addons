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

import java.math.BigDecimal;
import java.util.HashMap;
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
import org.openhab.binding.hasslink.internal.entity.impl.WaterHeaterEntity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonPrimitive;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant water heater entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Water heater entity fixture contracts")
class WaterHeaterEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("water_heater.demo_water_heater"), //
                new Fixture("water_heater.demo_water_heater_celsius"));
    }

    private final WaterHeaterEntity entity = new WaterHeaterEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WaterHeaterEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(WaterHeaterEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WaterHeaterEntityTest#fixtures")
        @DisplayName("exposes temperature range and operation mode options")
        void exposesStateDescriptions(Fixture fixture) {
            EntityState state = fixture.load(WaterHeaterEntityTest.this);

            StateDescriptionFragment range = entity.getStateDescriptionFragment(state, "temperature", context);
            assertThat(Objects.requireNonNull(range).getMinimum(), is(state.getAttributeAsBigDecimal("min_temp")));
            assertThat(range.getMaximum(), is(state.getAttributeAsBigDecimal("max_temp")));
            assertThat(range.getStep(), is(state.getAttributeAsBigDecimal("target_temp_step")));

            StateDescriptionFragment operations = entity.getStateDescriptionFragment(state, EntityType.PRIMARY_ATTR,
                    context);
            List<String> modes = Objects.requireNonNull(state.getAttributeAsList("operation_list")).stream()
                    .map(Object::toString).toList();
            assertThat(Objects.requireNonNull(Objects.requireNonNull(operations).getOptions()).stream()
                    .map(StateOption::getValue).toList(), is(modes));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WaterHeaterEntityTest#fixtures")
        @DisplayName("dispatches state, power, and optional away mode")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(WaterHeaterEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));
            assertThat(parsed.get("power"), is(new ParsedData.StateData(OnOffType.ON)));

            if (state.attributes().containsKey("away_mode")) {
                OnOffType awayMode = "on".equalsIgnoreCase(state.getAttributeAsString("away_mode")) ? OnOffType.ON
                        : OnOffType.OFF;
                assertThat(parsed.get("away_mode"), is(new ParsedData.StateData(awayMode)));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WaterHeaterEntityTest#fixtures")
        @DisplayName("parses temperature attributes with explicitly specified units")
        void parsesTemperatureAttributesWithTheirUnit(Fixture fixture) {
            EntityState state = fixture.load(WaterHeaterEntityTest.this);
            Map<String, com.google.gson.JsonElement> attributes = new HashMap<>(state.attributes());
            attributes.put("temperature_unit", new JsonPrimitive("°C"));
            EntityState withUnit = new EntityState(state.entityId(), state.state(), attributes);
            Map<String, ParsedData> parsed = entity.parseState(withUnit, context);

            BigDecimal target = Objects.requireNonNull(state.getAttributeAsBigDecimal("temperature"));
            assertThat(parsed.get("temperature"),
                    is(new ParsedData.StateData(QuantityType.valueOf(target.toPlainString() + " °C"))));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.WaterHeaterEntityTest#fixtures")
        @DisplayName("maps mode, power, away, and target temperature commands to services")
        void mapsServices(Fixture fixture) {
            EntityState state = fixture.load(WaterHeaterEntityTest.this);
            String id = fixture.entityId();

            assertService(entity.toServiceCall(id, EntityType.PRIMARY_ATTR, new StringType("eco"), state, context)
                    .orElseThrow(), "water_heater", "set_operation_mode", id, Map.of("operation_mode", "eco"));
            assertService(entity.toServiceCall(id, "power", OnOffType.ON, state, context).orElseThrow(), "water_heater",
                    "turn_on", id, null);
            assertService(entity.toServiceCall(id, "away_mode", OnOffType.ON, state, context).orElseThrow(),
                    "water_heater", "turn_away_mode_on", id, null);
            assertService(entity.toServiceCall(id, "temperature", new DecimalType(50), state, context).orElseThrow(),
                    "water_heater", "set_temperature", id, Map.of("temperature", 50.0));
            assertService(
                    entity.toServiceCall(id, "target_temp_high", new DecimalType(55), state, context).orElseThrow(),
                    "water_heater", "set_temperature", id, Map.of("temperature_high", 55.0));
            assertService(
                    entity.toServiceCall(id, "target_temp_low", new DecimalType(45), state, context).orElseThrow(),
                    "water_heater", "set_temperature", id, Map.of("temperature_low", 45.0));
        }
    }
}
