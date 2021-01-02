/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal.model;

import org.eclipse.jdt.annotation.*;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the Status request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class Status {

    @SerializedName("response_code")
    private @Nullable String responseCode;

    @SerializedName("power")
    private @Nullable String power;

    @SerializedName("mute")
    private @Nullable String mute;

    @SerializedName("volume")
    private int volume;

    @SerializedName("max_volume")
    private int maxVolume;

    @SerializedName("input")
    private @Nullable String input;

    @SerializedName("sound_program")
    private @Nullable String soundProgram;

    @SerializedName("sleep")
    private int sleep = 0;

    public @Nullable String getResponseCode() {
        return responseCode;
    }

    public @Nullable String getPower() {
        return power;
    }

    public @Nullable String getMute() {
        return mute;
    }

    public int getVolume() {
        return volume;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public @Nullable String getInput() {
        return input;
    }

    public @Nullable String getSoundProgram() {
        return soundProgram;
    }

    public int getSleep() {
        return sleep;
    }
}
