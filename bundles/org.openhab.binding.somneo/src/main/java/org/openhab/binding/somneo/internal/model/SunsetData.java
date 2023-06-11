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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the sunset program state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SunsetData {

    @SerializedName("onoff")
    private @Nullable Boolean state;

    /**
     * Brightness range from 0 to 25.
     */
    @SerializedName("curve")
    private @Nullable Integer lightIntensity;

    @SerializedName("durat")
    private @Nullable Integer durationInMin;

    @SerializedName("ctype")
    private @Nullable Integer colorSchema;

    @SerializedName("snddv")
    private @Nullable String soundSource;

    @SerializedName("sndch")
    private @Nullable String ambientNoise;

    /**
     * Volume range from 0 to 25.
     */
    @SerializedName("sndlv")
    private @Nullable Integer soundVolume;

    public State getSwitchState() {
        final Boolean state = this.state;
        if (state == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(state);
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public State getLightIntensity() {
        final Integer lightIntensity = this.lightIntensity;
        if (lightIntensity == null) {
            return UnDefType.NULL;
        }
        return new PercentType(lightIntensity * 4);
    }

    public void setLightIntensity(int percent) {
        this.lightIntensity = percent / 4;
    }

    public State getDurationInMin() {
        final Integer durationInMin = this.durationInMin;
        if (durationInMin == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(durationInMin);
    }

    public void setDurationInMin(int durationInMin) {
        this.durationInMin = durationInMin;
    }

    public State getColorSchema() {
        final Integer colorSchema = this.colorSchema;
        if (colorSchema == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(colorSchema);
    }

    public void setColorSchema(int colorSchema) {
        this.colorSchema = colorSchema;
    }

    public State getAmbientNoise() {
        final String soundSource = this.soundSource;
        if (soundSource == null) {
            return UnDefType.NULL;
        }
        final String suffix = "off".equals(soundSource) ? "" : "-" + ambientNoise;
        return new StringType(soundSource + suffix);
    }

    public void setAmbientNoise(String option) {
        final String[] values = option.split("-");
        soundSource = values[0];
        ambientNoise = values.length == 1 ? "" : values[1];
    }

    public State getSoundVolume() {
        final Integer soundVolume = this.soundVolume;
        if (soundVolume == null) {
            return UnDefType.NULL;
        }
        return new PercentType(soundVolume * 4);
    }

    public void setSoundVolume(int percent) {
        this.soundVolume = percent / 4;
    }
}
