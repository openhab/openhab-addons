/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The configuration of the smart locks in a Verisure installation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLockConfigurationDTO {

    private @Nullable Data data;

    public @Nullable Configuration getConfiguration(String deviceLabel) {
        Data localData = data;
        Installation installation = localData != null ? localData.installation : null;
        List<SmartLock> smartLocks = installation != null ? installation.smartLocks : null;
        if (smartLocks != null) {
            for (SmartLock smartLock : smartLocks) {
                Device device = smartLock.device;
                if (device != null && deviceLabel.equals(device.deviceLabel)) {
                    return smartLock.configuration;
                }
            }
        }
        return null;
    }

    private static class Data {
        private @Nullable Installation installation;
    }

    private static class Installation {
        private @Nullable List<SmartLock> smartLocks;
    }

    private static class SmartLock {
        private @Nullable Device device;
        private @Nullable Configuration configuration;
    }

    private static class Device {
        private @Nullable String deviceLabel;
    }

    public static class Configuration {
        private @Nullable Boolean autoLockEnabled;
        private @Nullable String volume;
        private @Nullable String voiceLevel;

        public @Nullable Boolean getAutoLockEnabled() {
            return autoLockEnabled;
        }

        public @Nullable String getVolume() {
            return volume;
        }

        public @Nullable String getVoiceLevel() {
            return voiceLevel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(autoLockEnabled, volume, voiceLevel);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Configuration other = (Configuration) obj;
            return Objects.equals(autoLockEnabled, other.autoLockEnabled) && Objects.equals(volume, other.volume)
                    && Objects.equals(voiceLevel, other.voiceLevel);
        }

        @Override
        public String toString() {
            return "Configuration [autoLockEnabled=" + autoLockEnabled + ", volume=" + volume + ", voiceLevel="
                    + voiceLevel + "]";
        }
    }
}
