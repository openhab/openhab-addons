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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.ClimateEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant climate entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Climate entity fixture contracts")
class ClimateEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("climate.ecobee"), //
                new Fixture("climate.heatpump"), //
                new Fixture("climate.hvac")); //
    }

    private final ClimateEntity entity = new ClimateEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("creates the common and fixture-specific climate channels")
        void createsExpectedChannels(Fixture fixture) {
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);

            EntityState state = fixture.load(ClimateEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            assertThat(channelIds, hasItems(primaryId, primaryId + "#power", primaryId + "#current_temperature"));
            if (state.getAttributeAsString("temperature") != null) {
                assertThat(channelIds, hasItems(primaryId + "#temperature"));
            }
            if (state.getAttributeAsString("target_temp_high") != null) {
                assertThat(channelIds, hasItems(primaryId + "#target_temp_high", primaryId + "#target_temp_low"));
            }
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps primary HVAC modes to state options")
        void mapsHvacModesToStateOptions(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, EntityType.PRIMARY_ATTR,
                    context);

            assertThat(fragment, is(notNullValue()));
            List<StateOption> options = Objects.requireNonNull(fragment).getOptions();
            List<String> expectedModes = state.getAttributeAsStringList("hvac_modes");
            assertThat(options, hasSize(expectedModes.size()));
            assertThat(Objects.requireNonNull(options).stream().map(StateOption::getValue).toList(),
                    containsInAnyOrder(expectedModes.toArray(new String[0])));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps preset, fan, and swing modes to channel options when present")
        void mapsAuxiliaryModesToStateOptions(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);

            List<String> presetModes = state.getAttributeAsStringList("preset_modes");
            if (!presetModes.isEmpty()) {
                StateDescriptionFragment presetFragment = entity.getStateDescriptionFragment(state, "preset_mode",
                        context);
                assertThat(presetFragment, is(notNullValue()));
                assertThat(Objects.requireNonNull(presetFragment).getOptions().stream().map(StateOption::getValue)
                        .toList(), containsInAnyOrder(presetModes.toArray(new String[0])));
            }

            List<String> fanModes = state.getAttributeAsStringList("fan_modes");
            if (!fanModes.isEmpty()) {
                StateDescriptionFragment fanFragment = entity.getStateDescriptionFragment(state, "fan_mode", context);
                assertThat(fanFragment, is(notNullValue()));
                assertThat(
                        Objects.requireNonNull(fanFragment).getOptions().stream().map(StateOption::getValue).toList(),
                        containsInAnyOrder(fanModes.toArray(new String[0])));
            }

            List<String> swingModes = state.getAttributeAsStringList("swing_modes");
            if (!swingModes.isEmpty()) {
                StateDescriptionFragment swingFragment = entity.getStateDescriptionFragment(state, "swing_mode",
                        context);
                assertThat(swingFragment, is(notNullValue()));
                assertThat(
                        Objects.requireNonNull(swingFragment).getOptions().stream().map(StateOption::getValue).toList(),
                        containsInAnyOrder(swingModes.toArray(new String[0])));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("extracts min, max, and step bounds for temperature and humidity channels")
        void mapsNumericRangeBounds(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);

            String minTempStr = state.getAttributeAsString("min_temp");
            String maxTempStr = state.getAttributeAsString("max_temp");
            String tempStepStr = state.getAttributeAsString("target_temp_step");

            if (minTempStr != null && maxTempStr != null) {
                BigDecimal minTemp = new BigDecimal(minTempStr);
                BigDecimal maxTemp = new BigDecimal(maxTempStr);

                StateDescriptionFragment tempFragment = entity.getStateDescriptionFragment(state, "temperature",
                        context);
                assertThat(tempFragment, is(notNullValue()));
                assertThat(Objects.requireNonNull(tempFragment).getMinimum(), is(minTemp));
                assertThat(tempFragment.getMaximum(), is(maxTemp));

                if (tempStepStr != null) {
                    BigDecimal tempStep = new BigDecimal(tempStepStr);
                    assertThat(tempFragment.getStep(), is(tempStep));
                }
            }

            String minHumidityStr = state.getAttributeAsString("min_humidity");
            String maxHumidityStr = state.getAttributeAsString("max_humidity");
            if (minHumidityStr != null && maxHumidityStr != null) {
                StateDescriptionFragment humidityFragment = entity.getStateDescriptionFragment(state, "humidity",
                        context);
                assertThat(humidityFragment, is(notNullValue()));
                assertThat(Objects.requireNonNull(humidityFragment).getMinimum(), is(new BigDecimal(minHumidityStr)));
                assertThat(humidityFragment.getMaximum(), is(new BigDecimal(maxHumidityStr)));
            }
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("dispatches every fixture state according to its available attributes")
        void dispatchesExpectedStates(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);
            Map<String, ParsedData> states = entity.parseState(state, context);

            // Primary HVAC Mode
            assertThat(states.get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));

            // Power State
            boolean expectedPower = !state.isUnavailableOrUnknown() && !"off".equalsIgnoreCase(state.state());
            assertThat(states.get("power"), is(new ParsedData.StateData(OnOffType.from(expectedPower))));

            // Temperature Attributes
            for (String attribute : List.of("current_temperature", "temperature", "target_temp_high",
                    "target_temp_low")) {
                String value = state.getAttributeAsString(attribute);
                if (value != null) {
                    assertThat(states.get(attribute), is(new ParsedData.StateData(new QuantityType<>(value + " °C"))));
                }
            }

            // Humidity Attributes
            Double currentHumidity = state.getAttributeAsDouble("current_humidity");
            if (currentHumidity != null) {
                assertThat(states.get("current_humidity"),
                        is(new ParsedData.StateData(new QuantityType<>(currentHumidity, Units.PERCENT))));
            }

            Double targetHumidity = state.getAttributeAsDouble("humidity");
            if (targetHumidity != null) {
                assertThat(states.get("humidity"),
                        is(new ParsedData.StateData(new QuantityType<>(targetHumidity, Units.PERCENT))));
            }

            // String Modes
            for (String modeAttr : List.of("fan_mode", "preset_mode", "swing_mode")) {
                String val = state.getAttributeAsString(modeAttr);
                if (val != null) {
                    assertThat(states.get(modeAttr), is(new ParsedData.StateData(new StringType(val))));
                }
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps primary HVAC mode to set_hvac_mode service")
        void mapsHvacModeCommand(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);
            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, new StringType(state.state()),
                            state, context).orElseThrow(),
                    "climate", "set_hvac_mode", fixture.entityId(), Map.of("hvac_mode", state.state()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps power ON and OFF to climate turn_on and turn_off services")
        void mapsPowerCommands(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);

            assertService(entity.toServiceCall(fixture.entityId(), "power", OnOffType.ON, state, context).orElseThrow(),
                    "climate", "turn_on", fixture.entityId(), null);

            assertService(
                    entity.toServiceCall(fixture.entityId(), "power", OnOffType.OFF, state, context).orElseThrow(),
                    "climate", "turn_off", fixture.entityId(), null);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps target temperature commands to set_temperature service")
        void mapsTargetTemperatureCommands(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);

            Double temp = state.getAttributeAsDouble("temperature");
            if (temp != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "temperature", new DecimalType(temp), state, context)
                                .orElseThrow(),
                        "climate", "set_temperature", fixture.entityId(), Map.of("temperature", temp));
            }

            Double high = state.getAttributeAsDouble("target_temp_high");
            if (high != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "target_temp_high", new DecimalType(high), state,
                                context).orElseThrow(),
                        "climate", "set_temperature", fixture.entityId(), Map.of("target_temp_high", high));
            }

            Double low = state.getAttributeAsDouble("target_temp_low");
            if (low != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "target_temp_low", new DecimalType(low), state,
                                context).orElseThrow(),
                        "climate", "set_temperature", fixture.entityId(), Map.of("target_temp_low", low));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps humidity commands to set_humidity service via quantity or percentage")
        void mapsHumidityCommand(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);
            Double targetHumidity = state.getAttributeAsDouble("humidity");

            if (targetHumidity != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "humidity", new PercentType(targetHumidity.intValue()),
                                state, context).orElseThrow(),
                        "climate", "set_humidity", fixture.entityId(),
                        Map.of("humidity", (double) targetHumidity.intValue()));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ClimateEntityTest#fixtures")
        @DisplayName("maps preset, fan, and swing mode commands to their respective climate services")
        void mapsAuxiliaryModeCommands(Fixture fixture) {
            EntityState state = fixture.load(ClimateEntityTest.this);

            String fanMode = state.getAttributeAsString("fan_mode");
            if (fanMode != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "fan_mode", new StringType(fanMode), state, context)
                                .orElseThrow(),
                        "climate", "set_fan_mode", fixture.entityId(), Map.of("fan_mode", fanMode));
            }

            String presetMode = state.getAttributeAsString("preset_mode");
            if (presetMode != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "preset_mode", new StringType(presetMode), state,
                                context).orElseThrow(),
                        "climate", "set_preset_mode", fixture.entityId(), Map.of("preset_mode", presetMode));
            }

            String swingMode = state.getAttributeAsString("swing_mode");
            if (swingMode != null) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), "swing_mode", new StringType(swingMode), state,
                                context).orElseThrow(),
                        "climate", "set_swing_mode", fixture.entityId(), Map.of("swing_mode", swingMode));
            }
        }
    }
}
