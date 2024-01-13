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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Vehicle {
    private @Nullable String id;
    private @Nullable String vin;
    private @Nullable String brand;
    private @Nullable String label;
    @SerializedName("engine")
    private @Nullable List<Engine> engines;
    private @Nullable ZonedDateTime createdAt;
    private @Nullable ZonedDateTime updatedAt;

    public @Nullable String getId() {
        return id;
    }

    public @Nullable String getVin() {
        return vin;
    }

    public @Nullable String getBrand() {
        return brand;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable List<Engine> getEngines() {
        return engines;
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("createdAt", createdAt).append("updatedAt", createdAt).append("id", id)
                .append("vin", vin).append("brand", brand).append("label", label).append("engines", engines).toString();
    }
}
