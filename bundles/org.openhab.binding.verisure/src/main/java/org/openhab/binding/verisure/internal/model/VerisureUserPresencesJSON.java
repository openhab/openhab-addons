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
 * The user presences of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureUserPresencesJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureUserPresencesJSON() {
    }

    public VerisureUserPresencesJSON(Data data) {
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

        if (!(other instanceof VerisureUserPresencesJSON)) {
            return false;
        }
        VerisureUserPresencesJSON rhs = ((VerisureUserPresencesJSON) other);
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

        public Data(Installation installation) {
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

        private @Nullable List<UserTracking> userTrackings = null;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Installation() {
        }

        public @Nullable List<UserTracking> getUserTrackings() {
            return userTrackings;
        }

        public void setUserTrackings(@Nullable List<UserTracking> userTrackings) {
            this.userTrackings = userTrackings;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("userTrackings", userTrackings).append("typename", typename)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(userTrackings).append(typename).toHashCode();
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
            return new EqualsBuilder().append(userTrackings, rhs.userTrackings).append(typename, rhs.typename)
                    .isEquals();
        }

    }

    @NonNullByDefault
    public static class UserTracking {
        private @Nullable Boolean isCallingUser;
        private @Nullable String webAccount;
        private @Nullable String status;
        private @Nullable String xbnContactId;
        private @Nullable String currentLocationName;
        private @Nullable String deviceId;
        private @Nullable String name;
        private @Nullable String currentLocationTimestamp;
        private @Nullable String deviceName;
        private @Nullable String currentLocationId;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public UserTracking() {
        }

        /**
         *
         * @param currentLocationId
         * @param deviceName
         * @param status
         * @param currentLocationTimestamp
         * @param currentLocationName
         * @param typename
         * @param name
         * @param isCallingUser
         * @param xbnContactId
         * @param webAccount
         * @param deviceId
         */
        public UserTracking(@Nullable Boolean isCallingUser, @Nullable String webAccount, @Nullable String status,
                @Nullable String xbnContactId, @Nullable String currentLocationName, @Nullable String deviceId,
                @Nullable String name, @Nullable String currentLocationTimestamp, @Nullable String deviceName,
                @Nullable String currentLocationId, @Nullable String typename) {
            super();
            this.isCallingUser = isCallingUser;
            this.webAccount = webAccount;
            this.status = status;
            this.xbnContactId = xbnContactId;
            this.currentLocationName = currentLocationName;
            this.deviceId = deviceId;
            this.name = name;
            this.currentLocationTimestamp = currentLocationTimestamp;
            this.deviceName = deviceName;
            this.currentLocationId = currentLocationId;
            this.typename = typename;
        }

        public @Nullable Boolean getIsCallingUser() {
            return isCallingUser;
        }

        public void setIsCallingUser(@Nullable Boolean isCallingUser) {
            this.isCallingUser = isCallingUser;
        }

        public @Nullable String getWebAccount() {
            return webAccount;
        }

        public void setWebAccount(@Nullable String webAccount) {
            this.webAccount = webAccount;
        }

        public @Nullable String getStatus() {
            return status;
        }

        public void setStatus(@Nullable String status) {
            this.status = status;
        }

        public @Nullable String getXbnContactId() {
            return xbnContactId;
        }

        public void setXbnContactId(@Nullable String xbnContactId) {
            this.xbnContactId = xbnContactId;
        }

        public @Nullable String getCurrentLocationName() {
            return currentLocationName;
        }

        public void setCurrentLocationName(@Nullable String currentLocationName) {
            this.currentLocationName = currentLocationName;
        }

        public @Nullable String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(@Nullable String deviceId) {
            this.deviceId = deviceId;
        }

        public @Nullable String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        public @Nullable String getCurrentLocationTimestamp() {
            return currentLocationTimestamp;
        }

        public void setCurrentLocationTimestamp(@Nullable String currentLocationTimestamp) {
            this.currentLocationTimestamp = currentLocationTimestamp;
        }

        public @Nullable String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(@Nullable String deviceName) {
            this.deviceName = deviceName;
        }

        public @Nullable String getCurrentLocationId() {
            return currentLocationId;
        }

        public void setCurrentLocationId(@Nullable String currentLocationId) {
            this.currentLocationId = currentLocationId;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("isCallingUser", isCallingUser).append("webAccount", webAccount)
                    .append("status", status).append("xbnContactId", xbnContactId)
                    .append("currentLocationName", currentLocationName).append("deviceId", deviceId)
                    .append("name", name).append("currentLocationTimestamp", currentLocationTimestamp)
                    .append("deviceName", deviceName).append("currentLocationId", currentLocationId)
                    .append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(currentLocationId).append(deviceName).append(status)
                    .append(currentLocationTimestamp).append(currentLocationName).append(typename).append(name)
                    .append(isCallingUser).append(xbnContactId).append(webAccount).append(deviceId).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof UserTracking)) {
                return false;
            }
            UserTracking rhs = ((UserTracking) other);
            return new EqualsBuilder().append(currentLocationId, rhs.currentLocationId)
                    .append(deviceName, rhs.deviceName).append(status, rhs.status)
                    .append(currentLocationTimestamp, rhs.currentLocationTimestamp)
                    .append(currentLocationName, rhs.currentLocationName).append(typename, rhs.typename)
                    .append(name, rhs.name).append(isCallingUser, rhs.isCallingUser)
                    .append(xbnContactId, rhs.xbnContactId).append(webAccount, rhs.webAccount)
                    .append(deviceId, rhs.deviceId).isEquals();
        }

    }
}
