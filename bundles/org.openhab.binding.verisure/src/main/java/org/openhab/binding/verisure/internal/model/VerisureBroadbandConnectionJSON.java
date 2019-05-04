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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * THe Broadband connection in Verisure.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBroadbandConnectionJSON extends VerisureBaseThingJSON {
    private @Nullable String date;
    private @Nullable Boolean hasWifi;

    /**
     * @return the date
     */
    public @Nullable String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return WiFi status
     */
    public @Nullable Boolean hasWiFi() {
        return hasWifi;
    }

    /**
     * @param hasWiFi
     */
    public void setHasWifi(Boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    @Override
    public @Nullable String getDeviceId() {
        return "broadband" + id;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((hasWifi == null) ? 0 : hasWifi.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VerisureClimateBaseJSON)) {
            return false;
        }

        VerisureBroadbandConnectionJSON other = (VerisureBroadbandConnectionJSON) obj;
        if (hasWifi == null) {
            if (other.hasWifi != null) {
                return false;
            }
        } else if (hasWifi != null && !hasWifi.equals(other.hasWifi)) {
            return false;
        }
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (date != null && !date.equals(other.date)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureBroadbandConnectionJSON [");
        if (date != null) {
            builder.append("date=");
            builder.append(date);
            builder.append(", ");
        }
        if (hasWifi != null) {
            builder.append("hasWifi=");
            builder.append(hasWifi);
            builder.append(", ");
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }

}
