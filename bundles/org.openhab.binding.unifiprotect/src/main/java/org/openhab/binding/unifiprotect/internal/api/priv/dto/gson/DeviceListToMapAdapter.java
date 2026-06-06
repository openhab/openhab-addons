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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.gson;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter that converts arrays of devices to Map<String, Device>
 * The API returns devices as arrays, but we want to access them as maps keyed by ID
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceListToMapAdapter<T extends UniFiProtectModel> extends TypeAdapter<Map<String, T>> {

    private final TypeAdapter<T> deviceAdapter;

    public DeviceListToMapAdapter(Gson gson, Class<T> deviceClass) {
        this.deviceAdapter = gson.getAdapter(deviceClass);
    }

    @Override
    public void write(JsonWriter out, Map<String, T> value) throws IOException {
        // Convert map back to array when writing
        out.beginArray();
        if (value != null) {
            for (T device : value.values()) {
                deviceAdapter.write(out, device);
            }
        }
        out.endArray();
    }

    @Override
    public Map<String, T> read(JsonReader in) throws IOException {
        Map<String, T> map = new ConcurrentHashMap<>();

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return map;
        }

        in.beginArray();
        while (in.hasNext()) {
            T device = deviceAdapter.read(in);
            if (device != null && device.id != null) {
                map.put(device.id, device);
            }
        }
        in.endArray();

        return map;
    }
}
