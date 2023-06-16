/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.growatt.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.growatt.internal.GrowattBindingConstants;
import org.openhab.binding.growatt.internal.GrowattBindingConstants.UoM;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * The {@link GrowattTest} is a JUnit test suite for the Growatt binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattTest {

    private final Gson gson = new Gson();

    /**
     * load a string from a file
     */
    private String load(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    @Test
    void testCreateGrottDeviceFromJson() {
        String json = load("simple");
        GrottDevice device = gson.fromJson(json, GrottDevice.class);

        assertNotNull(device);
        GrottValues grottValues = device.getValues();
        assertNotNull(grottValues);

        Map<String, State> channelStates = new HashMap<>();
        for (Entry<String, UoM> entry : GrowattBindingConstants.CHANNEL_ID_UOM_MAP.entrySet()) {
            String channelId = entry.getKey();
            Field field;
            try {
                field = GrottValues.class.getField(channelId);
            } catch (NoSuchFieldException e) {
                fail(e.getMessage());
                continue;
            } catch (SecurityException e) {
                fail(e.getMessage());
                continue;
            }
            Object value;
            try {
                value = field.get(grottValues);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                fail(e.getMessage());
                continue;
            }
            if (value != null && (value instanceof Integer)) {
                UoM uom = entry.getValue();
                channelStates.put(channelId,
                        QuantityType.valueOf(((Integer) value).doubleValue() / uom.divisor, uom.units));
            }
        }

        assertEquals(29, channelStates.size());

        channelStates.forEach((channelId, state) -> {
            assertTrue(state instanceof QuantityType<?>);
        });

        assertEquals(QuantityType.ONE, channelStates.get("pvstatus"));
        assertEquals(QuantityType.valueOf(235.3, Units.VOLT), channelStates.get("pvgridvoltage"));
        assertEquals(QuantityType.valueOf(0.7, Units.AMPERE), channelStates.get("pvgridcurrent"));
        assertEquals(QuantityType.valueOf(146, Units.WATT), channelStates.get("pvgridpower"));
        assertEquals(QuantityType.valueOf(49.97, Units.HERTZ), channelStates.get("pvfrequency"));
        assertEquals(QuantityType.valueOf(32751939, Units.SECOND), channelStates.get("totworktime"));
        assertEquals(QuantityType.valueOf(27.3, SIUnits.CELSIUS), channelStates.get("pvtemperature"));
        assertEquals(QuantityType.valueOf(4545.3, Units.KILOWATT_HOUR), channelStates.get("epvtotal"));

        assertNull(channelStates.get("BatWatt"));
    }
}
