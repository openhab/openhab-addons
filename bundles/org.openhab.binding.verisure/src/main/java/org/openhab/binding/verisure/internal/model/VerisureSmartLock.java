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

    public @Nullable String getDeviceLabel() {
        return deviceLabel;
    }

    public DoorLockVolumeSettings getDoorLockVolumeSettings() {
        return doorLockVolumeSettings;
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
            String active = this.active;
            result = prime * result + ((active == null) ? 0 : active.hashCode());
            result = prime * result + availableVoiceLevels.hashCode();
            result = prime * result + availableVolumes.hashCode();
            String voiceLevel = this.voiceLevel;
            result = prime * result + ((voiceLevel == null) ? 0 : voiceLevel.hashCode());
            String volume = this.volume;
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
        String deviceLabel = this.deviceLabel;
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
        builder.append(doorLockVolumeSettings.toString());
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }
}
