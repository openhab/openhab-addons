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
package org.openhab.binding.verisure.internal.dto;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_USERPRESENCE;

import org.apache.commons.lang.builder.EqualsBuilder;
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
public class VerisureUserPresencesDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_USERPRESENCE;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof VerisureUserPresencesDTO)) {
            return false;
        }
        VerisureUserPresencesDTO rhs = ((VerisureUserPresencesDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

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
            return status != null ? new ToStringBuilder(this).append("isCallingUser", isCallingUser)
                    .append("webAccount", webAccount).append("status", status).append("xbnContactId", xbnContactId)
                    .append("currentLocationName", currentLocationName).append("deviceId", deviceId)
                    .append("name", name).append("currentLocationTimestamp", currentLocationTimestamp)
                    .append("deviceName", deviceName).append("currentLocationId", currentLocationId)
                    .append("typename", typename).toString() : "";
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
