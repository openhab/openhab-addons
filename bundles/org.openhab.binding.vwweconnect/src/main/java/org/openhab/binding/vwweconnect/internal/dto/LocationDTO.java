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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Vehicle homeLocation representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class LocationDTO {

    private String errorCode = "";
    private PositionDTO position = new PositionDTO();

    public String getErrorCode() {
        return errorCode;
    }

    public PositionDTO getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "LocationDTO [errorCode=" + errorCode + ", position=" + position + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode.hashCode();
        result = prime * result + position.hashCode();
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
        LocationDTO other = (LocationDTO) obj;
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!position.equals(other.position)) {
            return false;
        }
        return true;
    }

    public class PositionDTO {

        private double lat = BaseVehicleDTO.UNDEFINED;
        private double lng = BaseVehicleDTO.UNDEFINED;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        @Override
        public String toString() {
            return "PositionDTO [lat=" + lat + ", lng=" + lng + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(lat);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(lng);
            result = prime * result + (int) (temp ^ (temp >>> 32));
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
            PositionDTO other = (PositionDTO) obj;
            if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) {
                return false;
            }
            if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng)) {
                return false;
            }
            return true;
        }

        private LocationDTO getEnclosingInstance() {
            return LocationDTO.this;
        }
    }
}
