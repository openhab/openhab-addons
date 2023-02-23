/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

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
    public int hashCode() {
        return super.hashCode();
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isBroadbandConnected ? 1231 : 1237);
            String localTestDate = testDate;
            result = prime * result + ((localTestDate == null) ? 0 : localTestDate.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Broadband other = (Broadband) obj;
            if (isBroadbandConnected != other.isBroadbandConnected) {
                return false;
            }
            String localTestDate = testDate;
            if (localTestDate == null) {
                if (other.testDate != null) {
                    return false;
                }
            } else if (!localTestDate.equals(other.testDate)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Broadband [testDate=" + testDate + ", isBroadbandConnected=" + isBroadbandConnected + ", typename="
                    + typename + "]";
        }
    }
}
