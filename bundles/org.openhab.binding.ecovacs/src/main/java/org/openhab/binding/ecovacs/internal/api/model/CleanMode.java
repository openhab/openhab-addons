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
package org.openhab.binding.ecovacs.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public enum CleanMode {
    @SerializedName("auto")
    AUTO,
    @SerializedName("border")
    EDGE,
    @SerializedName("spot")
    SPOT,
    @SerializedName(value = "SpotArea", alternate = { "spotArea", "freeClean" })
    SPOT_AREA,
    @SerializedName(value = "CustomArea", alternate = { "customArea" })
    CUSTOM_AREA,
    @SerializedName("singleRoom")
    SINGLE_ROOM,
    @SerializedName("sceneClean")
    SCENE_CLEAN,
    @SerializedName("pause")
    PAUSE,
    @SerializedName("stop")
    STOP,
    @SerializedName(value = "going", alternate = { "goCharging" })
    RETURNING,
    @SerializedName("autoEmpty")
    EMPTYING,
    @SerializedName("washing")
    WASHING,
    @SerializedName(value = "drying", alternate = { "spin-dry" })
    DRYING,
    @SerializedName("idle")
    IDLE;

    public boolean isActive() {
        return this == AUTO || this == EDGE || this == SPOT || this == SPOT_AREA || this == CUSTOM_AREA
                || this == SINGLE_ROOM || this == SCENE_CLEAN;
    }

    public boolean isIdle() {
        return this == IDLE || this == DRYING || this == WASHING || this == EMPTYING;
    }
}
