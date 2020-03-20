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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_ALARM;

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
 * The alarms of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarms extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_ALARM;
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
        if (!(other instanceof VerisureAlarms)) {
            return false;
        }
        VerisureAlarms rhs = ((VerisureAlarms) other);
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

        private ArmState armState = new ArmState();
        @SerializedName("__typename")
        private @Nullable String typename;

        public ArmState getArmState() {
            return armState;
        }

        public void setArmState(ArmState armState) {
            this.armState = armState;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("armState", armState).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(armState).toHashCode();
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
            return new EqualsBuilder().append(typename, rhs.typename).append(armState, rhs.armState).isEquals();
        }

    }

    @NonNullByDefault
    public static class ArmState {

        private @Nullable String type;
        private @Nullable String statusType;
        private @Nullable String date;
        private @Nullable String name;
        private @Nullable String changedVia;
        private boolean allowedForFirstLine;
        private boolean allowed;
        private List<Object> errorCodes = new ArrayList<>();
        private @Nullable String typename;

        public @Nullable String getType() {
            return type;
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        public @Nullable String getStatusType() {
            return statusType;
        }

        public void setStatusType(@Nullable String statusType) {
            this.statusType = statusType;
        }

        public @Nullable String getDate() {
            return date;
        }

        public void setDate(@Nullable String date) {
            this.date = date;
        }

        public @Nullable String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        public @Nullable String getChangedVia() {
            return changedVia;
        }

        public void setChangedVia(@Nullable String changedVia) {
            this.changedVia = changedVia;
        }

        public boolean getAllowedForFirstLine() {
            return allowedForFirstLine;
        }

        public void setAllowedForFirstLine(boolean allowedForFirstLine) {
            this.allowedForFirstLine = allowedForFirstLine;
        }

        public boolean getAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        public List<Object> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(List<Object> errorCodes) {
            this.errorCodes = errorCodes;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("type", type).append("statusType", statusType).append("date", date)
                    .append("name", name).append("changedVia", changedVia)
                    .append("allowedForFirstLine", allowedForFirstLine).append("allowed", allowed)
                    .append("errorCodes", errorCodes).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(allowed).append(statusType).append(typename).append(name)
                    .append(allowedForFirstLine).append(errorCodes).append(date).append(type).append(changedVia)
                    .toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof ArmState)) {
                return false;
            }
            ArmState rhs = ((ArmState) other);
            return new EqualsBuilder().append(allowed, rhs.allowed).append(statusType, rhs.statusType)
                    .append(typename, rhs.typename).append(name, rhs.name)
                    .append(allowedForFirstLine, rhs.allowedForFirstLine).append(errorCodes, rhs.errorCodes)
                    .append(date, rhs.date).append(type, rhs.type).append(changedVia, rhs.changedVia).isEquals();
        }
    }
}
