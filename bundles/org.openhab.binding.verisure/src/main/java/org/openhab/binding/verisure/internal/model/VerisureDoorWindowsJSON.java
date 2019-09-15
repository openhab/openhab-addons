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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The door and window devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowsJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureDoorWindowsJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureDoorWindowsJSON(@Nullable Data data) {
        super();
        this.data = data;
    }

    public @Nullable Data getData() {
        return data;
    }

    public void setData(@Nullable Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureDoorWindowsJSON)) {
            return false;
        }
        VerisureDoorWindowsJSON rhs = ((VerisureDoorWindowsJSON) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        private @Nullable Installation installation;

        /**
         * No args constructor for use in serialization
         *
         */
        public Data() {
        }

        /**
         *
         * @param installation
         */
        public Data(@Nullable Installation installation) {
            super();
            this.installation = installation;
        }

        public @Nullable Installation getInstallation() {
            return installation;
        }

        public void setInstallation(@Nullable Installation installation) {
            this.installation = installation;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("installation", installation).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(installation).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Data)) {
                return false;
            }
            Data rhs = ((Data) other);
            return new EqualsBuilder().append(installation, rhs.installation).isEquals();
        }

    }

    @NonNullByDefault
    public static class Installation {

        private @Nullable List<DoorWindow> doorWindows = null;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Installation() {
        }

        /**
         *
         * @param typename
         * @param doorWindows
         */
        public Installation(@Nullable List<DoorWindow> doorWindows, @Nullable String typename) {
            super();
            this.doorWindows = doorWindows;
            this.typename = typename;
        }

        public @Nullable List<DoorWindow> getDoorWindows() {
            return doorWindows;
        }

        public void setDoorWindows(@Nullable List<DoorWindow> doorWindows) {
            this.doorWindows = doorWindows;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("doorWindows", doorWindows).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(doorWindows).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Installation)) {
                return false;
            }
            Installation rhs = ((Installation) other);
            return new EqualsBuilder().append(typename, rhs.typename).append(doorWindows, rhs.doorWindows).isEquals();
        }

    }

    @NonNullByDefault
    public static class DoorWindow {

        private @Nullable Device device;
        private @Nullable String type;
        private @Nullable String state;
        private @Nullable Boolean wired;
        private @Nullable String reportTime;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public DoorWindow() {
        }

        /**
         *
         * @param reportTime
         * @param typename
         * @param state
         * @param device
         * @param wired
         * @param type
         */
        public DoorWindow(@Nullable Device device, @Nullable String type, @Nullable String state,
                @Nullable Boolean wired, @Nullable String reportTime, @Nullable String typename) {
            super();
            this.device = device;
            this.type = type;
            this.state = state;
            this.wired = wired;
            this.reportTime = reportTime;
            this.typename = typename;
        }

        public @Nullable Device getDevice() {
            return device;
        }

        public void setDevice(@Nullable Device device) {
            this.device = device;
        }

        public @Nullable String getType() {
            return type;
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        public @Nullable String getState() {
            return state;
        }

        public void setState(@Nullable String state) {
            this.state = state;
        }

        public @Nullable Boolean getWired() {
            return wired;
        }

        public void setWired(@Nullable Boolean wired) {
            this.wired = wired;
        }

        public @Nullable String getReportTime() {
            return reportTime;
        }

        public void setReportTime(@Nullable String reportTime) {
            this.reportTime = reportTime;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("device", device).append("type", type).append("state", state)
                    .append("wired", wired).append("reportTime", reportTime).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(reportTime).append(typename).append(state).append(device).append(wired)
                    .append(type).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof DoorWindow)) {
                return false;
            }
            DoorWindow rhs = ((DoorWindow) other);
            return new EqualsBuilder().append(reportTime, rhs.reportTime).append(typename, rhs.typename)
                    .append(state, rhs.state).append(device, rhs.device).append(wired, rhs.wired).append(type, rhs.type)
                    .isEquals();
        }

    }

    @NonNullByDefault
    public static class Device {

        private @Nullable String deviceLabel;
        private @Nullable String area;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Device() {
        }

        /**
         *
         * @param area
         * @param typename
         * @param deviceLabel
         */
        public Device(@Nullable String deviceLabel, @Nullable String area, @Nullable String typename) {
            super();
            this.deviceLabel = deviceLabel;
            this.area = area;
            this.typename = typename;
        }

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public void setDeviceLabel(@Nullable String deviceLabel) {
            this.deviceLabel = deviceLabel;
        }

        public @Nullable String getArea() {
            return area;
        }

        public void setArea(@Nullable String area) {
            this.area = area;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("deviceLabel", deviceLabel).append("area", area)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(area).append(typename).append(deviceLabel).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Device)) {
                return false;
            }
            Device rhs = ((Device) other);
            return new EqualsBuilder().append(area, rhs.area).append(typename, rhs.typename)
                    .append(deviceLabel, rhs.deviceLabel).isEquals();
        }

    }

}
