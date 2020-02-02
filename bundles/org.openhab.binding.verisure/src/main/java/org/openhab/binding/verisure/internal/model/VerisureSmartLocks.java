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
public class VerisureSmartLocks extends VerisureBaseThing {

    private @Nullable Data data;
    private @Nullable VerisureSmartLock smartLockJSON;

    public @Nullable Data getData() {
        return data;
    }

    public void setData(@Nullable Data data) {
        this.data = data;
    }

    public @Nullable VerisureSmartLock getSmartLockJSON() {
        return smartLockJSON;
    }

    public void setSmartLockJSON(@Nullable VerisureSmartLock smartLockJSON) {
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
        if (!(other instanceof VerisureSmartLocks)) {
            return false;
        }
        VerisureSmartLocks rhs = ((VerisureSmartLocks) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        private @Nullable Installation installation;

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
