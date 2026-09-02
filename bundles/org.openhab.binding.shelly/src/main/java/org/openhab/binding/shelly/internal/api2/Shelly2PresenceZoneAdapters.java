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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_PRESENCE_ZONE_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2SettingsPresence;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2StatusPresence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The Presence reports each detection zone as a dynamic {@code presencezone:<id>} key rather than as a list, which
 * Gson can't map to a field. These adapters collect those keys into a list on the surrounding config/status result.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2PresenceZoneAdapters {

    public static class ConfigZoneFactory extends PresenceZoneFactory<Shelly2GetConfigResult, Shelly2SettingsPresence> {
        public ConfigZoneFactory() {
            super(Shelly2GetConfigResult.class, Shelly2SettingsPresence.class);
        }

        @Override
        protected void assignZones(Shelly2GetConfigResult result, ArrayList<Shelly2SettingsPresence> zones) {
            result.presence = zones;
        }
    }

    public static class StatusZoneFactory
            extends PresenceZoneFactory<Shelly2DeviceStatusResult, Shelly2StatusPresence> {
        public StatusZoneFactory() {
            super(Shelly2DeviceStatusResult.class, Shelly2StatusPresence.class);
        }

        @Override
        protected void initZone(Shelly2StatusPresence zone, String zoneKey) {
            if (zone.id == null) {
                zone.id = zoneIdFromKey(zoneKey);
            }
        }

        @Override
        protected void assignZones(Shelly2DeviceStatusResult result, ArrayList<Shelly2StatusPresence> zones) {
            result.presence = zones;
        }
    }

    public static @Nullable Integer zoneIdFromKey(String zoneKey) {
        int colon = zoneKey.indexOf(':');
        if (colon >= 0) {
            try {
                return Integer.parseInt(zoneKey.substring(colon + 1));
            } catch (NumberFormatException e) {
                // malformed key, treated as unknown zone
            }
        }
        return null;
    }

    protected abstract static class PresenceZoneFactory<R, Z> implements TypeAdapterFactory {
        private final Class<R> resultType;
        private final Class<Z> zoneType;

        protected PresenceZoneFactory(Class<R> resultType, Class<Z> zoneType) {
            this.resultType = resultType;
            this.zoneType = zoneType;
        }

        protected abstract void assignZones(R result, ArrayList<Z> zones);

        protected void initZone(Z zone, String zoneKey) {
        }

        @Override
        @NonNullByDefault({})
        public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!resultType.equals(type.getRawType())) {
                return null;
            }
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                @Override
                public @Nullable T read(JsonReader in) throws IOException {
                    JsonElement je = JsonParser.parseReader(in);
                    @Nullable
                    T result = delegate.fromJsonTree(je != null ? je : new JsonObject());
                    if (je == null || !je.isJsonObject() || !resultType.isInstance(result)) {
                        return result;
                    }
                    ArrayList<Z> zones = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : je.getAsJsonObject().entrySet()) {
                        String zoneKey = entry.getKey();
                        JsonElement value = entry.getValue();
                        if (!zoneKey.startsWith(SHELLY2_PRESENCE_ZONE_PREFIX) || value.isJsonNull()) {
                            continue;
                        }
                        Z zone = gson.fromJson(value, zoneType);
                        if (zone != null) {
                            initZone(zone, zoneKey);
                            zones.add(zone);
                        }
                    }
                    if (!zones.isEmpty()) {
                        assignZones(resultType.cast(result), zones);
                    }
                    return result;
                }
            };
        }
    }
}
