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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 light level sensor report.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class LightLevelReport {
    private @NonNullByDefault({}) Instant changed;
    private @SerializedName("light_level") int lightLevel;

    /**
     * @return last time the value of this property is changed.
     */
    public Instant getLastChanged() {
        return changed;
    }

    /**
     * Light level in 10000*log10(lux) +1 measured by sensor.
     * Logarithmic scale used because the human eye adjusts to light levels and small changes at low lux levels
     * are more noticeable than at high lux levels. This allows use of linear scale configuration sliders.
     *
     * @return light level in 10000*log10(lux) +1 measured by sensor
     */
    public int getLightLevel() {
        return lightLevel;
    }
}
