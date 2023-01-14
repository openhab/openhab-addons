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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the audio state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class AudioData {

    private static final String SOURCE_RADIO = "fmr";

    private static final String SOURCE_AUX = "aux";

    private static final String SOURCE_OFF = "off";

    @SerializedName("onoff")
    private @Nullable Boolean power;

    /**
     * Must be set to false when the audio is turned on, otherwise a light that is
     * turned on will be turned off.
     */
    @SuppressWarnings("unused")
    @SerializedName("tempy")
    private @Nullable Boolean previewLight;

    /**
     * Volume range from 0 to 25.
     */
    @SerializedName("sdvol")
    private @Nullable Integer volume;

    /**
     * Current active audio source. Can be radio, aux or off.
     */
    @SerializedName("snddv")
    private @Nullable String source;

    /**
     * Current active radio preset.
     */
    @SerializedName("sndch")
    private @Nullable String preset;

    public void disableAudio() {
        power = false;
        source = SOURCE_OFF;
    }

    public void enableRadio() {
        power = true;
        source = SOURCE_RADIO;
        previewLight = false;
    }

    public State getRadioState() {
        final Boolean power = this.power;
        if (power == null) {
            return UnDefType.NULL;
        }
        return power && SOURCE_RADIO.equals(source) ? PlayPauseType.PLAY : PlayPauseType.PAUSE;
    }

    public void enableAux() {
        power = true;
        source = SOURCE_AUX;
        previewLight = false;
    }

    public State getAuxState() {
        final Boolean power = this.power;
        if (power == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(power && SOURCE_AUX.equals(source));
    }

    public void setVolume(int percent) {
        this.volume = percent / 4;
    }

    public State getVolumeState() {
        final Integer volume = this.volume;
        if (volume == null) {
            return UnDefType.NULL;
        }
        return new PercentType(volume * 4);
    }

    public void setRadioPreset(String preset) {
        this.preset = preset;
    }

    public State getPresetState() {
        final String preset = this.preset;
        if (preset == null) {
            return UnDefType.NULL;
        }
        return new StringType(preset);
    }
}
