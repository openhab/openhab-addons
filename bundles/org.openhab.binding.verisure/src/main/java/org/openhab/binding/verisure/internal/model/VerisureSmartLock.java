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
package org.openhab.binding.verisure.internal.model;

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
public class VerisureSmartLock {

    private boolean autoRelockEnabled;
    private @Nullable String deviceLabel;
    private DoorLockVolumeSettings doorLockVolumeSettings = new DoorLockVolumeSettings();

    public boolean getAutoRelockEnabled() {
        return autoRelockEnabled;
    }

    public void setAutoRelockEnabled(boolean autoRelockEnabled) {
        this.autoRelockEnabled = autoRelockEnabled;
    }

    /**
     * @return the label
     */
    public @Nullable String getDeviceLabel() {
        return deviceLabel;
    }

    /**
     * @param label the label to set
     */
    public void setDeviceLabel(String deviceLabel) {
        this.deviceLabel = deviceLabel;
    }

    /**
     * @return the doorLockVolumeSettings
     */
    public DoorLockVolumeSettings getDoorLockVolumeSettings() {
        return doorLockVolumeSettings;
    }

    /**
     * @param doorLockVolumeSettings to set
     */
    public void setDoorLockVolumeSettings(DoorLockVolumeSettings doorLockVolumeSettings) {
        this.doorLockVolumeSettings = doorLockVolumeSettings;
    }

    @NonNullByDefault
    public static class DoorLockVolumeSettings {
        private @Nullable String volume;
        private @Nullable String voiceLevel;
        private @Nullable String active;
        private List<String> availableVolumes = new ArrayList<>();
        private List<String> availableVoiceLevels = new ArrayList<>();

        public @Nullable String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public @Nullable String getVoiceLevel() {
            return voiceLevel;
        }

        public void setVoiceLevel(String voiceLevel) {
            this.voiceLevel = voiceLevel;
        }

        public @Nullable String getActive() {
            return active;
        }

        public void setActive(String active) {
            this.active = active;
        }

        public List<String> getAvailableVolumes() {
            return availableVolumes;
        }

        public void setAvailableVolumes(List<String> availableVolumes) {
            this.availableVolumes = availableVolumes;
        }

        public List<String> getAvailableVoiceLevels() {
            return availableVoiceLevels;
        }

        public void setAvailableVoiceLevels(List<String> availableVoiceLevels) {
            this.availableVoiceLevels = availableVoiceLevels;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((active == null) ? 0 : active.hashCode());
            result = prime * result + availableVoiceLevels.hashCode();
            result = prime * result + availableVolumes.hashCode();
            result = prime * result + ((voiceLevel == null) ? 0 : voiceLevel.hashCode());
            result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
            if (!(obj instanceof DoorLockVolumeSettings)) {
                return false;
            }
            DoorLockVolumeSettings other = (DoorLockVolumeSettings) obj;
            if (active == null) {
                if (other.active != null) {
                    return false;
                }
            } else if (active != null && !active.equals(other.active)) {
                return false;
            }
            if (!availableVoiceLevels.equals(other.availableVoiceLevels)) {
                return false;
            }
            if (!availableVolumes.equals(other.availableVolumes)) {
                return false;
            }
            if (voiceLevel == null) {
                if (other.voiceLevel != null) {
                    return false;
                }
            } else if (voiceLevel != null && !voiceLevel.equals(other.voiceLevel)) {
                return false;
            }
            if (volume == null) {
                if (other.volume != null) {
                    return false;
                }
            } else if (volume != null && !volume.equals(other.volume)) {
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
        int result = super.hashCode();
        result = prime * result + ((deviceLabel == null) ? 0 : deviceLabel.hashCode());
        result = prime * result + doorLockVolumeSettings.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VerisureSmartLock)) {
            return false;
        }
        VerisureSmartLock other = (VerisureSmartLock) obj;
        if (deviceLabel == null) {
            if (other.deviceLabel != null) {
                return false;
            }
        } else if (deviceLabel != null && !deviceLabel.equals(other.deviceLabel)) {
            return false;
        }
        if (!doorLockVolumeSettings.equals(other.doorLockVolumeSettings)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureSmartLockJSON [");
        builder.append("autoRelockEnabled=");
        builder.append(autoRelockEnabled);
        builder.append(", ");
        if (deviceLabel != null) {
            builder.append("deviceLabel=");
            builder.append(deviceLabel);
            builder.append(", ");
        }
        if (doorLockVolumeSettings != null) {
            builder.append(doorLockVolumeSettings.toString());
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }

}
