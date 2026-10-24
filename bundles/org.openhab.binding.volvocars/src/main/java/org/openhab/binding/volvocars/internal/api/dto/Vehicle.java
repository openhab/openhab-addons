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
package org.openhab.binding.volvocars.internal.api.dto;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Vehicle details as returned by <code>GET /connected-vehicle/v2/vehicles/{vin}</code>.
 *
 * @author Michael Weger - Initial contribution
 */
public class Vehicle {
    private static final Set<String> BATTERY_FUEL_TYPES = Set.of("ELECTRIC", "PETROL/ELECTRIC", "NONE");
    private static final Set<String> COMBUSTION_FUEL_TYPES = Set.of("DIESEL", "PETROL", "PETROL/ELECTRIC");

    public static class Images {
        public @Nullable String exteriorImageUrl;
        public @Nullable String internalImageUrl;
    }

    public static class Descriptions {
        public @Nullable String model;
        public @Nullable String upholstery;
        public @Nullable String steering;
    }

    public @Nullable String vin;
    public int modelYear;
    public @Nullable String gearbox;
    public @Nullable String fuelType;
    public @Nullable String externalColour;
    @SerializedName("batteryCapacityKWH")
    public @Nullable Double batteryCapacityKWh;
    public @Nullable Images images;
    public @Nullable Descriptions descriptions;

    public boolean hasBattery() {
        return BATTERY_FUEL_TYPES.contains(fuelType);
    }

    public boolean hasCombustionEngine() {
        return COMBUSTION_FUEL_TYPES.contains(fuelType);
    }

    public @Nullable String getModel() {
        Descriptions localDescriptions = descriptions;
        return localDescriptions != null ? localDescriptions.model : null;
    }
}
