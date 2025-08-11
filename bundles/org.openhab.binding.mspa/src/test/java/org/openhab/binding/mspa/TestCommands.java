/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mspa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mspa.internal.MSpaCommandOptionProvider;
import org.openhab.binding.mspa.internal.handler.MSpaPool;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * {@link TestCommands} tests command creation and processing
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestCommands {

    JSONObject getRequestBody(String fileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/test/resources/" + fileName)));
            JSONObject json = new JSONObject(content);
            return json;
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
        return new JSONObject();
    }

    @Test
    void testTemperatureCommands() {
        Thing thing = new ThingImpl(THING_TYPE_POOL, new ThingUID("mspa", "pool"));
        MSpaPool pool = new MSpaPool(thing, mock(UnitProvider.class), mock(MSpaCommandOptionProvider.class));
        CallbackMock callback = new CallbackMock();
        pool.setCallback(callback);

        // Test Celsius temperature command creation
        int temperatureRequest = 37;
        QuantityType<?> qt37C = QuantityType.valueOf(temperatureRequest + " °C");
        Optional<JSONObject> commandCelsius = pool.createCommandBody(CHANNEL_WATER_TARGET_TEMPERATURE, qt37C);
        assertTrue(commandCelsius.isPresent(), "Temperature command creation");
        assertEquals(new JSONObject(String.format(COMMAND_TEMPLATE, "\"temperature_setting\":" + 74)).toString(),
                commandCelsius.get().toString(), "Temperature command content");

        // Test Fahrenheit temperature command creation
        QuantityType<?> qtFahrenheit = qt37C.toUnit(ImperialUnits.FAHRENHEIT);
        assertNotNull(qtFahrenheit, "Temperature conversion to Fahrenheit");
        Optional<JSONObject> commandFahrenheit = pool.createCommandBody(CHANNEL_WATER_TARGET_TEMPERATURE, qtFahrenheit);
        assertTrue(commandFahrenheit.isPresent(), "Temperature command creation");
        assertEquals(new JSONObject(String.format(COMMAND_TEMPLATE, "\"temperature_setting\":" + 74)).toString(),
                commandFahrenheit.get().toString(), "Temperature command content");

        // Test wrong unit
        Optional<JSONObject> wrongCommand = pool.createCommandBody(CHANNEL_WATER_TARGET_TEMPERATURE,
                QuantityType.valueOf("37 km"));
        assertTrue(wrongCommand.isEmpty(), "Temperature command creation wrong unit");

        // Test upper and lower bounds
        Optional<JSONObject> command = pool.createCommandBody(CHANNEL_WATER_TARGET_TEMPERATURE,
                QuantityType.valueOf("120 °C"));
        assertTrue(command.isPresent(), "Temperature command creation");
        assertEquals(new JSONObject(String.format(COMMAND_TEMPLATE, "\"temperature_setting\":" + 80)).toString(),
                command.get().toString(), "Temperature command content");
        command = pool.createCommandBody(CHANNEL_WATER_TARGET_TEMPERATURE, QuantityType.valueOf("5 °C"));
        assertTrue(command.isPresent(), "Temperature command creation");
        assertEquals(new JSONObject(String.format(COMMAND_TEMPLATE, "\"temperature_setting\":" + 40)).toString(),
                command.get().toString(), "Temperature command content");
    }
}
