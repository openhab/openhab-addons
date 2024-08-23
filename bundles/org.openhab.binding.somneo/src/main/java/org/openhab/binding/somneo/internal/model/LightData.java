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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the light state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class LightData {

    /**
     * Brightness range from 0 to 25.
     */
    @SerializedName("ltlvl")
    private @Nullable Integer mainLightLevel;

    @SerializedName("onoff")
    private @Nullable Boolean mainLight;

    @SuppressWarnings("unused")
    @SerializedName("tempy")
    private @Nullable Boolean previewLight;

    @SerializedName("ngtlt")
    private @Nullable Boolean nightLight;

    public int getMainLightLevel() {
        final Integer mainLightLevel = this.mainLightLevel;
        if (mainLightLevel == null) {
            return 0;
        }
        return mainLightLevel * 4;
    }

    public void setMainLightLevel(int mainLightLevel) {
        this.mainLightLevel = mainLightLevel / 4;
    }

    public State getMainLightState() {
        final Boolean mainLight = this.mainLight;
        final Integer mainLightLevel = this.mainLightLevel;
        if (mainLight == null) {
            return UnDefType.NULL;
        }
        if (mainLightLevel == null) {
            return UnDefType.NULL;
        }
        if (mainLight) {
            return new PercentType(mainLightLevel * 4);
        }
        return OnOffType.OFF;
    }

    public void setMainLight(boolean mainLight) {
        this.mainLight = mainLight;
    }

    public void setPreviewLight(boolean previewLight) {
        this.previewLight = previewLight;
    }

    public State getNightLightState() {
        final Boolean nightLight = this.nightLight;
        if (nightLight == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(nightLight);
    }

    public void setNightLight(@Nullable Boolean nightLight) {
        this.nightLight = nightLight;
    }
}
