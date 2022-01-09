/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTLOCK;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The smart locks of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLocksDTO extends VerisureBaseThingDTO {

    private @Nullable VerisureSmartLockDTO smartLockJSON;

    public @Nullable VerisureSmartLockDTO getSmartLockJSON() {
        return smartLockJSON;
    }

    public void setSmartLockJSON(@Nullable VerisureSmartLockDTO smartLockJSON) {
        this.smartLockJSON = smartLockJSON;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_SMARTLOCK;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        VerisureSmartLockDTO localSmartLockJSON = smartLockJSON;
        result = prime * result + ((localSmartLockJSON == null) ? 0 : localSmartLockJSON.hashCode());
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureSmartLocksDTO other = (VerisureSmartLocksDTO) obj;
        VerisureSmartLockDTO localSmartLockJSON = smartLockJSON;
        if (localSmartLockJSON == null) {
            if (other.smartLockJSON != null) {
                return false;
            }
        } else if (!localSmartLockJSON.equals(other.smartLockJSON)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VerisureSmartLocksDTO [smartLockJSON=" + smartLockJSON + "]";
    }

    public static class Doorlock {

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable String currentLockState;
        private @Nullable String eventTime;
        private @Nullable String method;
        private @Nullable String userString;
        private Device device = new Device();
        private boolean motorJam;
        private boolean secureModeActive;

        public @Nullable String getTypename() {
            return typename;
        }

        public @Nullable String getCurrentLockState() {
            return currentLockState;
        }

        public Device getDevice() {
            return device;
        }

        public @Nullable String getEventTime() {
            return eventTime;
        }

        public @Nullable String getMethod() {
            return method;
        }

        public boolean isMotorJam() {
            return motorJam;
        }

        public boolean getSecureModeActive() {
            return secureModeActive;
        }

        public @Nullable String getUserString() {
            return userString;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localCurrentLockState = currentLockState;
            result = prime * result + ((localCurrentLockState == null) ? 0 : localCurrentLockState.hashCode());
            result = prime * result + device.hashCode();
            String localEventTime = eventTime;
            result = prime * result + ((localEventTime == null) ? 0 : localEventTime.hashCode());
            String localMethod = method;
            result = prime * result + ((localMethod == null) ? 0 : localMethod.hashCode());
            result = prime * result + (motorJam ? 1231 : 1237);
            result = prime * result + (secureModeActive ? 1231 : 1237);
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            String localUserString = userString;
            result = prime * result + ((localUserString == null) ? 0 : localUserString.hashCode());
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
            Doorlock other = (Doorlock) obj;
            String localCurrentLockState = currentLockState;
            if (localCurrentLockState == null) {
                if (other.currentLockState != null) {
                    return false;
                }
            } else if (!localCurrentLockState.equals(other.currentLockState)) {
                return false;
            }
            if (!device.equals(other.device)) {
                return false;
            }
            String localEventTime = eventTime;
            if (localEventTime == null) {
                if (other.eventTime != null) {
                    return false;
                }
            } else if (!localEventTime.equals(other.eventTime)) {
                return false;
            }
            String localMethod = method;
            if (localMethod == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!localMethod.equals(other.method)) {
                return false;
            }
            if (motorJam != other.motorJam) {
                return false;
            }
            if (secureModeActive != other.secureModeActive) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            String localUserString = userString;
            if (localUserString == null) {
                if (other.userString != null) {
                    return false;
                }
            } else if (!localUserString.equals(other.userString)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Doorlock [typename=" + typename + ", currentLockState=" + currentLockState + ", eventTime="
                    + eventTime + ", method=" + method + ", userString=" + userString + ", device=" + device
                    + ", motorJam=" + motorJam + ", secureModeActive=" + secureModeActive + "]";
        }
    }
}
