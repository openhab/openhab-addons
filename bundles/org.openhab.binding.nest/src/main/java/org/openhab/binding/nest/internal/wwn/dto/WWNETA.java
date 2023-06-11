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
package org.openhab.binding.nest.internal.wwn.dto;

import java.util.Date;

/**
 * Used to set and update the WWN ETA values.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Extract ETA object from Structure
 * @author Wouter Born - Add equals, hashCode, toString methods
 */
public class WWNETA {

    private String tripId;
    private Date estimatedArrivalWindowBegin;
    private Date estimatedArrivalWindowEnd;

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Date getEstimatedArrivalWindowBegin() {
        return estimatedArrivalWindowBegin;
    }

    public void setEstimatedArrivalWindowBegin(Date estimatedArrivalWindowBegin) {
        this.estimatedArrivalWindowBegin = estimatedArrivalWindowBegin;
    }

    public Date getEstimatedArrivalWindowEnd() {
        return estimatedArrivalWindowEnd;
    }

    public void setEstimatedArrivalWindowEnd(Date estimatedArrivalWindowEnd) {
        this.estimatedArrivalWindowEnd = estimatedArrivalWindowEnd;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WWNETA other = (WWNETA) obj;
        if (estimatedArrivalWindowBegin == null) {
            if (other.estimatedArrivalWindowBegin != null) {
                return false;
            }
        } else if (!estimatedArrivalWindowBegin.equals(other.estimatedArrivalWindowBegin)) {
            return false;
        }
        if (estimatedArrivalWindowEnd == null) {
            if (other.estimatedArrivalWindowEnd != null) {
                return false;
            }
        } else if (!estimatedArrivalWindowEnd.equals(other.estimatedArrivalWindowEnd)) {
            return false;
        }
        if (tripId == null) {
            if (other.tripId != null) {
                return false;
            }
        } else if (!tripId.equals(other.tripId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((estimatedArrivalWindowBegin == null) ? 0 : estimatedArrivalWindowBegin.hashCode());
        result = prime * result + ((estimatedArrivalWindowEnd == null) ? 0 : estimatedArrivalWindowEnd.hashCode());
        result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ETA [tripId=").append(tripId).append(", estimatedArrivalWindowBegin=")
                .append(estimatedArrivalWindowBegin).append(", estimatedArrivalWindowEnd=")
                .append(estimatedArrivalWindowEnd).append("]");
        return builder.toString();
    }
}
