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
package org.openhab.binding.verisure.internal.dto;

import java.util.ArrayList;
import java.util.List;

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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localActive = active;
            result = prime * result + ((localActive == null) ? 0 : localActive.hashCode());
            result = prime * result + availableVoiceLevels.hashCode();
            result = prime * result + availableVolumes.hashCode();
            String localVoiceLevel = voiceLevel;
            result = prime * result + ((localVoiceLevel == null) ? 0 : localVoiceLevel.hashCode());
            String localVolume = volume;
            result = prime * result + ((localVolume == null) ? 0 : localVolume.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DoorLockVolumeSettings other = (DoorLockVolumeSettings) obj;
            String localActive = active;
            if (localActive == null) {
                if (other.active != null) {
                    return false;
                }
            } else if (!localActive.equals(other.active)) {
                return false;
            }
            if (!availableVoiceLevels.equals(other.availableVoiceLevels)) {
                return false;
            }
            if (!availableVolumes.equals(other.availableVolumes)) {
                return false;
            }
            String localVoiceLevel = voiceLevel;
            if (localVoiceLevel == null) {
                if (other.voiceLevel != null) {
                    return false;
                }
            } else if (!localVoiceLevel.equals(other.voiceLevel)) {
                return false;
            }
            String localVolume = volume;
            if (localVolume == null) {
                if (other.volume != null) {
                    return false;
                }
            } else if (!localVolume.equals(other.volume)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DoorLockVolumeSettings [volume=" + volume + ", voiceLevel=" + voiceLevel + ", active=" + active
                    + ", availableVolumes=" + availableVolumes + ", availableVoiceLevels=" + availableVoiceLevels + "]";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (autoRelockEnabled ? 1231 : 1237);
        String localDeviceLabel = deviceLabel;
        result = prime * result + ((localDeviceLabel == null) ? 0 : localDeviceLabel.hashCode());
        result = prime * result + doorLockVolumeSettings.hashCode();
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureSmartLockDTO other = (VerisureSmartLockDTO) obj;
        if (autoRelockEnabled != other.autoRelockEnabled) {
            return false;
        }
        String localDeviceLabel = deviceLabel;
        if (localDeviceLabel == null) {
            if (other.deviceLabel != null) {
                return false;
            }
        } else if (!localDeviceLabel.equals(other.deviceLabel)) {
            return false;
        }
        if (!doorLockVolumeSettings.equals(other.doorLockVolumeSettings)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VerisureSmartLockDTO [autoRelockEnabled=" + autoRelockEnabled + ", deviceLabel=" + deviceLabel
                + ", doorLockVolumeSettings=" + doorLockVolumeSettings + "]";
    }
}
