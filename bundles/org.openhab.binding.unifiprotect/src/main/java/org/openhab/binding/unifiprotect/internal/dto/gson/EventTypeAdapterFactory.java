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
package org.openhab.binding.unifiprotect.internal.dto.gson;

import java.io.IOException;

import org.openhab.binding.unifiprotect.internal.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraMotionEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectAudioEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectLineEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectLoiterEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.LightMotionEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.RingEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorAlarmEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorBatteryLowEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorClosedEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorExtremeValueEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorMotionEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorOpenEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorTamperEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.SensorWaterLeakEvent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Polymorphic adapter for events based on type discriminator.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EventTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!BaseEvent.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        return (TypeAdapter<T>) new TypeAdapter<BaseEvent>() {
            @Override
            public void write(JsonWriter out, BaseEvent value) throws IOException {
                @SuppressWarnings({ "rawtypes", "null" })
                TypeAdapter<BaseEvent> delegate = (TypeAdapter) gson.getAdapter((Class) value.getClass());
                delegate.write(out, value);
            }

            @Override
            public BaseEvent read(JsonReader in) throws IOException {
                JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "";
                switch (type) {
                    case "ring":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this, TypeToken.get(RingEvent.class))
                                .fromJsonTree(obj);
                    case "sensorExtremeValues":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorExtremeValueEvent.class)).fromJsonTree(obj);
                    case "sensorWaterLeak":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorWaterLeakEvent.class)).fromJsonTree(obj);
                    case "sensorTamper":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorTamperEvent.class)).fromJsonTree(obj);
                    case "sensorBatteryLow":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorBatteryLowEvent.class)).fromJsonTree(obj);
                    case "sensorAlarm":
                        return gson
                                .getDelegateAdapter(EventTypeAdapterFactory.this, TypeToken.get(SensorAlarmEvent.class))
                                .fromJsonTree(obj);
                    case "sensorOpened":
                        return gson
                                .getDelegateAdapter(EventTypeAdapterFactory.this, TypeToken.get(SensorOpenEvent.class))
                                .fromJsonTree(obj);
                    case "sensorClosed":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorClosedEvent.class)).fromJsonTree(obj);
                    case "sensorMotion":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(SensorMotionEvent.class)).fromJsonTree(obj);
                    case "lightMotion":
                        return gson
                                .getDelegateAdapter(EventTypeAdapterFactory.this, TypeToken.get(LightMotionEvent.class))
                                .fromJsonTree(obj);
                    case "motion":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(CameraMotionEvent.class)).fromJsonTree(obj);
                    case "smartAudioDetect":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(CameraSmartDetectAudioEvent.class)).fromJsonTree(obj);
                    case "smartDetectZone":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(CameraSmartDetectZoneEvent.class)).fromJsonTree(obj);
                    case "smartDetectLine":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(CameraSmartDetectLineEvent.class)).fromJsonTree(obj);
                    case "smartDetectLoiterZone":
                        return gson.getDelegateAdapter(EventTypeAdapterFactory.this,
                                TypeToken.get(CameraSmartDetectLoiterEvent.class)).fromJsonTree(obj);
                    default:
                        throw new IOException("Unknown event type '" + type + "' for Event payload");
                }
            }
        };
    }
}
