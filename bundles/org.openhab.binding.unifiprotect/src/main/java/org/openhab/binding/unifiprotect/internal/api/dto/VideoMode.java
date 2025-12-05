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
package org.openhab.binding.unifiprotect.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Camera video mode setting.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum VideoMode implements ApiValueEnum {
    @SerializedName("default")
    DEFAULT("default"),
    @SerializedName("highFps")
    HIGH_FPS("highFps"),
    @SerializedName("sport")
    SPORT("sport"),
    @SerializedName("slowShutter")
    SLOW_SHUTTER("slowShutter"),
    @SerializedName("lprReflex")
    LPR_REFLEX("lprReflex"),
    @SerializedName("lprNoneReflex")
    LPR_NONE_REFLEX("lprNoneReflex");

    private final String apiValue;

    VideoMode(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }
}
