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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_USERPRESENCE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The user presences of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureUserPresences extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_USERPRESENCE;
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

        if (!(other instanceof VerisureUserPresences)) {
            return false;
        }
        VerisureUserPresences rhs = ((VerisureUserPresences) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
        }

        public void setInstallation(Installation installation) {
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

        private List<UserTracking> userTrackings = new ArrayList<>();
        @SerializedName("__typename")
        private @Nullable String typename;

        public List<UserTracking> getUserTrackings() {
            return userTrackings;
        }

        public void setUserTrackings(List<UserTracking> userTrackings) {
            this.userTrackings = userTrackings;
        }

        public @Nullable String getTypename() {
            return typename;
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
        private boolean isCallingUser;
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

        public boolean getIsCallingUser() {
            return isCallingUser;
        }

        public @Nullable String getWebAccount() {
            return webAccount;
        }

        public @Nullable String getStatus() {
            return status;
        }

        public @Nullable String getXbnContactId() {
            return xbnContactId;
        }

        public @Nullable String getCurrentLocationName() {
            return currentLocationName;
        }

        public @Nullable String getDeviceId() {
            return deviceId;
        }

        public @Nullable String getName() {
            return name;
        }

        public @Nullable String getCurrentLocationTimestamp() {
            return currentLocationTimestamp;
        }

        public @Nullable String getDeviceName() {
            return deviceName;
        }

        public @Nullable String getCurrentLocationId() {
            return currentLocationId;
        }

        public @Nullable String getTypename() {
            return typename;
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
