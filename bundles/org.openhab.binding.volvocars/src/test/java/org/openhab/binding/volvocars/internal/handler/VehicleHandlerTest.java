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
package org.openhab.binding.volvocars.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.volvocars.internal.api.dto.ValueField;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonPrimitive;

/**
 * Tests the conversion of API values to openHAB states.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VehicleHandlerTest {
    private static final Unit<?> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);

    @Test
    void reportedUnitIsUsed() {
        assertEquals(new QuantityType<>(150, ImperialUnits.MILE),
                VehicleHandler.toQuantity(field(new JsonPrimitive(150), "mi", null), KILOMETRE));
        assertEquals(new QuantityType<>(58, Units.PERCENT),
                VehicleHandler.toQuantity(field(new JsonPrimitive("58"), "percentage", null), Units.PERCENT));
        assertEquals(new QuantityType<>(250, KILOMETRE),
                VehicleHandler.toQuantity(field(new JsonPrimitive("250"), "kilometers", null), KILOMETRE));
        assertEquals(new QuantityType<>(9, Units.MONTH),
                VehicleHandler.toQuantity(field(new JsonPrimitive(9), "months", null), Units.MONTH));
        assertEquals(new QuantityType<>(270, Units.DAY),
                VehicleHandler.toQuantity(field(new JsonPrimitive(270), "days", null), Units.MONTH));
        assertEquals(new QuantityType<>(11, MetricPrefix.KILO(Units.WATT)),
                VehicleHandler.toQuantity(field(new JsonPrimitive(11), "kW", null), Units.WATT));
    }

    @Test
    void energyChannelsFollowCapabilities() {
        // capabilities unknown, e.g. because the scope was not granted
        assertTrue(VehicleHandler.isBatteryChannelSupported(CHANNEL_CHARGING_POWER, null));
        // Energy API not supported: the battery level is read from the fuel status
        assertTrue(VehicleHandler.isBatteryChannelSupported(CHANNEL_BATTERY_LEVEL, Set.of()));
        assertFalse(VehicleHandler.isBatteryChannelSupported(CHANNEL_CHARGING_STATUS, Set.of()));
        // Energy API supported
        Set<String> capabilities = Set.of("batteryChargeLevel", "chargingStatus");
        assertTrue(VehicleHandler.isBatteryChannelSupported(CHANNEL_CHARGING_STATUS, capabilities));
        assertFalse(VehicleHandler.isBatteryChannelSupported(CHANNEL_CHARGING_POWER, capabilities));
        // not provided by the Energy API
        assertTrue(VehicleHandler.isBatteryChannelSupported(CHANNEL_DISTANCE_TO_EMPTY_BATTERY, Set.of()));
    }

    @Test
    void defaultUnitIsUsedForMissingUnknownOrIncompatibleUnits() {
        assertEquals(new QuantityType<>(30000, KILOMETRE),
                VehicleHandler.toQuantity(field(new JsonPrimitive(30000), null, null), KILOMETRE));
        assertEquals(new QuantityType<>(5, KILOMETRE),
                VehicleHandler.toQuantity(field(new JsonPrimitive(5), "parsecs", null), KILOMETRE));
        assertEquals(new QuantityType<>(5, KILOMETRE),
                VehicleHandler.toQuantity(field(new JsonPrimitive(5), "minutes", null), KILOMETRE));
    }

    @Test
    void invalidValuesAreUndefined() {
        assertEquals(UnDefType.UNDEF, VehicleHandler.toQuantity(null, KILOMETRE));
        assertEquals(UnDefType.UNDEF,
                VehicleHandler.toQuantity(field(new JsonPrimitive("not a number"), "km", null), KILOMETRE));
        assertEquals(UnDefType.UNDEF, VehicleHandler.toQuantity(field(null, null, "ERROR"), KILOMETRE));
        assertEquals(UnDefType.UNDEF,
                VehicleHandler.toQuantity(field(new JsonPrimitive(53), "percentage", "ERROR"), Units.PERCENT));
    }

    private static ValueField field(@Nullable JsonPrimitive value, @Nullable String unit, @Nullable String status) {
        ValueField field = new ValueField();
        field.value = value;
        field.unit = unit;
        field.status = status;
        return field;
    }
}
