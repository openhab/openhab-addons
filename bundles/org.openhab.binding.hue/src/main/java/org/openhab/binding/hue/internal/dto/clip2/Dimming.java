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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for dimming brightness of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Dimming {
    private @Nullable Double brightness;
    private @Nullable @SerializedName("min_dim_level") Double minimumDimmingLevel;

    public static final double DEFAULT_MINIMUM_DIMMIMG_LEVEL = 0.5f;

    /**
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public double getBrightness() throws DTOPresentButEmptyException {
        Double brightness = this.brightness;
        if (Objects.nonNull(brightness)) {
            return brightness;
        }
        throw new DTOPresentButEmptyException("'dimming' DTO is present but empty");
    }

    public @Nullable Double getMinimumDimmingLevel() {
        return minimumDimmingLevel;
    }

    public Dimming setBrightness(double brightness) {
        this.brightness = brightness;
        return this;
    }

    public Dimming setMinimumDimmingLevel(Double minimumDimmingLevel) {
        this.minimumDimmingLevel = minimumDimmingLevel;
        return this;
    }

    public @Nullable String toPropertyValue() {
        Double minimumDimmingLevel = this.minimumDimmingLevel;
        if (Objects.nonNull(minimumDimmingLevel)) {
            return String.format("%.1f %% .. 100 %%", minimumDimmingLevel.doubleValue());
        }
        return null;
    }
}
