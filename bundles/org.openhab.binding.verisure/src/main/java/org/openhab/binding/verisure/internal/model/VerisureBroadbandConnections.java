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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_BROADBAND_CONNECTION;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The broadband connections of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBroadbandConnections extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_BROADBAND_CONNECTION;
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
        if (!(other instanceof VerisureBroadbandConnections)) {
            return false;
        }
        VerisureBroadbandConnections rhs = ((VerisureBroadbandConnections) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Broadband {

        private @Nullable String testDate;
        private boolean isBroadbandConnected;
        @SerializedName("__typename")
        private @Nullable String typename;

        public @Nullable String getTestDate() {
            return testDate;
        }

        public boolean isBroadbandConnected() {
            return isBroadbandConnected;
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

        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
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

        private Broadband broadband = new Broadband();
        @SerializedName("__typename")
        private @Nullable String typename;

        public Broadband getBroadband() {
            return broadband;
        }

        public @Nullable String getTypename() {
            return typename;
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
