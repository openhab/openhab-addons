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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A device of a system from {@code currentSystem}, e.g. the heat pump ({@code primary_heat_generator}) or the
 * electric backup heater of the hydraulic station, with its runtime statistics (RTS) and current power (MPC).
 *
 * @param type key of the device in {@code currentSystem}, e.g. {@code primary_heat_generator}
 * @param json device as returned by the API, keys in snake_case
 * @param rtsStatistics operation time (minutes) and on/off cycles, if fetched
 * @param mpc current power, if fetched
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public record MyVaillantDevice(String type, String deviceUuid, JsonObject json, @Nullable JsonObject rtsStatistics,
        @Nullable JsonObject mpc) {

    /** Name like the app shows it: the name, else the product name, else the device type */
    public String getName() {
        String name = JsonUtil.getString(json, "name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        String productName = JsonUtil.getString(json, "product_name");
        if (productName != null && !productName.isBlank()) {
            return productName.equals(productName.toLowerCase(Locale.ROOT)) ? titleCase(productName) : productName;
        }
        String deviceType = JsonUtil.getString(json, "device_type");
        return deviceType == null ? type : titleCase(deviceType.replace("_", " "));
    }

    public @Nullable String getDeviceType() {
        return JsonUtil.getString(json, "device_type");
    }

    /** The energy values (operation mode and value type) the device reports */
    public List<EnergyDataType> getEnergyDataTypes() {
        List<EnergyDataType> result = new ArrayList<>();
        JsonArray data = JsonUtil.getArray(json, "data");
        if (data != null) {
            for (JsonElement element : data) {
                if (element.isJsonObject()) {
                    String operationMode = JsonUtil.getString(element.getAsJsonObject(), "operation_mode");
                    String valueType = JsonUtil.getString(element.getAsJsonObject(), "value_type");
                    if (operationMode != null && valueType != null) {
                        result.add(new EnergyDataType(operationMode, valueType));
                    }
                }
            }
        }
        return result;
    }

    public @Nullable Integer getOperationTimeMinutes() {
        return JsonUtil.getInteger(rtsStatistics, "operation_time");
    }

    public @Nullable Integer getOnOffCycles() {
        return JsonUtil.getInteger(rtsStatistics, "on_off_cycles");
    }

    /** Current power in W from MPC, older systems report it with the RTS statistics */
    public @Nullable Double getCurrentPower() {
        Double power = JsonUtil.getDouble(mpc, "current_power");
        return power != null ? power : JsonUtil.getDouble(rtsStatistics, "current_power_consumption");
    }

    /** Water pressure in bar measured by the device, if it has a sensor */
    public @Nullable Double getWaterPressure() {
        return JsonUtil.getDouble(JsonUtil.getObject(JsonUtil.getObject(json, "operational_data"), "water_pressure"),
                "value");
    }

    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.toLowerCase(Locale.ROOT).split(" ")) {
            if (!word.isEmpty()) {
                if (!result.isEmpty()) {
                    result.append(' ');
                }
                result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        return result.toString();
    }
}
