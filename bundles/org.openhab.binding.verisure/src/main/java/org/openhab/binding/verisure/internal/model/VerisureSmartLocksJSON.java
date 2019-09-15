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
 * The smart locks of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLocksJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;
    private @Nullable VerisureSmartLockJSON smartLockJSON;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureSmartLocksJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureSmartLocksJSON(@Nullable Data data) {
        super();
        this.data = data;
    }

    public @Nullable Data getData() {
        return data;
    }

    public void setData(@Nullable Data data) {
        this.data = data;
    }

    public @Nullable VerisureSmartLockJSON getSmartLockJSON() {
        return smartLockJSON;
    }

    public void setSmartLockJSON(@Nullable VerisureSmartLockJSON smartLockJSON) {
        this.smartLockJSON = smartLockJSON;
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
        if (!(other instanceof VerisureSmartLocksJSON)) {
            return false;
        }
        VerisureSmartLocksJSON rhs = ((VerisureSmartLocksJSON) other);
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

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable List<Doorlock> doorlocks = null;

        /**
         * No args constructor for use in serialization
         *
         */
        public Installation() {
        }

        /**
         *
         * @param typename
         * @param doorlocks
         */
        public Installation(@Nullable String typename, @Nullable List<Doorlock> doorlocks) {
            super();
            this.typename = typename;
            this.doorlocks = doorlocks;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        public @Nullable List<Doorlock> getDoorlocks() {
            return doorlocks;
        }

        public void setDoorlocks(@Nullable List<Doorlock> doorlocks) {
            this.doorlocks = doorlocks;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("typename", typename).append("doorlocks", doorlocks).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(doorlocks).toHashCode();
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
            return new EqualsBuilder().append(typename, rhs.typename).append(doorlocks, rhs.doorlocks).isEquals();
        }

    }

    @NonNullByDefault
    public static class Device {

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable String area;
        private @Nullable String deviceLabel;

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
        public Device(@Nullable String typename, @Nullable String area, @Nullable String deviceLabel) {
            super();
            this.typename = typename;
            this.area = area;
            this.deviceLabel = deviceLabel;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        public @Nullable String getArea() {
            return area;
        }

        public void setArea(@Nullable String area) {
            this.area = area;
        }

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public void setDeviceLabel(@Nullable String deviceLabel) {
            this.deviceLabel = deviceLabel;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("typename", typename).append("area", area)
                    .append("deviceLabel", deviceLabel).toString();
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

    @NonNullByDefault
    public static class Doorlock {

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable String currentLockState;
        private @Nullable Device device;
        private @Nullable String eventTime;
        private @Nullable String method;
        private @Nullable Boolean motorJam;
        private @Nullable Boolean secureModeActive;
        private @Nullable String userString;

        /**
         * No args constructor for use in serialization
         *
         */
        public Doorlock() {
        }

        /**
         *
         * @param typename
         * @param device
         * @param secureModeActive
         * @param motorJam
         * @param method
         * @param eventTime
         * @param currentLockState
         * @param userString
         */
        public Doorlock(@Nullable String typename, @Nullable String currentLockState, @Nullable Device device,
                @Nullable String eventTime, @Nullable String method, @Nullable Boolean motorJam,
                @Nullable Boolean secureModeActive, @Nullable String userString) {
            super();
            this.typename = typename;
            this.currentLockState = currentLockState;
            this.device = device;
            this.eventTime = eventTime;
            this.method = method;
            this.motorJam = motorJam;
            this.secureModeActive = secureModeActive;
            this.userString = userString;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        public @Nullable String getCurrentLockState() {
            return currentLockState;
        }

        public void setCurrentLockState(@Nullable String currentLockState) {
            this.currentLockState = currentLockState;
        }

        public @Nullable Device getDevice() {
            return device;
        }

        public void setDevice(@Nullable Device device) {
            this.device = device;
        }

        public @Nullable String getEventTime() {
            return eventTime;
        }

        public void setEventTime(@Nullable String eventTime) {
            this.eventTime = eventTime;
        }

        public @Nullable String getMethod() {
            return method;
        }

        public void setMethod(@Nullable String method) {
            this.method = method;
        }

        public @Nullable Boolean isMotorJam() {
            return motorJam;
        }

        public void setMotorJam(@Nullable Boolean motorJam) {
            this.motorJam = motorJam;
        }

        public @Nullable Boolean getSecureModeActive() {
            return secureModeActive;
        }

        public void setSecureModeActive(@Nullable Boolean secureModeActive) {
            this.secureModeActive = secureModeActive;
        }

        public @Nullable String getUserString() {
            return userString;
        }

        public void setUserString(@Nullable String userString) {
            this.userString = userString;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("typename", typename).append("currentLockState", currentLockState)
                    .append("device", device).append("eventTime", eventTime).append("method", method)
                    .append("motorJam", motorJam).append("secureModeActive", secureModeActive)
                    .append("userString", userString).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(device).append(secureModeActive).append(motorJam)
                    .append(method).append(eventTime).append(currentLockState).append(userString).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Doorlock)) {
                return false;
            }
            Doorlock rhs = ((Doorlock) other);
            return new EqualsBuilder().append(typename, rhs.typename).append(device, rhs.device)
                    .append(secureModeActive, rhs.secureModeActive).append(motorJam, rhs.motorJam)
                    .append(method, rhs.method).append(eventTime, rhs.eventTime)
                    .append(currentLockState, rhs.currentLockState).append(userString, rhs.userString).isEquals();
        }

    }

}
