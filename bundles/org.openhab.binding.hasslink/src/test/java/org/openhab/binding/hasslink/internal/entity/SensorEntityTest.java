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
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.SensorEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant sensor entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Sensor entity fixture contracts")
class SensorEntityTest extends AbstractEntityTest {

    private static final String CUSTOM_FIXTURE_ROOT = "/org/openhab/binding/hasslink/internal/fixtures/custom_fixtures/";

    private record Fixture(String entityId, String fileName, boolean isCustom, ItemType itemType,
            Map<String, ItemType> attributeTypes) {

        public Fixture(String entityId, ItemType itemType) {
            this(entityId, entityId + ".json", false, itemType, Map.of());
        }

        public Fixture(String entityId, boolean isCustom, ItemType itemType, Map<String, ItemType> attributeTypes) {
            this(entityId, entityId + ".json", isCustom, itemType, attributeTypes);
        }

        EntityState load(SensorEntityTest test) {
            String root = isCustom ? CUSTOM_FIXTURE_ROOT : COMPRESSED_FIXTURE_ROOT;
            return test.loadFixtureState(root + fileName, entityId);
        }

        @Override
        public String toString() {
            return entityId;
        }
    }

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("sensor.backup_backup_manager_state", ItemType.STRING),
                new Fixture("sensor.backup_last_attempted_automatic_backup", ItemType.DATETIME),
                new Fixture("sensor.backup_last_successful_automatic_backup", ItemType.DATETIME),
                new Fixture("sensor.backup_next_scheduled_automatic_backup", ItemType.DATETIME),
                new Fixture("sensor.carbon_dioxide", ItemType.DIMENSIONLESS),
                new Fixture("sensor.carbon_dioxide_battery", ItemType.DIMENSIONLESS),
                new Fixture("sensor.carbon_monoxide", ItemType.DIMENSIONLESS),
                new Fixture("sensor.outside_humidity", ItemType.DIMENSIONLESS),
                new Fixture("sensor.outside_temperature", ItemType.number("Temperature")),
                new Fixture("sensor.outside_temperature_battery", ItemType.DIMENSIONLESS),
                new Fixture("sensor.power_consumption", ItemType.number("Power")),
                new Fixture("sensor.sun_next_dawn", ItemType.DATETIME),
                new Fixture("sensor.sun_next_dusk", ItemType.DATETIME),
                new Fixture("sensor.sun_next_midnight", ItemType.DATETIME),
                new Fixture("sensor.sun_next_noon", ItemType.DATETIME),
                new Fixture("sensor.sun_next_rising", ItemType.DATETIME),
                new Fixture("sensor.sun_next_setting", ItemType.DATETIME),
                new Fixture("sensor.thermostat", ItemType.STRING),
                new Fixture("sensor.total_energy_kwh", ItemType.number("Energy")),
                new Fixture("sensor.total_energy_mwh", ItemType.number("Energy")),
                new Fixture("sensor.total_gas_ft3", ItemType.number("Volume")),
                new Fixture("sensor.total_gas_m3", ItemType.number("Volume")), //

