/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Vehicle details representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class DetailsDTO {

    private VehicleDetailsDTO vehicleDetails = new VehicleDetailsDTO();
    private String errorCode = "";

    public VehicleDetailsDTO getVehicleDetails() {
        return vehicleDetails;
    }

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "DetailsDTO [vehicleDetails=" + vehicleDetails + ", errorCode=" + errorCode + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode.hashCode();
        result = prime * result + vehicleDetails.hashCode();
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
        DetailsDTO other = (DetailsDTO) obj;
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!vehicleDetails.equals(other.vehicleDetails)) {
            return false;
        }
        return true;
    }

    public class VehicleDetailsDTO {

        private List<String> lastConnectionTimeStamp = new ArrayList<>();
        private double distanceCovered = BaseVehicleDTO.UNDEFINED;
        private double range = BaseVehicleDTO.UNDEFINED;
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
            return "VehicleDetailsDTO [lastConnectionTimeStamp=" + lastConnectionTimeStamp + ", distanceCovered="
                    + distanceCovered + ", range=" + range + ", serviceInspectionData=" + serviceInspectionData
                    + ", oilInspectionData=" + oilInspectionData + ", showOil=" + showOil + ", showService="
                    + showService + ", flightMode=" + flightMode + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(distanceCovered);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + (flightMode ? 1231 : 1237);
            result = prime * result + lastConnectionTimeStamp.hashCode();
            String oilInspectionData2 = oilInspectionData;
            result = prime * result + ((oilInspectionData2 == null) ? 0 : oilInspectionData2.hashCode());
            temp = Double.doubleToLongBits(range);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            String serviceInspectionData2 = serviceInspectionData;
            result = prime * result + ((serviceInspectionData2 == null) ? 0 : serviceInspectionData2.hashCode());
            result = prime * result + (showOil ? 1231 : 1237);
            result = prime * result + (showService ? 1231 : 1237);
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
            VehicleDetailsDTO other = (VehicleDetailsDTO) obj;
            if (Double.doubleToLongBits(distanceCovered) != Double.doubleToLongBits(other.distanceCovered)) {
                return false;
            }
            if (flightMode != other.flightMode) {
                return false;
            }
            if (!lastConnectionTimeStamp.equals(other.lastConnectionTimeStamp)) {
                return false;
            }
            if (oilInspectionData == null) {
                if (other.oilInspectionData != null) {
                    return false;
                }
            } else if (oilInspectionData != null && !oilInspectionData.equals(other.oilInspectionData)) {
                return false;
            }
            if (Double.doubleToLongBits(range) != Double.doubleToLongBits(other.range)) {
                return false;
            }
            if (serviceInspectionData == null) {
                if (other.serviceInspectionData != null) {
                    return false;
                }
            } else if (serviceInspectionData != null && !serviceInspectionData.equals(other.serviceInspectionData)) {
                return false;
            }
            if (showOil != other.showOil) {
                return false;
            }
            if (showService != other.showService) {
                return false;
            }
            return true;
        }

        private DetailsDTO getEnclosingInstance() {
            return DetailsDTO.this;
        }
    }
}
