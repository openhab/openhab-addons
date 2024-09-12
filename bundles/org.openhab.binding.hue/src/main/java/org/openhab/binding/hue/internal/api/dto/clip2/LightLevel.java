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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 light level sensor.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class LightLevel {
    private @SerializedName("light_level") int lightLevel;
    private @SerializedName("light_level_valid") boolean lightLevelValid;
    private @Nullable @SerializedName("light_level_report") LightLevelReport lightLevelReport;

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Moved to light_level_report/light_level
     * Should be used only as fallback for older firmwares.
     */
    public int getLightLevel() {
        return lightLevel;
    }

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Indication whether the value presented in light_level is valid
     * Should be used only as fallback for older firmwares.
     */
    public boolean isLightLevelValid() {
        return lightLevelValid;
    }

    /**
     * Raw sensor light level formula is '10000 * log10(lux + 1)' so apply the inverse formula to convert back to Lux.
     * NOTE: the Philips/Signify API documentation quotes the formula as '10000 * log10(lux) + 1', however this code
     * author thinks that that formula is wrong since zero Lux would cause a log10(0) negative infinity overflow!
     *
     * @return a QuantityType with light level in Lux, or UNDEF.
     */
    public State getLightLevelState() {
        if (lightLevelValid) {
            return new QuantityType<>(Math.pow(10f, (double) lightLevel / 10000f) - 1f, Units.LUX);
        }
        return UnDefType.UNDEF;
    }

    public State isLightLevelValidState() {
        return OnOffType.from(lightLevelValid);
    }

    public @Nullable LightLevelReport getLightLevelReport() {
        return lightLevelReport;
    }
}
