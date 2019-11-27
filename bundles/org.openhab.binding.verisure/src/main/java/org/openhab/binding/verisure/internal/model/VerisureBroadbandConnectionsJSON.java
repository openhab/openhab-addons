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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The broadband connections of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBroadbandConnectionsJSON extends VerisureBaseThingJSON {

    private @Nullable Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public VerisureBroadbandConnectionsJSON() {
    }

    /**
     *
     * @param data
     */
    public VerisureBroadbandConnectionsJSON(@Nullable Data data) {
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
        if (!(other instanceof VerisureBroadbandConnectionsJSON)) {
            return false;
        }
        VerisureBroadbandConnectionsJSON rhs = ((VerisureBroadbandConnectionsJSON) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Broadband {

        private @Nullable String testDate;
        private @Nullable Boolean isBroadbandConnected;
        @SerializedName("__typename")
        private @Nullable String typename;

        /**
         * No args constructor for use in serialization
         *
         */
        public Broadband() {
        }

        /**
         *
         * @param testDate
         * @param typename
         * @param isBroadbandConnected
         */
        public Broadband(@Nullable String testDate, @Nullable Boolean isBroadbandConnected, @Nullable String typename) {
            super();
            this.testDate = testDate;
            this.isBroadbandConnected = isBroadbandConnected;
            this.typename = typename;
        }

        public @Nullable String getTestDate() {
            return testDate;
        }

        public void setTestDate(@Nullable String testDate) {
            this.testDate = testDate;
        }

        public @Nullable Boolean isBroadbandConnected() {
            return isBroadbandConnected;
        }

        public void setBroadbandConnected(@Nullable Boolean isBroadbandConnected) {
            this.isBroadbandConnected = isBroadbandConnected;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("testDate", testDate)
                    .append("isBroadbandConnected", isBroadbandConnected).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(testDate).append(typename).append(isBroadbandConnected).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Broadband)) {
                return false;
            }
            Broadband rhs = ((Broadband) other);
            return new EqualsBuilder().append(testDate, rhs.testDate).append(typename, rhs.typename)
                    .append(isBroadbandConnected, rhs.isBroadbandConnected).isEquals();
        }

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

        private @Nullable Broadband broadband;
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
         * @param broadband
         */
        public Installation(@Nullable Broadband broadband, @Nullable String typename) {
            super();
            this.broadband = broadband;
            this.typename = typename;
        }

        public @Nullable Broadband getBroadband() {
            return broadband;
        }

        public void setBroadband(@Nullable Broadband broadband) {
            this.broadband = broadband;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        public void setTypename(@Nullable String typename) {
            this.typename = typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("broadband", broadband).append("typename", typename).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(broadband).toHashCode();
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
            return new EqualsBuilder().append(typename, rhs.typename).append(broadband, rhs.broadband).isEquals();
        }

    }

}
