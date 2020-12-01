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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the volume information and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class VolumeInformation_1_1 {
    /** The constant for turning mute ON */
    public static final String MUTEON = "on";

    /** The constant for turning mute OFF */
    public static final String MUTEOFF = "off";

    /** The output of the volume */
    private @Nullable String output;

    /** The current volume */
    private @Nullable Integer volume;

    /** Whether muted or not */
    private @Nullable String mute;

    /** The max volume level */
    private @Nullable Integer maxVolume;

    /** The min volume level */
    private @Nullable Integer minVolume;

    /**
     * Constructor used for deserialization only
     */
    public VolumeInformation_1_1() {
    }

    /**
     * Gets the output of the volume
     *
     * @return the output of the volume
     */
    public @Nullable String getOutput() {
        return output;
    }

    /**
     * Gets the current volume
     *
     * @return the current volume
     */
    public @Nullable Integer getVolume() {
        return volume;
    }

    /**
     * Get's the mute setting
     *
     * @return the mute setting
     */
    public @Nullable String getMute() {
        return mute;
    }

    /**
     * Determines if the channel is muted or not
     *
     * @return true if muted, false otherwise
     */
    public boolean isMute() {
        return StringUtils.equalsIgnoreCase(MUTEON, mute);
    }

    /**
     * Gets the maximum volume level
     *
     * @return the maximum volume level
     */
    public @Nullable Integer getMaxVolume() {
        return maxVolume;
    }

    /**
     * Gets the minimum volume level
     *
     * @return the minimum volume level
     */
    public @Nullable Integer getMinVolume() {
        return minVolume;
    }

    @Override
    public String toString() {
        return "VolumeInformation_1_1 [output=" + output + ", volume=" + volume + ", mute=" + mute + ", maxVolume="
                + maxVolume + ", minVolume=" + minVolume + "]";
    }
}
