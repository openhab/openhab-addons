/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The SmartLock state of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLockJSON extends VerisureAlarmJSON {

    private @Nullable Boolean autoRelockEnabled;
    private @Nullable String deviceLabel;
    private @Nullable DoorLockVolumeSettings doorLockVolumeSettings;

    public @Nullable Boolean getAutoRelockEnabled() {
        return autoRelockEnabled;
    }

    public void setAutoRelockEnabled(Boolean autoRelockEnabled) {
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
    public @Nullable DoorLockVolumeSettings getDoorLockVolumeSettings() {
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
        @SerializedName("volume")
        private @Nullable String volume;

        @SerializedName("voiceLevel")
        private @Nullable String voiceLevel;

        @SerializedName("active")
        private @Nullable String active;

        @SerializedName("availableVolumes")
        private @Nullable List<String> availableVolumes;

        @SerializedName("availableVoiceLevels")
        private @Nullable List<String> availableVoiceLevels;

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

        public @Nullable List<String> getAvailableVolumes() {
            return availableVolumes;
        }

        public void setAvailableVolumes(List<String> availableVolumes) {
            this.availableVolumes = availableVolumes;
        }

        public @Nullable List<String> getAvailableVoiceLevels() {
            return availableVoiceLevels;
        }

        public void setAvailableVoiceLevels(List<String> availableVoiceLevels) {
            this.availableVoiceLevels = availableVoiceLevels;
        }

        @SuppressWarnings("null")
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((active == null) ? 0 : active.hashCode());
            result = prime * result + ((availableVoiceLevels == null) ? 0 : availableVoiceLevels.hashCode());
            result = prime * result + ((availableVolumes == null) ? 0 : availableVolumes.hashCode());
            result = prime * result + ((voiceLevel == null) ? 0 : voiceLevel.hashCode());
            result = prime * result + ((volume == null) ? 0 : volume.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
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
            if (availableVoiceLevels == null) {
                if (other.availableVoiceLevels != null) {
                    return false;
                }
            } else if (availableVoiceLevels != null && !availableVoiceLevels.equals(other.availableVoiceLevels)) {
                return false;
            }
            if (availableVolumes == null) {
                if (other.availableVolumes != null) {
                    return false;
                }
            } else if (availableVolumes != null && !availableVolumes.equals(other.availableVolumes)) {
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

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "DoorLockVolumeSettings [volume=" + volume + ", voiceLevel=" + voiceLevel + ", active=" + active
                    + ", availableVolumes=" + availableVolumes + ", availableVoiceLevels=" + availableVoiceLevels + "]";
        }
    }

    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((autoRelockEnabled == null) ? 0 : autoRelockEnabled.hashCode());
        result = prime * result + ((deviceLabel == null) ? 0 : deviceLabel.hashCode());
        result = prime * result + ((doorLockVolumeSettings == null) ? 0 : doorLockVolumeSettings.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VerisureSmartLockJSON)) {
            return false;
        }
        VerisureSmartLockJSON other = (VerisureSmartLockJSON) obj;
        if (autoRelockEnabled == null) {
            if (other.autoRelockEnabled != null) {
                return false;
            }
        } else if (autoRelockEnabled != null && !autoRelockEnabled.equals(other.autoRelockEnabled)) {
            return false;
        }
        if (deviceLabel == null) {
            if (other.deviceLabel != null) {
                return false;
            }
        } else if (deviceLabel != null && !deviceLabel.equals(other.deviceLabel)) {
            return false;
        }
        if (doorLockVolumeSettings == null) {
            if (other.doorLockVolumeSettings != null) {
                return false;
            }
        } else if (doorLockVolumeSettings != null && !doorLockVolumeSettings.equals(other.doorLockVolumeSettings)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureSmartLockJSON [");
        if (autoRelockEnabled != null) {
            builder.append("autoRelockEnabled=");
            builder.append(autoRelockEnabled);
            builder.append(", ");
        }
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
