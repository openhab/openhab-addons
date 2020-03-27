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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_SMARTLOCK;

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
 * The smart locks of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartLocks extends VerisureBaseThing {

    private Data data = new Data();
    private @Nullable VerisureSmartLock smartLockJSON;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
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
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_SMARTLOCK;
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

        @SerializedName("__typename")
        private @Nullable String typename;
        private List<Doorlock> doorlocks = new ArrayList<>();

        public @Nullable String getTypename() {
            return typename;
        }

        public List<Doorlock> getDoorlocks() {
            return doorlocks;
        }

        public void setDoorlocks(List<Doorlock> doorlocks) {
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
