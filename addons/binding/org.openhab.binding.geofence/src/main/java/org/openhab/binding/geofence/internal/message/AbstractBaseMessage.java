/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.message;

import com.google.gson.annotations.SerializedName;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Common part of tracker messages.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class AbstractBaseMessage {
    /**
     * Message type
     */
    @SerializedName("_type")
    String type;

    /**
     * Tracker ID used to display the initials of a user (iOS,Android/string/optional) required for http mode
     */
    @SerializedName("tid")
    String trackerId;

    /**
     * Latitude (iOS, Android/float/meters/required)
     */
    @SerializedName("lat")
    BigDecimal latitude;

    /**
     * Longitude (iOS,Android/float/meters/required)
     */
    @SerializedName("lon")
    BigDecimal longitude;

    /**
     *  Accuracy of the reported location in meters without unit (iOS,Android/integer/meters/optional)
     */
    @SerializedName("acc")
    BigDecimal accuracy;

    /**
     * Device battery level (iOS,Android/integer/percent/optional)
     */
    @SerializedName("batt")
    Integer batteryLevel;

    /**
     * Timestamp at which the event occurred (iOS,Android/integer/epoch/required)
     */
    @SerializedName("tst")
    Long timestampMillis;

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
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(timestampMillis*1000).toInstant(),
                    ZoneId.systemDefault());
            return new DateTimeType(zonedDateTime);
        }
        return null;
    }

    /**
     * Converts coordinates into PointType
     *
     * @return Conversion result
     */
    public PointType getPoint() {
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
}
