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
package org.openhab.binding.fronius.internal.api.dto.storage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StorageDetails} is responsible for storing the "Details" node of
 * the {@link StorageController}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class StorageDetails {
    @SerializedName("Manufacturer")
    private @Nullable String manufacturer;
    @SerializedName("Model")
    private @Nullable String model;
    @SerializedName("Serial")
    private @Nullable String serial;

    public @Nullable String getManufacturer() {
        return manufacturer;
    }

    public @Nullable String getModel() {
        return model;
    }

    public @Nullable String getSerial() {
        return serial;
    }
}
