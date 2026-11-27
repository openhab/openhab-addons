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
package org.openhab.binding.myvaillant.internal.api;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Snapshot of a system as read in one update.
 *
 * <p>
 * The API returns the system, its zones, circuits and hot water separately as {@code configuration}, {@code state} and
 * {@code properties}. Like myPyllant's {@code System.merge_object}, they are merged into one object per index (state
 * and properties overwrite configuration). All keys are snake_case.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class SystemData {

    private final String systemId;
    private final ZoneId timeZone;
    private final @Nullable JsonObject home;
    private final JsonObject system;
    private final Map<Integer, JsonObject> zones;
    private final Map<Integer, JsonObject> circuits;
    private final Map<Integer, JsonObject> domesticHotWater;
    private final List<MyVaillantDevice> devices;
    private final @Nullable Boolean connected;
    private final @Nullable JsonArray troubleCodes;
    private final Instant timestamp = Instant.now();

    /**
     * @param home the home of the system from {@code /homes}
     * @param tli the system from {@code /systems/{id}/tli}
     * @param currentSystem devices from {@code /emf/v2/{id}/currentSystem}
     * @param rts runtime statistics, {@code null} if not fetched
     * @param mpc current power, {@code null} if not fetched
     * @param connected connection status, {@code null} if not fetched
     * @param troubleCodes diagnostic trouble codes, {@code null} if not fetched
     */
    public SystemData(String systemId, ZoneId timeZone, @Nullable JsonObject home, JsonObject tli,
            JsonObject currentSystem, @Nullable JsonObject rts, @Nullable JsonObject mpc, @Nullable Boolean connected,
            @Nullable JsonArray troubleCodes) {
        this.systemId = systemId;
        this.timeZone = timeZone;
        this.home = home;
        this.connected = connected;
        this.troubleCodes = troubleCodes;

        JsonObject configuration = JsonUtil.getObject(tli, "configuration");
        JsonObject state = JsonUtil.getObject(tli, "state");
        JsonObject properties = JsonUtil.getObject(tli, "properties");
        system = new JsonObject();
        merge(system, JsonUtil.getObject(configuration, "system"));
        merge(system, JsonUtil.getObject(state, "system"));
        merge(system, JsonUtil.getObject(properties, "system"));

        Map<Integer, JsonObject> allZones = mergeByIndex(configuration, state, properties, "zones");
        Map<Integer, JsonObject> activeZones = new LinkedHashMap<>();
        allZones.forEach((index, zone) -> {
            if (!Boolean.FALSE.equals(JsonUtil.getBoolean(zone, "is_active"))) {
                activeZones.put(index, zone);
            }
        });
        zones = Collections.unmodifiableMap(activeZones);
        circuits = Collections.unmodifiableMap(mergeByIndex(configuration, state, properties, "circuits"));
        domesticHotWater = Collections.unmodifiableMap(mergeByIndex(configuration, state, properties, "dhw"));
        devices = Collections.unmodifiableList(parseDevices(currentSystem, rts, mpc));
    }

    private static void merge(JsonObject target, @Nullable JsonObject source) {
        if (source != null) {
            source.entrySet().forEach(entry -> target.add(entry.getKey(), entry.getValue()));
        }
    }

    private static Map<Integer, JsonObject> mergeByIndex(@Nullable JsonObject configuration, @Nullable JsonObject state,
            @Nullable JsonObject properties, String key) {
        Map<Integer, JsonObject> result = new LinkedHashMap<>();
        JsonArray configured = JsonUtil.getArray(configuration, key);
        if (configured == null) {
            return result;
        }
        for (JsonElement element : configured) {
            Integer index = element.isJsonObject() ? JsonUtil.getInteger(element.getAsJsonObject(), "index") : null;
            if (index != null) {
                JsonObject merged = new JsonObject();
                merge(merged, element.getAsJsonObject());
                merge(merged, findByIndex(JsonUtil.getArray(state, key), index));
                merge(merged, findByIndex(JsonUtil.getArray(properties, key), index));
                result.put(index, merged);
            }
        }
        return result;
    }

    private static @Nullable JsonObject findByIndex(@Nullable JsonArray array, int index) {
        if (array != null) {
            for (JsonElement element : array) {
                if (element.isJsonObject()
                        && Integer.valueOf(index).equals(JsonUtil.getInteger(element.getAsJsonObject(), "index"))) {
                    return element.getAsJsonObject();
                }
            }
        }
        return null;
    }

    private static List<MyVaillantDevice> parseDevices(JsonObject currentSystem, @Nullable JsonObject rts,
            @Nullable JsonObject mpc) {
        List<MyVaillantDevice> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : currentSystem.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                addDevice(result, entry.getKey(), value.getAsJsonObject(), rts, mpc);
            } else if (value.isJsonArray() && "secondary_heat_generators".equals(entry.getKey())) {
                for (JsonElement secondary : value.getAsJsonArray()) {
                    if (secondary.isJsonObject()) {
                        addDevice(result, entry.getKey(), secondary.getAsJsonObject(), rts, mpc);
                    }
                }
            }
        }
        return result;
    }

    private static void addDevice(List<MyVaillantDevice> devices, String type, JsonObject json,
            @Nullable JsonObject rts, @Nullable JsonObject mpc) {
        String deviceUuid = JsonUtil.getString(json, "device_uuid");
        if (deviceUuid != null) {
            devices.add(new MyVaillantDevice(type, deviceUuid, json,
                    findByDeviceId(JsonUtil.getArray(rts, "statistics"), deviceUuid),
                    findByDeviceId(JsonUtil.getArray(mpc, "devices"), deviceUuid)));
        }
    }

    private static @Nullable JsonObject findByDeviceId(@Nullable JsonArray array, String deviceUuid) {
        if (array != null) {
            for (JsonElement element : array) {
                if (element.isJsonObject()
                        && deviceUuid.equals(JsonUtil.getString(element.getAsJsonObject(), "device_id"))) {
                    return element.getAsJsonObject();
                }
            }
        }
        return null;
    }

    public String getSystemId() {
        return systemId;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public @Nullable JsonObject getHome() {
        return home;
    }

    /** Merged configuration, state and properties of the system itself */
    public JsonObject getSystem() {
        return system;
    }

    /** Active zones by index */
    public Map<Integer, JsonObject> getZones() {
        return zones;
    }

    public Map<Integer, JsonObject> getCircuits() {
        return circuits;
    }

    public Map<Integer, JsonObject> getDomesticHotWater() {
        return domesticHotWater;
    }

    public List<MyVaillantDevice> getDevices() {
        return devices;
    }

    public @Nullable MyVaillantDevice getDevice(String deviceUuid) {
        return devices.stream().filter(device -> device.deviceUuid().equals(deviceUuid)).findFirst().orElse(null);
    }

    public @Nullable Boolean getConnected() {
        return connected;
    }

    public @Nullable JsonArray getTroubleCodes() {
        return troubleCodes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /** Name of the home, e.g. "Zuhause", falls back to the product name of the gateway */
    public String getHomeName() {
        String name = JsonUtil.getString(home, "home_name");
        if (name == null || name.isBlank()) {
            name = JsonUtil.getString(home, "nomenclature");
        }
        return name == null || name.isBlank() ? systemId : name;
    }
}
