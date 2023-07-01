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
package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    public int getLightLevel() {
        return lightLevel;
    }

    public boolean isLightLevelValid() {
        return lightLevelValid;
    }

    /**
     * Raw sensor light level is '10000 * log10(lux) + 1' so apply the inverse formula to convert to Lux.
     *
     * @return a QuantityType with light level in Lux, or UNDEF.
     */
    public State getLightLevelState() {
        if (lightLevelValid) {
            double rawLightLevel = lightLevel;
            if (rawLightLevel > 1f) {
                return new QuantityType<>(Math.pow(10f, (rawLightLevel - 1f) / 10000f), Units.LUX);
            }
        }
        return UnDefType.UNDEF;
    }

    public State isLightLevelValidState() {
        return OnOffType.from(lightLevelValid);
    }
}
