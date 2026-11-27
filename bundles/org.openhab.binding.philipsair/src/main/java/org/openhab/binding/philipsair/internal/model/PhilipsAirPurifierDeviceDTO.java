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
package org.openhab.binding.philipsair.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains info details of the Air Purifier thing
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Add CoAP device support
 */
@NonNullByDefault
public class PhilipsAirPurifierDeviceDTO {

    @SerializedName("device_id")
    @Expose
    private @Nullable String deviceId;
    @SerializedName("name")
    @Expose
    private @Nullable String name;
    @SerializedName("type")
    @Expose
    private @Nullable String type;
    @SerializedName("modelid")
    @Expose
    private @Nullable String modelId;
    @SerializedName("swversion")
    @Expose
    private @Nullable String softwareVersion;

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getDeviceId() {
        return deviceId;
    }

    public @Nullable String getType() {
        return type;
    }

    public @Nullable String getModelId() {
        return modelId;
    }

    public @Nullable String getSoftwareVersion() {
        return softwareVersion;
    }
}
