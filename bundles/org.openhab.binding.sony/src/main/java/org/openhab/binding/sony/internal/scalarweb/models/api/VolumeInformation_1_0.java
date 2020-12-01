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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the volume information and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class VolumeInformation_1_0 {
    /** The target of the volume */
    private @Nullable String target;

    /** The current volume */
    private @Nullable Integer volume;

    /** Whether muted or not */
    private @Nullable Boolean mute;

    /** The max volume level */
    private @Nullable Integer maxVolume;

    /** The min volume level */
    private @Nullable Integer minVolume;

    /**
     * Constructor used for deserialization only
     */
    public VolumeInformation_1_0() {
    }

    /**
     * Gets the target of the volume
     *
     * @return the target of the volume
     */
    public @Nullable String getTarget() {
        return target;
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
     * Whether the target is muted
     *
     * @return true if muted, false otherwise
     */
    public @Nullable Boolean isMute() {
        return mute;
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
        return "VolumeInformation_1_0 [target=" + target + ", volume=" + volume + ", mute=" + mute + ", maxVolume="
                + maxVolume + ", minVolume=" + minVolume + "]";
    }
}
