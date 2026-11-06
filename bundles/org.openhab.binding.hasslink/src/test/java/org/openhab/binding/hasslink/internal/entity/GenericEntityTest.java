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
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.GenericEntity;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/**
 * Tests fallback channel inference, state mapping, and commands for generic entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Generic fallback entity type inference contracts")
class GenericEntityTest extends AbstractEntityTest {

    private final GenericEntity entity = new GenericEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    private EntityState createEntityState(String entityId, String state, Map<String, Object> attributes) {
        Map<String, JsonElement> jsonAttributes = attributes.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue() == null ? JsonNull.INSTANCE : GSON.toJsonTree(e.getValue())));
        return new EntityState(entityId, state, jsonAttributes);
    }

    private EntityState createEntityState(String entityId, String state) {
        return createEntityState(entityId, state, Map.of());
    }

    @Nested
    @DisplayName("Channel discovery type inference")
    class ChannelDiscoveryTests {

        @Test
        @DisplayName("infers Switch item type for on/off primary state")
        void infersSwitchChannel() {
            EntityState state = createEntityState("custom.device", "on");
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.SWITCH));
        }

        @Test
        @DisplayName("infers Number item type for numeric primary state")
        void infersNumberChannel() {
            EntityState state = createEntityState("custom.device", "42.5");
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.NUMBER));
        }

        @Test
        @DisplayName("infers DateTime item type when device_class is timestamp")
        void infersDateTimeChannel() {
            EntityState state = createEntityState("custom.device", "2026-09-24T12:00:00+00:00",
                    Map.of("device_class", "timestamp"));
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.DATETIME));
        }

        @Test
        @DisplayName("falls back to String item type for arbitrary text primary state")
        void infersStringChannel() {
            EntityState state = createEntityState("custom.device", "disarmed");
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.STRING));
        }

        @Test
        @DisplayName("infers channel item types for generic attributes")
        void infersAttributeChannelTypes() {
            EntityState state = createEntityState("custom.device", "ok",
                    Map.of("battery_charging", true, "battery_level", 85.0, "firmware_version", "1.2.3"));

            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);
            assertThat(specs, hasSize(4));

            Map<String, ItemType> attributeTypes = specs.stream()
                    .collect(Collectors.toMap(ChannelSpec::attribute, ChannelSpec::itemType));

            assertThat(attributeTypes.get("battery_charging"), is(ItemType.SWITCH));
            assertThat(attributeTypes.get("battery_level"), is(ItemType.NUMBER));
            assertThat(attributeTypes.get("firmware_version"), is(ItemType.STRING));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch type inference")
    class InboundStateDispatchTests {

        @Test
        @DisplayName("parses on/off states to OnOffType")
        void parsesOnOffState() {
            EntityState onState = createEntityState("custom.device", "on");
            EntityState offState = createEntityState("custom.device", "off");

            assertThat(entity.parseState(onState, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(OnOffType.ON)));
            assertThat(entity.parseState(offState, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(OnOffType.OFF)));
        }

        @Test
        @DisplayName("parses numeric primary state to DecimalType or QuantityType when unit is present")
        void parsesNumericState() {
            EntityState plainNumber = createEntityState("custom.device", "23.5");
            EntityState measuredNumber = createEntityState("custom.device", "23.5",
                    Map.of("unit_of_measurement", "°C"));

            assertThat(entity.parseState(plainNumber, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new DecimalType("23.5"))));
            assertThat(entity.parseState(measuredNumber, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(QuantityType.valueOf("23.5 °C"))));
        }

        @Test
        @DisplayName("parses ISO timestamp primary state to DateTimeType when device_class is timestamp")
        void parsesDateTimeState() {
            String isoTimestamp = "2026-09-24T12:00:00+00:00";
            EntityState state = createEntityState("custom.device", isoTimestamp, Map.of("device_class", "timestamp"));

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(DateTimeType.valueOf(isoTimestamp))));
        }

        @Test
        @DisplayName("parses arbitrary string primary state to StringType")
        void parsesStringState() {
            EntityState state = createEntityState("custom.device", "home");

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType("home"))));
        }

        @Test
        @DisplayName("parses attribute values to corresponding openHAB state types")
        void parsesAttributeTypes() {
            EntityState state = createEntityState("custom.device", "active", Map.of("is_connected", true,
                    "signal_strength", -65, "temperature", 21.5, "status_message", "All systems normal"));

            Map<String, ParsedData> parsed = entity.parseState(state, context);

            assertThat(parsed.get("is_connected"), is(new ParsedData.StateData(OnOffType.ON)));
            assertThat(parsed.get("signal_strength"), is(new ParsedData.StateData(new DecimalType(-65))));
            assertThat(parsed.get("temperature"), is(new ParsedData.StateData(new DecimalType("21.5"))));
            assertThat(parsed.get("status_message"),
                    is(new ParsedData.StateData(new StringType("All systems normal"))));
        }

        @Test
        @DisplayName("maps unavailable or unknown primary state to UNDEF")
        void handlesUnavailableState() {
            EntityState unavailable = createEntityState("custom.device", "unavailable");
            EntityState unknown = createEntityState("custom.device", "unknown");

            assertThat(entity.parseState(unavailable, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(UnDefType.UNDEF)));
            assertThat(entity.parseState(unknown, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(UnDefType.UNDEF)));
        }

        @Test
        @DisplayName("does not infer QuantityType for generic attributes even if sibling unit exists")
        void doesNotInferSiblingUnitsForGenericAttributes() {
            EntityState state = createEntityState("custom.device", "active",
                    Map.of("temperature", 21.5, "temperature_unit", "°C"));

            Map<String, ParsedData> parsed = entity.parseState(state, context);

            // Generic attributes remain raw DecimalType and StringType
            assertThat(parsed.get("temperature"), is(new ParsedData.StateData(new DecimalType("21.5"))));
            assertThat(parsed.get("temperature_unit"), is(new ParsedData.StateData(new StringType("°C"))));
        }
    }
}
