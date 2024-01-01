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
package org.openhab.binding.somneo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the relax program state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class RelaxData {

    @SerializedName("onoff")
    private @Nullable Boolean state;

    @SerializedName("progr")
    private @Nullable Integer breathingRate;

    @SerializedName("durat")
    private @Nullable Integer durationInMin;

    @SerializedName("rtype")
    private @Nullable Integer guidanceType;

    /**
     * Brightness range from 0 to 25.
     */
    @SerializedName("intny")
    private @Nullable Integer lightIntensity;

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

    public State getBreathingRate() {
        final Integer breathingRate = this.breathingRate;
        if (breathingRate == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(breathingRate);
    }

    public void setBreathingRate(int breathingRate) {
        this.breathingRate = breathingRate;
    }

    public State getDurationInMin() {
        final Integer durationInMin = this.durationInMin;
        if (durationInMin == null) {
            return UnDefType.NULL;
        }
        return new QuantityType<>(durationInMin, Units.MINUTE);
    }

    public void setDurationInMin(int durationInMin) {
        this.durationInMin = durationInMin;
    }

    public State getGuidanceType() {
        final Integer guidanceType = this.guidanceType;
        if (guidanceType == null) {
            return UnDefType.NULL;
        }
        return new DecimalType(guidanceType);
    }

    public void setGuidanceType(int guidanceType) {
        this.guidanceType = guidanceType;
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
