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
package org.openhab.binding.unifiprotect.internal.api.dto.gson;

import java.io.IOException;

import org.openhab.binding.unifiprotect.internal.api.dto.AiPort;
import org.openhab.binding.unifiprotect.internal.api.dto.AiProcessor;
import org.openhab.binding.unifiprotect.internal.api.dto.Bridge;
import org.openhab.binding.unifiprotect.internal.api.dto.Camera;
import org.openhab.binding.unifiprotect.internal.api.dto.Chime;
import org.openhab.binding.unifiprotect.internal.api.dto.Device;
import org.openhab.binding.unifiprotect.internal.api.dto.Doorlock;
import org.openhab.binding.unifiprotect.internal.api.dto.Light;
import org.openhab.binding.unifiprotect.internal.api.dto.LinkStation;
import org.openhab.binding.unifiprotect.internal.api.dto.Nvr;
import org.openhab.binding.unifiprotect.internal.api.dto.Sensor;
import org.openhab.binding.unifiprotect.internal.api.dto.Speaker;
import org.openhab.binding.unifiprotect.internal.api.dto.Viewer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter for Device based on modelKey.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Device.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        return (TypeAdapter<T>) new TypeAdapter<Device>() {
            @Override
            public void write(JsonWriter out, Device value) throws IOException {
                @SuppressWarnings({ "rawtypes", "null" })
                TypeAdapter<Device> delegate = (TypeAdapter) gson.getAdapter((Class) value.getClass());
                delegate.write(out, value);
            }

            @Override
            public Device read(JsonReader in) throws IOException {
                JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
                String modelKey = obj.has("modelKey") && obj.get("modelKey").isJsonPrimitive()
                        ? obj.get("modelKey").getAsString()
                        : null;

                if (modelKey == null) {
                    throw new IOException("Missing modelKey for Device payload");
                }
                switch (modelKey) {
                    case "camera":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Camera.class))
                                .fromJsonTree(obj);
                    case "nvr":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Nvr.class))
                                .fromJsonTree(obj);
                    case "chime":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Chime.class))
                                .fromJsonTree(obj);
                    case "light":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Light.class))
                                .fromJsonTree(obj);
                    case "viewer":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Viewer.class))
                                .fromJsonTree(obj);
                    case "speaker":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Speaker.class))
                                .fromJsonTree(obj);
                    case "bridge":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Bridge.class))
                                .fromJsonTree(obj);
                    case "doorlock":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Doorlock.class))
                                .fromJsonTree(obj);
                    case "sensor":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(Sensor.class))
                                .fromJsonTree(obj);
                    case "aiprocessor":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(AiProcessor.class))
                                .fromJsonTree(obj);
                    case "aiport":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(AiPort.class))
                                .fromJsonTree(obj);
                    case "linkstation":
                        return gson.getDelegateAdapter(DeviceTypeAdapterFactory.this, TypeToken.get(LinkStation.class))
                                .fromJsonTree(obj);
                    default:
                        throw new IOException("Unknown modelKey '" + modelKey + "' for Device payload");
                }
            }
        };
    }
}
