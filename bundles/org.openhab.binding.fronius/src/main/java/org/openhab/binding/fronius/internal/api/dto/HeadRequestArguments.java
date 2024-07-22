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
package org.openhab.binding.fronius.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HeadRequestArguments} is responsible for storing the "RequestArguments" node from the {@link Head}.
 *
 * @author Thomas Rokohl - Initial contribution
 */
@NonNullByDefault
public class HeadRequestArguments {
    @SerializedName("DataCollection")
    private @Nullable String dataCollection;
    @SerializedName("DeviceClass")
    private @Nullable String deviceClass;
    @SerializedName("DeviceId")
    private @Nullable String deviceId;
    @SerializedName("Scope")
    private @Nullable String scope;

    public @Nullable String getDataCollection() {
        return dataCollection;
    }

    public @Nullable String getDeviceClass() {
        return deviceClass;
    }

    public @Nullable String getDeviceId() {
        return deviceId;
    }

    public @Nullable String getScope() {
        return scope;
    }
}
