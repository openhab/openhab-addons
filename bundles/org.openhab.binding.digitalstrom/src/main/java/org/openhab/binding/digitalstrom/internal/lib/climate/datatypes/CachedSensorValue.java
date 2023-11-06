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
package org.openhab.binding.digitalstrom.internal.lib.climate.datatypes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CachedSensorValue} holds a read sensor value. For that the {@link CachedSensorValue} includes the sensor
 * type, sensor value and a the timestamp.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class CachedSensorValue {

    private final Logger logger = LoggerFactory.getLogger(CachedSensorValue.class);

    private final SensorEnum sensorType;
    private final Float sensorValue;
    private final String timestamp;

    /**
     * Create a new {@link CachedSensorValue}.
     *
     * @param sensorType must not be null
     * @param sensorValue must not be null
     * @param timestamp must not be null
     */
    public CachedSensorValue(SensorEnum sensorType, Float sensorValue, String timestamp) {
        this.sensorType = sensorType;
        this.sensorValue = sensorValue;
        this.timestamp = timestamp;
    }

    /**
     * Returns the sensor type as {@link SensorEnum}.
     *
     * @return the sensorType
     */
    public SensorEnum getSensorType() {
        return sensorType;
    }

    /**
     * Returns the sensor value.
     *
     * @return the sensorValue
     */
    public Float getSensorValue() {
        return sensorValue;
    }

    /**
     * Returns the timestamp as {@link String}.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the time stamp as {@link Date}.
     *
     * @return the timeStamp
     */
    public Date getTimestampAsDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
        try {
            return formatter.parse(timestamp);
        } catch (ParseException e) {
            logger.error("A ParseException occurred by parsing date string: {}", timestamp, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CachedSensorValue [SENSOR_TYPE=" + sensorType + ", SENSOR_VALUE=" + sensorValue + ", TIMESTAMP="
                + timestamp + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
        result = prime * result + ((sensorValue == null) ? 0 : sensorValue.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CachedSensorValue)) {
            return false;
        }
        CachedSensorValue other = (CachedSensorValue) obj;
        if (sensorType != other.sensorType) {
            return false;
        }
        if (sensorValue == null) {
            if (other.sensorValue != null) {
                return false;
            }
        } else if (!sensorValue.equals(other.sensorValue)) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        return true;
    }
}
