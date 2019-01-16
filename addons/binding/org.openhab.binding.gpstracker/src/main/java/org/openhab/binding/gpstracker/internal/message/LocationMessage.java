/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.message;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LocationMessage} is a POJO for location messages sent bz trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class LocationMessage {

    /**
     * Message type
     */
    @SerializedName("_type")
    private String type;

    /**
     * Tracker ID used to display the initials of a user (iOS,Android/string/optional) required for http mode
     */
    @SerializedName("tid")
    private String trackerId;

    /**
     * Latitude (iOS, Android/float/meters/required)
     */
    @SerializedName("lat")
    private BigDecimal latitude;

    /**
     * Longitude (iOS,Android/float/meters/required)
     */
    @SerializedName("lon")
    private BigDecimal longitude;

    /**
     * GPS accuracy
     */
    @SerializedName("acc")
    private BigDecimal gpsAccuracy;

    /**
     * Battery level (iOS,Android/integer/percent/optional)
     */
    @SerializedName("batt")
    private Integer batteryLevel;

    /**
     * Timestamp at which the event occurred (iOS,Android/integer/epoch/required)
     */
    @SerializedName("tst")
    private Long timestampMillis;

    public String getTrackerId() {
        return trackerId.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Converts event timestamp onto DateTimeType
     *
     * @return Conversion result
     */
    public DateTimeType getTimestamp() {
        if (timestampMillis != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(timestampMillis * 1000).toInstant(),
                    ZoneId.systemDefault());
            return new DateTimeType(zonedDateTime);
        }
        return null;
    }

    /**
     * Converts tracker coordinates into PointType
     *
     * @return Conversion result
     */
    public PointType getTrackerLocation() {
        if (latitude != null && longitude != null) {
            return new PointType(new DecimalType(latitude), new DecimalType(longitude));
        }
        return null;
    }

    /**
     * Converts battery level into DecimalType
     *
     * @return Conversion result
     */
    public DecimalType getBatteryLevel() {
        if (batteryLevel != null) {
            return new DecimalType(batteryLevel);
        }
        return null;
    }

    public BigDecimal getGpsAccuracy() {
        return gpsAccuracy;
    }

    @Override
    public String toString() {
        return "LocationMessage [" + (type != null ? "type=" + type + ", " : "")
                + (trackerId != null ? "trackerId=" + trackerId + ", " : "")
                + (latitude != null ? "latitude=" + latitude + ", " : "")
                + (longitude != null ? "longitude=" + longitude + ", " : "")
                + (gpsAccuracy != null ? "gpsAccuracy=" + gpsAccuracy + ", " : "")
                + (batteryLevel != null ? "batteryLevel=" + batteryLevel + ", " : "")
                + (timestampMillis != null ? "timestampMillis=" + timestampMillis : "") + "]";
    }
}
