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
package org.openhab.binding.hasslink.internal.entity.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.measure.Quantity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.JsonPrimitive;

/**
 * Unit tests for {@link TypeInferrer} dimension resolution and fallback inference.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
class TypeInferrerTest {

    // =========================================================================
    // 1. Tests for getDimensionFromAttributeName (Suffix Normalization)
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
            // Temperature aliases
            "target_temp, Temperature", //
            "current_temperature, Temperature", //
            "temp, Temperature", //
            "temperature, Temperature", //

            // Humidity & Moisture
            "soil_moisture, Dimensionless", //
            "relative_humidity, Dimensionless", //

            // Distance & Length synonyms
            "cut_length, Length", //
            "door_width, Length", //
            "wall_height, Length", //
            "pool_depth, Length", //
            "pipe_radius, Length", //
            "sensor_altitude, Length", //

            // Angle synonyms
            "sun_azimuth, Angle", //
            "sun_elevation, Angle", //
            "device_latitude, Angle", //
            "device_longitude, Angle", //

            // Dimensionless aliases
            "cover_position, Dimensionless", //
            "screen_brightness, Dimensionless", //
            "wifi_rssi, Dimensionless", //
            "battery_percent, Dimensionless", //
            "growth_percentage, Dimensionless", //

            // Mass synonyms
            "item_mass, Mass", //
            "total_weight, Mass", //

            // Speed synonyms
            "wind_velocity, Speed", //
            "vehicle_speed, Speed", //

            // Electric & Power
            "input_voltage, ElectricPotential", //
            "phase_current, ElectricCurrent", //
            "active_power, Power", //
            "total_energy, Energy" //
    })
    void testGetDimensionFromAttributeNameNormalization(String attributeName, String expectedDimension) {
        String dimension = TypeInferrer.getDimensionFromAttributeName(attributeName);

        assertEquals(expectedDimension, dimension, "Failed resolving dimension for attribute name: " + attributeName);

        // Verify that the resolved dimension string is a valid openHAB Quantity class
        Class<? extends Quantity<?>> quantityClass = UnitUtils.parseDimension(dimension);
        assertNotNull(quantityClass,
                "Dimension '" + dimension + "' for attribute '" + attributeName + "' is not registered in UnitUtils");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "custom_status", "unknown_attribute", "vendor_mode" })
    void testGetDimensionFromAttributeNameReturnsNullForUnmapped(String attributeName) {
        assertNull(TypeInferrer.getDimensionFromAttributeName(attributeName));
    }

    @Test
    void testGetDimensionFromAttributeNameNullSafety() {
        assertNull(TypeInferrer.getDimensionFromAttributeName(null));
    }

    // =========================================================================
    // 2. Tests for Secondary Attribute Fallback Pipeline (inferAttributeItemType)
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = { "last_triggered", "updated_at", "scheduled_time", "expiration_date", "start_timestamp" })
    void testInferAttributeItemTypeDateTimeFallback(String attributeName) {
        EntityState mockState = mock(EntityState.class);
        ItemType type = TypeInferrer.inferAttributeItemType(mockState, attributeName);

        assertEquals(ItemType.DATETIME, type);
    }

    @ParameterizedTest
    @ValueSource(strings = { "is_active", "has_battery", "can_reach", "sensor_enabled", "light_on",
            "feature_supported" })
    void testInferAttributeItemTypeBooleanPatternFallback(String attributeName) {
        EntityState mockState = mock(EntityState.class);
        ItemType type = TypeInferrer.inferAttributeItemType(mockState, attributeName);

        assertEquals(ItemType.SWITCH, type);
    }

    @Test
    void testInferAttributeItemTypeBatteryPercentage() {
        EntityState mockState = mock(EntityState.class);
        ItemType type = TypeInferrer.inferAttributeItemType(mockState, "battery_percentage");

        assertEquals(ItemType.number("Dimensionless"), type);
    }

    @ParameterizedTest
    @ValueSource(strings = { "error_count", "total_amount", "quality_index" })
    void testInferAttributeItemTypeNumericPatternFallback(String attributeName) {
        EntityState mockState = mock(EntityState.class);
        ItemType type = TypeInferrer.inferAttributeItemType(mockState, attributeName);

        assertEquals(ItemType.NUMBER, type);
    }

    @Test
    void testInferAttributeItemTypeJsonPrimitiveFallbackNumber() {
        EntityState mockState = mock(EntityState.class);
        when(mockState.getAttribute("custom_numeric_attr")).thenReturn(new JsonPrimitive(42.5));

        ItemType type = TypeInferrer.inferAttributeItemType(mockState, "custom_numeric_attr");

        assertEquals(ItemType.NUMBER, type);
    }

    @Test
    void testInferAttributeItemTypeJsonPrimitiveFallbackBoolean() {
        EntityState mockState = mock(EntityState.class);
        when(mockState.getAttribute("custom_flag")).thenReturn(new JsonPrimitive(true));

        ItemType type = TypeInferrer.inferAttributeItemType(mockState, "custom_flag");

        assertEquals(ItemType.SWITCH, type);
    }

    @Test
    void testInferAttributeItemTypeStringFallback() {
        EntityState mockState = mock(EntityState.class);
        when(mockState.getAttribute("vendor_mode")).thenReturn(new JsonPrimitive("eco_mode"));

        ItemType type = TypeInferrer.inferAttributeItemType(mockState, "vendor_mode");

        assertEquals(ItemType.STRING, type);
    }

    // =========================================================================
    // 3. Tests for Master Dispatcher (inferItemType)
    // =========================================================================

    @Test
    void testInferItemTypeDelegatesToPrimaryForState() {
        EntityState mockState = mock(EntityState.class);
        when(mockState.getDomain()).thenReturn("sensor");
        when(mockState.getAttributeAsString("device_class")).thenReturn("temperature");

        // The primary channel uses the empty attribute identifier.
        ItemType type = TypeInferrer.inferItemType(mockState, EntityType.PRIMARY_ATTR);

        assertEquals(ItemType.number("Temperature"), type);
    }

    @Test
    void testInferItemTypeDelegatesToAttributeForSecondaryKey() {
        EntityState mockState = mock(EntityState.class);

        ItemType type = TypeInferrer.inferItemType(mockState, "target_temperature");

        assertEquals(ItemType.number("Temperature"), type);
    }
}
