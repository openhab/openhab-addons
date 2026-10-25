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
package org.openhab.binding.transit.internal.net.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehiclesResult} is the response of the {@code /v4/vehicles} endpoint.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class VehiclesResult {
    private @Nullable List<Vehicle> vehicles;

    public List<Vehicle> getVehicles() {
        List<Vehicle> list = vehicles;
        return list != null ? list : Collections.emptyList();
    }

    public static class Vehicle {
        @SerializedName("vehicle_id")
        private @Nullable String vehicleId;

        private @Nullable Double latitude;
        private @Nullable Double longitude;

        // 0 = outbound, 1 = inbound
        @SerializedName("direction_id")
        private @Nullable Integer directionId;

        @SerializedName("updated_at")
        private @Nullable Long updatedAt;

        @SerializedName("vehicle_label")
        private @Nullable String vehicleLabel;

        // 1 = not crowded, 2 = some crowding, 3 = crowded
        @SerializedName("occupancy_status")
        private @Nullable Integer occupancyStatus;

        // 0 = unknown, 1 = accessible, 2 = not accessible
        @SerializedName("wheelchair_accessible")
        private @Nullable Integer wheelchairAccessible;

        @SerializedName("rt_trip_id")
        private @Nullable String rtTripId;

        public @Nullable String getVehicleId() {
            return vehicleId;
        }

        public @Nullable Double getLatitude() {
            return latitude;
        }

        public @Nullable Double getLongitude() {
            return longitude;
        }

        public @Nullable Integer getDirectionId() {
            return directionId;
        }

        public @Nullable Instant getUpdatedAt() {
            Long time = updatedAt;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable String getVehicleLabel() {
            return vehicleLabel;
        }

        public @Nullable Integer getOccupancyStatus() {
            return occupancyStatus;
        }

        public @Nullable Integer getWheelchairAccessible() {
            return wheelchairAccessible;
        }

        public @Nullable String getRtTripId() {
            return rtTripId;
        }
    }
}
