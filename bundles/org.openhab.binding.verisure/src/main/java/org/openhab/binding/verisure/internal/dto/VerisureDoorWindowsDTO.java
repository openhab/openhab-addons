/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_DOORWINDOW;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The door and window devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowsDTO extends VerisureBaseThingDTO {

    private @Nullable VerisureBatteryStatusDTO batteryStatus;

    public @Nullable VerisureBatteryStatusDTO getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(@Nullable VerisureBatteryStatusDTO batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_DOORWINDOW;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
        VerisureDoorWindowsDTO other = (VerisureDoorWindowsDTO) obj;
        VerisureBatteryStatusDTO localBatteryStatusJSON = batteryStatus;
        if (localBatteryStatusJSON == null) {
            if (other.batteryStatus != null) {
                return false;
            }
        } else if (!localBatteryStatusJSON.equals(other.batteryStatus)) {
            return false;
        }
        return true;
    }

    public static class DoorWindow {

        private Device device = new Device();
        private @Nullable String type;
        private @Nullable String state;
        private boolean wired;
        private @Nullable String reportTime;
        @SerializedName("__typename")
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public @Nullable String getType() {
            return type;
        }

        public @Nullable String getState() {
            return state;
        }

        public boolean getWired() {
            return wired;
        }

        public @Nullable String getReportTime() {
            return reportTime;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + device.hashCode();
            String localReportTime = reportTime;
            result = prime * result + ((localReportTime == null) ? 0 : localReportTime.hashCode());
            String localState = state;
            result = prime * result + ((localState == null) ? 0 : localState.hashCode());
            String localType = type;
            result = prime * result + ((localType == null) ? 0 : localType.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            result = prime * result + (wired ? 1231 : 1237);
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
            DoorWindow other = (DoorWindow) obj;
            if (!device.equals(other.device)) {
                return false;
            }
            String localReportTime = reportTime;
            if (localReportTime == null) {
                if (other.reportTime != null) {
                    return false;
                }
            } else if (!localReportTime.equals(other.reportTime)) {
                return false;
            }
            String localState = state;
            if (localState == null) {
                if (other.state != null) {
                    return false;
                }
            } else if (!localState.equals(other.state)) {
                return false;
            }
            String localType = type;
            if (localType == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!localType.equals(other.type)) {
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
            if (wired != other.wired) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DoorWindow [device=" + device + ", type=" + type + ", state=" + state + ", wired=" + wired
                    + ", reportTime=" + reportTime + ", typename=" + typename + "]";
        }
    }
}
