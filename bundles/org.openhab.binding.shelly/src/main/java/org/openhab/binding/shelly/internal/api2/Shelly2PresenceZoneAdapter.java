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

import static org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.SHELLY2_PRESENCE_ZONE_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.Shelly2StatusPresence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The Presence reports each detection zone as a dynamic {@code presencezone:<id>} key rather than as a list, which
 * Gson can't map to a field. This adapter collects those keys into a list on the surrounding status result.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2PresenceZoneAdapter implements TypeAdapterFactory {

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

    @Override
    @NonNullByDefault({})
    public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Shelly2DeviceStatusResult.class.equals(type.getRawType())) {
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
                T result = delegate.fromJsonTree(je);
                if (!je.isJsonObject() || !(result instanceof Shelly2DeviceStatusResult)) {
                    return result;
                }
                ArrayList<Shelly2StatusPresence> zones = new ArrayList<>();
                for (Map.Entry<String, JsonElement> entry : je.getAsJsonObject().entrySet()) {
                    String zoneKey = entry.getKey();
                    JsonElement value = entry.getValue();
                    if (!zoneKey.startsWith(SHELLY2_PRESENCE_ZONE_PREFIX) || value.isJsonNull()) {
                        continue;
                    }
                    @Nullable
                    Shelly2StatusPresence zone = gson.fromJson(value, Shelly2StatusPresence.class);
                    if (zone != null) {
                        if (zone.id == null) {
                            zone.id = zoneIdFromKey(zoneKey);
                        }
                        zones.add(zone);
                    }
                }
                if (!zones.isEmpty()) {
                    ((Shelly2DeviceStatusResult) result).presenceZones = zones;
                }
                return result;
            }
        };
    }
}
