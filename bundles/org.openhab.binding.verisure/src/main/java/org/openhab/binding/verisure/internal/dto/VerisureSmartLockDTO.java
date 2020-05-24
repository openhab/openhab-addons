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
package org.openhab.binding.verisure.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The SmartLock state of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLockDTO {

    private boolean autoRelockEnabled;
    private @Nullable String deviceLabel;
    private DoorLockVolumeSettings doorLockVolumeSettings = new DoorLockVolumeSettings();

    public boolean getAutoRelockEnabled() {
        return autoRelockEnabled;
    }

    public @Nullable String getDeviceLabel() {
        return deviceLabel;
    }

    public DoorLockVolumeSettings getDoorLockVolumeSettings() {
        return doorLockVolumeSettings;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureSmartLockDTO)) {
            return false;
        }
        VerisureSmartLockDTO rhs = ((VerisureSmartLockDTO) other);
        return new EqualsBuilder().append(autoRelockEnabled, rhs.autoRelockEnabled).append(deviceLabel, rhs.deviceLabel)
                .append(doorLockVolumeSettings, rhs.doorLockVolumeSettings).isEquals();
    }

    @Override
    public String toString() {
        return deviceLabel != null ? new ToStringBuilder(this).append("autoRelockEnabled", autoRelockEnabled)
                .append("deviceLabel", deviceLabel).append("doorLockVolumeSettings", doorLockVolumeSettings).toString()
                : "";
    }

    public static class DoorLockVolumeSettings {
        private @Nullable String volume;
        private @Nullable String voiceLevel;
        private @Nullable String active;
        private List<String> availableVolumes = new ArrayList<>();
        private List<String> availableVoiceLevels = new ArrayList<>();

        public @Nullable String getVolume() {
            return volume;
        }

        public @Nullable String getVoiceLevel() {
            return voiceLevel;
        }

        public @Nullable String getActive() {
            return active;
        }

        public List<String> getAvailableVolumes() {
            return availableVolumes;
        }

        public List<String> getAvailableVoiceLevels() {
            return availableVoiceLevels;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof DoorLockVolumeSettings)) {
                return false;
            }
            DoorLockVolumeSettings rhs = ((DoorLockVolumeSettings) other);
            return new EqualsBuilder().append(volume, rhs.volume).append(voiceLevel, rhs.voiceLevel)
                    .append(active, rhs.active).append(availableVoiceLevels, rhs.availableVoiceLevels)
                    .append(availableVolumes, rhs.availableVolumes).isEquals();
        }

        @Override
        public String toString() {
            return volume != null ? new ToStringBuilder(this).append("volume", volume).append("voiceLevel", voiceLevel)
                    .append("active", active).append("availableVolumes", availableVolumes)
                    .append("availableVoiceLevels", availableVoiceLevels).toString() : "";
        }
    }
}
