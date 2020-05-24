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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTLOCK;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureSmartLocksDTO)) {
            return false;
        }
        VerisureSmartLocksDTO rhs = ((VerisureSmartLocksDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
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
        public String toString() {
            return currentLockState != null
                    ? new ToStringBuilder(this).append("typename", typename)
                            .append("currentLockState", currentLockState).append("device", device)
                            .append("eventTime", eventTime).append("method", method).append("motorJam", motorJam)
                            .append("secureModeActive", secureModeActive).append("userString", userString).toString()
                    : "";
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
