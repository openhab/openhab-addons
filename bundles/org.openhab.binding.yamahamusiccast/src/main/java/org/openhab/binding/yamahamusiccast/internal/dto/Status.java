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
package org.openhab.binding.yamahamusiccast.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the Status request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 * @author Florian Hotze - Add volume in decibel
 */

public class Status {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("power")
    private String power;

    @SerializedName("mute")
    private String mute;

    @SerializedName("volume")
    private int volume;

    @SerializedName("actual_volume")
    private ActualVolume actualVolume;

    @SerializedName("max_volume")
    private int maxVolume = 1;

    @SerializedName("input")
    private String input;

    @SerializedName("sound_program")
    private String soundProgram;

    @SerializedName("sleep")
    private int sleep = 0;

    public String getResponseCode() {
        if (responseCode == null) {
            responseCode = "";
        }
        return responseCode;
    }

    public String getPower() {
        if (power == null) {
            power = "";
        }
        return power;
    }

    public String getMute() {
        if (mute == null) {
            mute = "";
        }
        return mute;
    }

    public int getVolume() {
        return volume;
    }

    public @Nullable ActualVolume getActualVolume() {
        return actualVolume;
    }

    public int getMaxVolume() {
        // if no value is returned, set to 1 to avoid division by zero
        if (maxVolume == 0) {
            maxVolume = 1;
        }
        return maxVolume;
    }

    public String getInput() {
        if (input == null) {
            input = "";
        }
        return input;
    }

    public String getSoundProgram() {
        if (soundProgram == null) {
            soundProgram = "";
        }
        return soundProgram;
    }

    public int getSleep() {
        return sleep;
    }
}
