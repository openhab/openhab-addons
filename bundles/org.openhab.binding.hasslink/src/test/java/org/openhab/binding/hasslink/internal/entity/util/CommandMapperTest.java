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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;

/**
 * Tests for {@link CommandMapper}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class CommandMapperTest {

    @Test
    void testOnDecimalOrQuantityWithDecimal() {
        DecimalType decimal = new DecimalType(22.5);
        Optional<ServiceCall> result = CommandMapper.onDecimalOrQuantity(decimal, "°C", "climate", "set_temperature",
                "temperature", "climate.living_room");

        assertTrue(result.isPresent());
        assertEquals("climate", result.get().domain());
        assertEquals("set_temperature", result.get().service());
        assertEquals(22.5, (Double) result.get().serviceData().get("temperature"), 0.001);
    }

    @Test
    void testOnDecimalOrQuantityWithQuantityType() {
        QuantityType<?> quantity = new QuantityType<>("72 °F");
        Optional<ServiceCall> result = CommandMapper.onDecimalOrQuantity(quantity, "°C", "climate", "set_temperature",
                "temperature", "climate.living_room");

        assertTrue(result.isPresent());
        // 72 °F converted to °C is approximately 22.222 °C
        Double targetTemp = (Double) result.get().serviceData().get("temperature");
        assertEquals(22.222, targetTemp, 0.01);
    }

    @Test
    void testOnDecimalOrQuantityWithIncompatibleUnitAborts() {
        QuantityType<?> quantity = new QuantityType<>("100 W");
        Optional<ServiceCall> result = CommandMapper.onDecimalOrQuantity(quantity, "°C", "climate", "set_temperature",
                "temperature", "climate.living_room");

        // Power cannot convert to Temperature -> should abort cleanly
        assertTrue(result.isEmpty());
    }

    @Test
    void testOnOnOffBoolean() {
        Optional<ServiceCall> onResult = CommandMapper.onOffBoolean(OnOffType.ON, "fan", "oscillate", "oscillating",
                "fan.bedroom");

        assertTrue(onResult.isPresent());
        assertEquals(true, onResult.get().serviceData().get("oscillating"));

        Optional<ServiceCall> offResult = CommandMapper.onOffBoolean(OnOffType.OFF, "fan", "oscillate", "oscillating",
                "fan.bedroom");

        assertTrue(offResult.isPresent());
        assertEquals(false, offResult.get().serviceData().get("oscillating"));
    }

    @Test
    void testOnPercent() {
        PercentType percent = new PercentType(75);
        Optional<ServiceCall> result = CommandMapper.onPercent(percent, "fan", "set_percentage", "percentage",
                "fan.bedroom");

        assertTrue(result.isPresent());
        assertEquals(75, result.get().serviceData().get("percentage"));
    }
}
