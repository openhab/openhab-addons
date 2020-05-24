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

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_BROADBAND_CONNECTION;

import org.apache.commons.lang.builder.EqualsBuilder;
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
public class VerisureBroadbandConnectionsDTO extends VerisureBaseThingDTO {

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_BROADBAND_CONNECTION;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VerisureBroadbandConnectionsDTO)) {
            return false;
        }
        VerisureBroadbandConnectionsDTO rhs = ((VerisureBroadbandConnectionsDTO) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

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

        @Override
        public String toString() {
            return testDate != null ? new ToStringBuilder(this).append("testDate", testDate)
                    .append("isBroadbandConnected", isBroadbandConnected).append("typename", typename).toString() : "";
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
}
