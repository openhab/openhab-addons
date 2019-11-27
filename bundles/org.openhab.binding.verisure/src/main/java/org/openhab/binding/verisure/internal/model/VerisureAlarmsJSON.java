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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The alarms of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmsJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureAlarmsJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureAlarmsJSON(@Nullable Data data) {
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
        if (!(other instanceof VerisureAlarmsJSON)) {
            return false;
        }
        VerisureAlarmsJSON rhs = ((VerisureAlarmsJSON) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        @SerializedName("installation")
        @Expose
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

        private @Nullable ArmState armState;
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
         * @param armState
         */
        public Installation(@Nullable ArmState armState, @Nullable String typename) {
            super();
            this.armState = armState;
            this.typename = typename;
        }

        public @Nullable ArmState getArmState() {
            return armState;
        }

        public void setArmState(@Nullable ArmState armState) {
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
        private @Nullable Boolean allowedForFirstLine;
        private @Nullable Boolean allowed;
        private @Nullable List<Object> errorCodes = null;
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public ArmState() {
        }

        /**
         *
         * @param allowed
         * @param statusType
         * @param typename
         * @param name
         * @param allowedForFirstLine
         * @param errorCodes
         * @param date
         * @param type
         * @param changedVia
         */
        public ArmState(@Nullable String type, @Nullable String statusType, @Nullable String date,
                @Nullable String name, @Nullable String changedVia, @Nullable Boolean allowedForFirstLine,
                @Nullable Boolean allowed, @Nullable List<Object> errorCodes, @Nullable String typename) {
            super();
            this.type = type;
            this.statusType = statusType;
            this.date = date;
            this.name = name;
            this.changedVia = changedVia;
            this.allowedForFirstLine = allowedForFirstLine;
            this.allowed = allowed;
            this.errorCodes = errorCodes;
            this.typename = typename;
        }

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

        public @Nullable Boolean getAllowedForFirstLine() {
            return allowedForFirstLine;
        }

        public void setAllowedForFirstLine(@Nullable Boolean allowedForFirstLine) {
            this.allowedForFirstLine = allowedForFirstLine;
        }

        public @Nullable Boolean getAllowed() {
            return allowed;
        }

        public void setAllowed(@Nullable Boolean allowed) {
            this.allowed = allowed;
        }

        public @Nullable List<Object> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(@Nullable List<Object> errorCodes) {
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
