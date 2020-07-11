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
package org.openhab.binding.vwweconnect.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Vehicle details representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class Details {

    private VehicleDetails vehicleDetails = new VehicleDetails();
    private @Nullable String errorCode;

    public VehicleDetails getVehicleDetails() {
        return vehicleDetails;
    }

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("vehicleDetails", vehicleDetails).append("errorCode", errorCode)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(vehicleDetails).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Details)) {
            return false;
        }
        Details rhs = ((Details) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode).append(vehicleDetails, rhs.vehicleDetails)
                .isEquals();
    }

    public class VehicleDetails {

        private List<String> lastConnectionTimeStamp = new ArrayList<>();
        private double distanceCovered = BaseVehicle.UNDEFINED;
        private double range = BaseVehicle.UNDEFINED;
        private @Nullable String serviceInspectionData;
        private @Nullable String oilInspectionData;
        private boolean showOil;
        private boolean showService;
        private boolean flightMode;

        public List<String> getLastConnectionTimeStamp() {
            return lastConnectionTimeStamp;
        }

        public double getDistanceCovered() {
            return distanceCovered;
        }

        public double getRange() {
            return range;
        }

        public @Nullable String getServiceInspectionData() {
            return serviceInspectionData;
        }

        public @Nullable String getOilInspectionData() {
            return oilInspectionData;
        }

        public boolean getShowOil() {
            return showOil;
        }

        public boolean getShowService() {
            return showService;
        }

        public boolean getFlightMode() {
            return flightMode;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("lastConnectionTimeStamp", lastConnectionTimeStamp)
                    .append("distanceCovered", distanceCovered).append("range", range)
                    .append("serviceInspectionData", serviceInspectionData)
                    .append("oilInspectionData", oilInspectionData).append("showOil", showOil)
                    .append("showService", showService).append("flightMode", flightMode).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(oilInspectionData).append(distanceCovered).append(range)
                    .append(serviceInspectionData).append(lastConnectionTimeStamp).append(showOil).append(flightMode)
                    .append(showService).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof VehicleDetails)) {
                return false;
            }
            VehicleDetails rhs = ((VehicleDetails) other);
            return new EqualsBuilder().append(oilInspectionData, rhs.oilInspectionData)
                    .append(distanceCovered, rhs.distanceCovered).append(range, rhs.range)
                    .append(serviceInspectionData, rhs.serviceInspectionData)
                    .append(lastConnectionTimeStamp, rhs.lastConnectionTimeStamp).append(showOil, rhs.showOil)
                    .append(flightMode, rhs.flightMode).append(showService, rhs.showService).isEquals();
        }
    }
}
