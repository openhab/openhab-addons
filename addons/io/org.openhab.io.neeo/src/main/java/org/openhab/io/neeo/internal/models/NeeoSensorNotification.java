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
package org.openhab.io.neeo.internal.models;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing an NEEO sensor notification (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoSensorNotification {

    /** The type of notification */
    private final String type;

    /** The value of the notification */
    private final SensorNotificationData data;

    /**
     * Instantiates a new neeo sensor notification from the key, item name and data using the default sensor update type
     *
     * @param deviceKey the non-null, non-empty device key
     * @param itemName the non-null, non-empty item name
     * @param data the possibly null, possibly empty (if a string) data
     */
    public NeeoSensorNotification(String deviceKey, String itemName, @Nullable Object data) {
        this(null, deviceKey, itemName, data);
    }

    /**
     * Instantiates a new neeo notification from the sensor type, key, item name and data.
     *
     * @param overrideType the sensor notification type
     * @param deviceKey the non-null, non-empty device key
     * @param itemName the non-null, non-empty item name
     * @param data the possibly null, possibly empty (if a string) data
     */
    public NeeoSensorNotification(@Nullable String overrideType, String deviceKey, String itemName,
            @Nullable Object data) {
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");
        NeeoUtil.requireNotEmpty(itemName, "itemName cannot be empty");

        this.type = overrideType == null || StringUtils.isEmpty(overrideType)
                ? NeeoConstants.NEEO_SENSOR_NOTIFICATION_TYPE
                : overrideType;
        this.data = new SensorNotificationData(
                deviceKey + ":" + itemName
                        + (StringUtils.endsWithIgnoreCase(itemName, NeeoConstants.NEEO_SENSOR_SUFFIX) ? ""
                                : NeeoConstants.NEEO_SENSOR_SUFFIX),
                data == null || (data instanceof String && StringUtils.isEmpty(data.toString())) ? "-" : data);
    }

    /**
     * Gets the notification type.
     *
     * @return the notification type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public SensorNotificationData getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NeeoNotification [type=" + type + ", data=" + data + "]";
    }

    class SensorNotificationData {
        private final String sensorEventKey;
        private final Object sensorValue;

        public SensorNotificationData(String sensorEventKey, Object sensorValue) {
            super();
            this.sensorEventKey = sensorEventKey;
            this.sensorValue = sensorValue;
        }

        @Override
        public String toString() {
            return "SensorNotificationData [sensorEventKey=" + sensorEventKey + ", sensorValue=" + sensorValue + "]";
        }
    }
}
