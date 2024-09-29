/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gardena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gardena.internal.model.DataItemDeserializer;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.api.DeviceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.SensorService;
import org.openhab.binding.gardena.internal.model.dto.api.SensorServiceDataItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for {@link DataItem} deserialization.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DataItemTest {

    private Gson gson = new GsonBuilder().registerTypeAdapter(DataItem.class, new DataItemDeserializer()).create();

    public <T> T getObjectFromJson(String filename, Class<T> clazz) throws IOException {
        try (InputStream inputStream = DataItemTest.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return Objects.requireNonNull(gson.fromJson(json, clazz));
        }
    }

    @Test
    public void sensorServiceWellformed() throws IOException {
        DataItem<?> dataItem = getObjectFromJson("SensorServiceDataItem.json", DataItem.class);
        assertInstanceOf(SensorServiceDataItem.class, dataItem);
        assertEquals("SENSOR", dataItem.type);
        SensorService attributes = ((SensorServiceDataItem) dataItem).attributes;
        assertNotNull(attributes);
        assertEquals(55, attributes.soilHumidity.value);
    }

    @Test
    public void sensorServiceNoAttributes() throws IOException {
        DataItem<?> dataItem = getObjectFromJson("SensorServiceDataItemNoAttributes.json", DataItem.class);
        assertInstanceOf(SensorServiceDataItem.class, dataItem);
        assertEquals("SENSOR", dataItem.type);
        assertNull((SensorServiceDataItem) dataItem.attributes);
    }

    @Test
    public void device() throws IOException {
        DataItem<?> dataItem = getObjectFromJson("DeviceDataItem.json", DataItem.class);
        assertInstanceOf(DeviceDataItem.class, dataItem);
        assertEquals("DEVICE", dataItem.type);
        assertNull((SensorServiceDataItem) dataItem.attributes);
    }
}
