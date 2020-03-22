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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_DOORWINDOW;

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
 * The door and window devices of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindows extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_DOORWINDOW;
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
        if (!(other instanceof VerisureDoorWindows)) {
            return false;
        }
        VerisureDoorWindows rhs = ((VerisureDoorWindows) other);
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

        private List<DoorWindow> doorWindows = new ArrayList<>();
        @SerializedName("__typename")
        private @Nullable String typename;

        public List<DoorWindow> getDoorWindows() {
            return doorWindows;
        }

        public void setDoorWindows(List<DoorWindow> doorWindows) {
            this.doorWindows = doorWindows;
        }

        public @Nullable String getTypename() {
            return typename;
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
}