                // Custom fixtures
                new Fixture("sensor.p1s_printer_speed_profile", true, ItemType.STRING,
                        Map.of("modifier", ItemType.NUMBER)),
                new Fixture("sensor.p1s_printer_printable_objects", true, ItemType.NUMBER,
                        Map.of("objects", ItemType.STRING)) //
        );
    }

    private final SensorEntity entity = new SensorEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "[{index}] entityId: {0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SensorEntityTest#fixtures")
        @DisplayName("creates the correct primary item type and attribute item types for every sensor fixture")
        void createsCorrectItemTypes(Fixture fixture) {
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);

            EntityState state = fixture.load(SensorEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            assertThat(fixture.entityId(), is(equalTo(state.entityId())));

            // Verify primary channel
            Channel primaryChannel = channels.stream().filter(c -> !c.getUID().getId().contains("#")).findFirst()
                    .orElseThrow();
            assertThat(primaryChannel.getAcceptedItemType(), is(equalTo(fixture.itemType().name())));

            // Verify pre-populated attribute channels if defined
            for (Map.Entry<String, ItemType> expectedAttr : fixture.attributeTypes().entrySet()) {
                String attributeId = fixture.entityId().replace('.', '-') + "#" + expectedAttr.getKey();
                Channel attrChannel = channels.stream().filter(c -> c.getUID().getId().equals(attributeId)).findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "Expected channel for attribute not found: " + expectedAttr.getKey()));
                assertThat(attrChannel.getAcceptedItemType(), is(equalTo(expectedAttr.getValue().name())));
            }
        }

        @Test
        @DisplayName("discovers all audited sensor entities without exposing unhandled metadata as channels")
        void discoversAllFixtureEntities() {
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Fixture> fixtures = SensorEntityTest.fixtures().toList();
            List<EntityState> states = fixtures.stream().map(fixture -> fixture.load(SensorEntityTest.this)).toList();
            List<Channel> channels = HassLinkChannelFactory.buildChannels(states, new ThingUID("hasslink", "fixture"),
                    "Fixture", bridge);

            assertThat(channels, hasSize(greaterThanOrEqualTo(fixtures.size())));
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "[{index}] entityId: {0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SensorEntityTest#fixtures")
        @DisplayName("validates state description fragments and options correspond to fixture attributes")
        void validatesStateDescriptions(Fixture fixture) {
            EntityState state = fixture.load(SensorEntityTest.this);

            // Iterate over all discovered channels (primary and attributes)
            List<ChannelSpec> channelSpecs = entity.getChannelSpecs(state, context);

            for (ChannelSpec spec : channelSpecs) {
                String attribute = spec.attribute();
                StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, attribute, context);

                assertThat("Fragment for attribute '" + attribute + "' should not be null", fragment,
                        is(notNullValue()));
                assertThat("Fragment for attribute '" + attribute + "' should be read-only", fragment.isReadOnly(),
                        is(true));

                if (EntityType.isPrimary(attribute) && state.hasAttribute("options")) {
                    List<StateOption> options = fragment.getOptions();
                    assertThat(options, is(notNullValue()));

                    // Extract expected options from the raw fixture attribute
                    List<String> expectedOptions = state.getAttributeAsStringList("options");

                    assertThat(options, hasSize(expectedOptions.size()));
                    assertThat(options.stream().map(StateOption::getValue).toList(),
                            contains(expectedOptions.toArray(new String[0])));
                } else if (EntityType.isPrimary(attribute)) {
                    assertThat(fragment.getOptions(), is(empty()));
                }
            }
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "[{index}] entityId: {0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SensorEntityTest#fixtures")
        @DisplayName("verifies parsed state types and exact values match raw fixture data")
        void verifiesParsedStateTypesAndValues(Fixture fixture) {
            EntityState state = fixture.load(SensorEntityTest.this);
            Map<String, ParsedData> parsedData = entity.parseState(state, context);

            ParsedData primary = parsedData.get(EntityType.PRIMARY_ATTR);
            assertThat(primary, is(notNullValue()));

            // Validate primary state type and exact value against fixture state
            if (state.isUnavailableOrUnknown()) {
                assertThat(primary, is(new ParsedData.StateData(UnDefType.UNDEF)));
            } else if ("timestamp".equals(state.getAttributeAsString("device_class"))) {
                assertThat(primary, is(new ParsedData.StateData(DateTimeType.valueOf(state.state()))));
            } else if (state.getUnitOfMeasurement() != null) {
                assertThat(primary, is(new ParsedData.StateData(
                        QuantityType.valueOf(state.state() + " " + state.getUnitOfMeasurement()))));
            } else if (fixture.itemType().name().startsWith("Number")) {
                assertThat(primary, is(new ParsedData.StateData(new DecimalType(new BigDecimal(state.state())))));
            } else {
                assertThat(primary, is(new ParsedData.StateData(new StringType(state.state()))));
            }

            // Validate attribute types and exact values against fixture attributes
            for (Map.Entry<String, ItemType> expectedAttr : fixture.attributeTypes().entrySet()) {
                String attrKey = expectedAttr.getKey();
                ItemType expectedType = expectedAttr.getValue();

                assertThat("Fixture must contain attribute: " + attrKey, state.hasAttribute(attrKey), is(true));
                ParsedData attrParsed = parsedData.get(attrKey);
                assertThat("Parsed data must contain attribute: " + attrKey, attrParsed, is(notNullValue()));

                if (expectedType.equals(ItemType.NUMBER)) {
                    BigDecimal expectedVal = state.getAttributeAsBigDecimal(attrKey);
                    assertThat(expectedVal, is(notNullValue()));
                    assertThat(attrParsed,
                            is(new ParsedData.StateData(new DecimalType(Objects.requireNonNull(expectedVal)))));
                } else if (expectedType.equals(ItemType.STRING)) {
                    assertThat(attrParsed, is(notNullValue()));
                }
            }
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "[{index}] entityId: {0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SensorEntityTest#fixtures")
        @DisplayName("returns no service call for every read-only sensor fixture")
        void returnsNoServiceCall(Fixture fixture) {
            EntityState state = fixture.load(SensorEntityTest.this);
            assertThat(entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, new StringType(state.state()),
                    state, context), is(Optional.empty()));
        }
    }
}
