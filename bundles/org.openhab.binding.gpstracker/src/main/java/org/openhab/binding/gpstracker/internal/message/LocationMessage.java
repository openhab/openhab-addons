/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.message;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LocationMessage} is a POJO for location messages sent bz trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class LocationMessage {

    /**
     * Message type
     */
    @SerializedName("_type")
    private String type = "";

    /**
     * Tracker ID used to display the initials of a user (iOS,Android/string/optional) required for http mode
     */
    @SerializedName("tid")
    private String trackerId = "";

    /**
     * Latitude (iOS, Android/float/meters/required)
     */
    @SerializedName("lat")
    private BigDecimal latitude = BigDecimal.ZERO;

    /**
     * Longitude (iOS,Android/float/meters/required)
     */
    @SerializedName("lon")
    private BigDecimal longitude = BigDecimal.ZERO;

    /**
     * GPS accuracy
     */
    @SerializedName("acc")
    private @Nullable BigDecimal gpsAccuracy;

    /**
     * Battery level (iOS,Android/integer/percent/optional)
     */
    @SerializedName("batt")
    private Integer batteryLevel = Integer.MIN_VALUE;

    /**
     * Timestamp at which the event occurred (iOS,Android/integer/epoch/required)
     */
    @SerializedName("tst")
    private Long timestampMillis = Long.MIN_VALUE;

    public String getTrackerId() {
        return trackerId.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Converts event timestamp onto DateTimeType
     *
     * @return Conversion result
     */
    public State getTimestamp() {
        if (timestampMillis != Long.MIN_VALUE) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(timestampMillis * 1000).toInstant(),
                    ZoneId.systemDefault());
            return new DateTimeType(zonedDateTime);
        }
        return UnDefType.UNDEF;
    }

    /**
     * Converts tracker coordinates into PointType
     *
     * @return Conversion result
     */
    public State getTrackerLocation() {
        if (!BigDecimal.ZERO.equals(latitude) && !BigDecimal.ZERO.equals(longitude)) {
            return new PointType(new DecimalType(latitude), new DecimalType(longitude));
        }
        return UnDefType.UNDEF;
    }

    /**
     * Converts battery level into DecimalType
     *
     * @return Conversion result
     */
    public State getBatteryLevel() {
        if (batteryLevel != Integer.MIN_VALUE) {
            return new DecimalType(batteryLevel);
        }
        return UnDefType.UNDEF;
    }

    public State getGpsAccuracy() {
        if (gpsAccuracy != null) {
            return new QuantityType<>(gpsAccuracy.intValue(), SIUnits.METRE);
        }
        return UnDefType.UNDEF;
    }

    @Override
    public String toString() {
        return "LocationMessage [" + ("type=" + type + ", ") + ("trackerId=" + trackerId + ", ")
                + ("latitude=" + latitude + ", ") + ("longitude=" + longitude + ", ")
                + (gpsAccuracy != null ? "gpsAccuracy=" + gpsAccuracy + ", " : "")
                + ("batteryLevel=" + batteryLevel + ", ") + ("timestampMillis=" + timestampMillis) + "]";
    }
}
