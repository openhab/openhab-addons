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
package org.openhab.binding.yamahareceiver.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;

/**
 * Zone settings.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
@NonNullByDefault
public class YamahaZoneConfig {
    /**
     * Zone name, will be one of {@link Zone}.
     */
    private String zone = "";
    /**
     * Volume relative change factor when sending {@link org.openhab.core.library.types.IncreaseDecreaseType}
     * commands.
     */
    private float volumeRelativeChangeFactor = 0.5f; // Default: 0.5 percent
    /**
     * Minimum allowed volume in dB.
     */
    private float volumeDbMin = -80f; // -80.0 dB
    /**
     * Maximum allowed volume in dB.
     */
    private float volumeDbMax = 12f; // 12.0 dB

    public @Nullable Zone getZone() {
        return YamahaUtils.tryParseEnum(Zone.class, zone);
    }

    public String getZoneValue() {
        return zone;
    }

    public float getVolumeRelativeChangeFactor() {
        return volumeRelativeChangeFactor;
    }

    public float getVolumeDbMin() {
        return volumeDbMin;
    }

    public float getVolumeDbMax() {
        return volumeDbMax;
    }

    private float getVolumeDbRange() {
        return getVolumeDbMax() - getVolumeDbMin();
    }

    /**
     * Converts from volume percentage to volume dB.
     *
     * @param volume volume percentage
     * @return volume dB
     */
    public float getVolumeDb(float volume) {
        return volume * getVolumeDbRange() / 100.0f + getVolumeDbMin();
    }

    /**
     * Converts from volume dB to volume percentage.
     *
     * @param volumeDb volume dB
     * @return volume percentage
     */
    public float getVolumePercentage(float volumeDb) {
        float volume = (volumeDb - getVolumeDbMin()) * 100.0f / getVolumeDbRange();
        if (volume < 0 || volume > 100) {
            volume = Math.max(0, Math.min(volume, 100));
        }
        return volume;
    }
}
