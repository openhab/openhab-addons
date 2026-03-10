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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.Archetype;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 product data.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ProductData {
    private @SerializedName("model_id") @NonNullByDefault({}) String modelId;
    private @SerializedName("manufacturer_name") @NonNullByDefault({}) String manufacturerName;
    private @SerializedName("product_name") @NonNullByDefault({}) String productName;
    private @SerializedName("product_archetype") @NonNullByDefault({}) String productArchetype;
    private @Nullable Boolean certified;
    private @SerializedName("software_version") @NonNullByDefault({}) String softwareVersion;
    private @SerializedName("hardware_platform_type") @Nullable String hardwarePlatformType;

    public String getModelId() {
        return modelId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public Archetype getProductArchetype() {
        return Archetype.of(productArchetype);
    }

    public Boolean getCertified() {
        return certified != null ? certified : false;
    }

    public String getSoftwareVersion() {
        return softwareVersion != null ? softwareVersion : "";
    }

    public @Nullable String getHardwarePlatformType() {
        return hardwarePlatformType;
    }
}
