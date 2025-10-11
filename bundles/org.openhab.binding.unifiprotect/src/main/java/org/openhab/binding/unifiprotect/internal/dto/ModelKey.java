/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Discriminator values for model types.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum ModelKey implements ApiValueEnum {
    @SerializedName("nvr")
    NVR("nvr"),
    @SerializedName("camera")
    CAMERA("camera"),
    @SerializedName("chime")
    CHIME("chime"),
    @SerializedName("light")
    LIGHT("light"),
    @SerializedName("viewer")
    VIEWER("viewer"),
    @SerializedName("speaker")
    SPEAKER("speaker"),
    @SerializedName("bridge")
    BRIDGE("bridge"),
    @SerializedName("doorlock")
    DOORLOCK("doorlock"),
    @SerializedName("sensor")
    SENSOR("sensor"),
    @SerializedName("aiprocessor")
    AIPROCESSOR("aiprocessor"),
    @SerializedName("aiport")
    AIPORT("aiport"),
    @SerializedName("linkstation")
    LINKSTATION("linkstation"),
    @SerializedName("liveview")
    LIVEVIEW("liveview"),
    @SerializedName("event")
    EVENT("event");

    private final String apiValue;

    ModelKey(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
