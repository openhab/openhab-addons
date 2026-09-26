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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.impl.LightEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant light entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Light entity fixture contracts")
class LightEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("light.bed_light"), //
                new Fixture("light.ceiling_lights"), //
                new Fixture("light.entrance_color_white_lights"), //
                new Fixture("light.kitchen_lights"), //
                new Fixture("light.living_room_rgbww_lights"), //
                new Fixture("light.office_rgbw_lights"));
    }

    private final LightEntity entity = new LightEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("creates only channels supported by each light fixture")
        void createsSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();
            String primaryId = fixture.entityId().replace('.', '-');

            assertThat(channelIds, containsInAnyOrder(expectedChannels(state, primaryId).toArray(new String[0])));
        }

        private List<String> expectedChannels(EntityState state, String primaryId) {
            List<String> channels = new ArrayList<>(List.of(primaryId));
            List<String> modes = state.getAttributeAsStringList("supported_color_modes");
            if (modes.contains("color_temp")) {
                channels.add(primaryId + "#color_temp");
                if (state.getAttributeAsInt("min_color_temp_kelvin") != null
                        && state.getAttributeAsInt("max_color_temp_kelvin") != null) {
                    channels.add(primaryId + "#color_temp_percent");
                }
            }
            if (state.getAttributeAsList("effect_list") != null) {
                channels.add(primaryId + "#effect");
            }
            return channels;
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("exposes fixture color temperature ranges and effects")
        void exposesDynamicDescriptions(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            StateDescriptionFragment temperature = entity.getStateDescriptionFragment(state, "color_temp", context);
            Integer min = state.getAttributeAsInt("min_color_temp_kelvin");
            Integer max = state.getAttributeAsInt("max_color_temp_kelvin");
            if (min != null && max != null) {
                assertThat(temperature, is(notNullValue()));
                assertThat(Objects.requireNonNull(temperature).getMinimum(), is(new BigDecimal(min)));
                assertThat(temperature.getMaximum(), is(new BigDecimal(max)));
            } else {
                assertThat(temperature, is(nullValue()));
            }

            StateDescriptionFragment effects = entity.getStateDescriptionFragment(state, "effect", context);
            List<String> effectList = state.getAttributeAsStringList("effect_list");
            if (effectList.isEmpty()) {
                assertThat(effects, is(nullValue()));
            } else {
                assertThat(effects, is(notNullValue()));
                assertThat(Objects.requireNonNull(effects).getOptions().stream().map(StateOption::getValue).toList(),
                        contains(effectList.toArray(new String[0])));
            }
            assertThat(entity.getStateDescriptionFragment(state, "missing", context), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("parses every raw fixture state and available light attributes")
        void parsesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            String effect = state.getAttributeAsString("effect");
            if (effect != null) {
                assertThat(parsed, hasEntry(equalTo("effect"), is(new ParsedData.StateData(new StringType(effect)))));
            }
            ParsedData.StateData primary = (ParsedData.StateData) parsed.get(EntityType.PRIMARY_ATTR);
            if ("off".equals(state.state())) {
                assertThat(primary.state(), is(OnOffType.OFF));
            } else {
                List<?> hsColor = Objects.requireNonNull(state.getAttributeAsList("hs_color"));
                HSBType hsb = (HSBType) primary.state();
                assertThat(hsb.getHue(), is(new DecimalType(((Number) hsColor.get(0)).doubleValue())));
                assertThat(hsb.getSaturation().doubleValue(),
                        closeTo(((Number) hsColor.get(1)).doubleValue(), 0.000001));
                Integer brightness = state.getAttributeAsInt("brightness");
                assertThat(hsb.getBrightness(), is(new PercentType(new BigDecimal((brightness / 255.0) * 100.0))));
            }
            Integer colorTemp = state.getAttributeAsInt("color_temp_kelvin");
            if (colorTemp != null) {
                assertThat(parsed, hasEntry(equalTo("color_temp"), is(new ParsedData.StateData(
                        new QuantityType<>(colorTemp, org.openhab.core.library.unit.Units.KELVIN)))));
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("maps primary on off commands to light services")
        void mapsPowerCommands(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.ON, state, context)
                            .orElseThrow(),
                    "light", "turn_on", fixture.entityId(), null);
            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, OnOffType.OFF, state, context)
                            .orElseThrow(),
                    "light", "turn_off", fixture.entityId(), null);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("maps an openHAB temperature percentage back to the fixture Kelvin value")
        void roundTripsColorTemperaturePercentage(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            Integer kelvin = state.getAttributeAsInt("color_temp_kelvin");
            if (kelvin != null) {
                Integer min = state.getAttributeAsInt("min_color_temp_kelvin");
                Integer max = state.getAttributeAsInt("max_color_temp_kelvin");
                double parsedPercent = 100.0 - (kelvin - min) * 100.0 / (max - min);
                assertService(
                        entity.toServiceCall(fixture.entityId(), "color_temp_percent",
                                new PercentType(BigDecimal.valueOf(parsedPercent)), state, context).orElseThrow(),
                        "light", "turn_on", fixture.entityId(), Map.of("color_temp_kelvin", kelvin.longValue()));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("maps fixture effects to the light turn on service")
        void mapsEffects(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            String effect = state.getAttributeAsString("effect");
            if (effect != null) {
                assertService(entity.toServiceCall(fixture.entityId(), "effect", new StringType(effect), state, context)
                        .orElseThrow(), "light", "turn_on", fixture.entityId(), Map.of("effect", effect));
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.LightEntityTest#fixtures")
        @DisplayName("maps color commands using the fixture's supported color mode")
        void mapsColorCommands(Fixture fixture) {
            EntityState state = fixture.load(LightEntityTest.this);
            HSBType command = new HSBType(new DecimalType(0), PercentType.HUNDRED, PercentType.HUNDRED);
            ServiceCall call = entity
                    .toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, command, state, context).orElseThrow();
            if (state.getAttributeAsStringList("supported_color_modes").stream()
                    .anyMatch(mode -> mode.equals("rgb") || mode.equals("rgbw") || mode.equals("rgbww"))) {
                assertService(call, "light", "turn_on", fixture.entityId(), Map.of("rgb_color", List.of(255, 0, 0)));
            } else {
                assertService(call, "light", "turn_on", fixture.entityId(),
                        Map.of("hs_color", List.of(0.0, 100.0), "brightness", 255));
            }
        }
    }
}
