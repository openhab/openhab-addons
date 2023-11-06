/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.somneo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the device data from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class DeviceData {

    @SerializedName("type")
    private @Nullable String modelId;

    @SerializedName("serial")
    private @Nullable String serial;

    public @Nullable String getModelId() {
        return modelId;
    }

    public @Nullable String getSerial() {
        return serial;
    }
}
