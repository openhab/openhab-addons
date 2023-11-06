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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link DeviceSensorValue} contains all needed information of a device sensor, e.g. the sensor type, to detect
 * which kind of sensor it is (see {@link SensorEnum}), the sensor index to read out sensor at the digitalSTROM device
 * by calling
 * {@link org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI#getDeviceSensorValue(String, DSID, String, String, Short)}
 * and as well as of course the value and timestamp of the last sensor update.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceSensorValue {

    private final Logger logger = LoggerFactory.getLogger(DeviceSensorValue.class);

    private SensorEnum sensorType;
    private Short sensorIndex;

    private Float floatValue;
    private Integer dsValue;

    private Date timestamp;
    private boolean valid = false;

    /**
     * Creates a new {@link DeviceSensorValue} through the {@link JsonObject} of the digitalSTROM json response for a
     * device.
     *
     * @param sensorValue must not be null
     */
    public DeviceSensorValue(JsonObject sensorValue) {
        if (sensorValue.get(JSONApiResponseKeysEnum.TYPE.getKey()) != null) {
            sensorType = SensorEnum.getSensor(sensorValue.get(JSONApiResponseKeysEnum.TYPE.getKey()).getAsShort());
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.INDEX.getKey()) != null) {
            sensorIndex = sensorValue.get(JSONApiResponseKeysEnum.INDEX.getKey()).getAsShort();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALID.getKey()) != null) {
            valid = sensorValue.get(JSONApiResponseKeysEnum.VALID.getKey()).getAsBoolean();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
            floatValue = sensorValue.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsFloat();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.VALUE_DS.getKey()) != null) {
            dsValue = sensorValue.get(JSONApiResponseKeysEnum.VALUE_DS.getKey()).getAsInt();
        }
        if (sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()) != null) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                timestamp = formatter.parse(sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()).getAsString());
            } catch (ParseException e) {
                logger.error("A ParseException occurred by parsing date string: {}",
                        sensorValue.get(JSONApiResponseKeysEnum.TIMESTAMP.getKey()).getAsString(), e);
            }
        }
    }

    /**
     * Creates a new {@link DeviceSensorValue} through the properties of a digitalSTROM
     * {@link org.openhab.binding.digitalstrom.internal.lib.event.constants.EventNames#DEVICE_SENSOR_VALUE} event.
     *
     * @param eventProperties must not be null
     */
    public DeviceSensorValue(Map<EventResponseEnum, String> eventProperties) {
        String strVal = eventProperties.get(EventResponseEnum.SENSOR_VALUE_FLOAT);
        if (strVal != null) {
            floatValue = Float.parseFloat(strVal);
        }
        strVal = eventProperties.get(EventResponseEnum.SENSOR_TYPE);
        if (strVal != null) {
            sensorType = SensorEnum.getSensor(Short.parseShort(strVal));
        }
        strVal = eventProperties.get(EventResponseEnum.SENSOR_VALUE);
        if (strVal != null) {
            dsValue = Integer.parseInt(strVal);
        }
        strVal = eventProperties.get(EventResponseEnum.SENSOR_INDEX);
        if (strVal != null) {
            sensorIndex = Short.parseShort(strVal);
        }
        timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
        valid = true;
    }

    /**
     * Creates a new {@link DeviceSensorValue} through the {@link SensorEnum} and the sensor index.
     *
     * @param sensorType must not be null
     * @param sensorIndex must not be null
     */
    public DeviceSensorValue(SensorEnum sensorType, Short sensorIndex) {
        this.sensorType = sensorType;
        this.sensorIndex = sensorIndex;
    }

    /**
     * Returns the {@link Float} value of this {@link DeviceSensorValue}.
     *
     * @return the floatValue
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * Sets a new sensor value as {@link Float}. The internal digitalSTROM value will be changed through the resolution
     * at {@link SensorEnum} automatically, too.
     *
     * @param floatValue the new float sensor value
     * @return true, if set was successful
     */
    public boolean setFloatValue(Float floatValue) {
        if (floatValue > -1) {
            this.floatValue = floatValue;
            if (sensorType.getResolution() != 800) {
                this.dsValue = (int) (floatValue / sensorType.getResolution());
            } else {
                this.dsValue = (int) (800 * Math.log10(floatValue));
            }
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    /**
     * Returns the internal digitalSTROM value as {@link Integer}. The resolution can be found at {@link SensorEnum},
     * but float sensor value will be changed through the resolution at {@link SensorEnum} automatically, too.
     *
     * @return the dsValue
     */
    public Integer getDsValue() {
        return dsValue;
    }

    /**
     * Sets a new internal digitalSTROM value as {@link Integer}.
     *
     * @param dsValue the internal digitalSTROM value to set
     * @return true, if set was successful
     */
    public boolean setDsValue(Integer dsValue) {
        if (dsValue > -1) {
            this.dsValue = dsValue;
            if (sensorType.getResolution() != 800) {
                this.floatValue = dsValue * sensorType.getResolution();
            } else {
                this.floatValue = 10 * (dsValue / sensorType.getResolution());
            }
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    /**
     * Sets a new internal digitalSTROM value as {@link Integer} and a new sensor value as {@link Float}.
     *
     * @param floatValue must not be null
     * @param dSvalue must not be null
     * @return true, if set was successful
     */
    public boolean setValues(Float floatValue, Integer dSvalue) {
        if (dsValue > -1 && floatValue > -1) {
            this.floatValue = floatValue;
            this.dsValue = dSvalue;
            timestamp = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
            this.valid = true;
            return true;
        }
        return false;
    }

    /**
     * Returns the sensor type as {@link SensorEnum} of this {@link DeviceSensorValue}.
     *
     * @return the sensorType
     */
    public SensorEnum getSensorType() {
        return sensorType;
    }

    /**
     * Returns the sensor index to read the sensor value out though
     * {@link org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI#getDeviceSensorValue(String, DSID, String, String, Short)}.
     *
     * @return the sensorIndex
     */
    public Short getSensorIndex() {
        return sensorIndex;
    }

    /**
     * Returns the timestamp of the last set value as {@link Date}.
     *
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns true if the sensor value is valid.
     *
     * @return the valid
     */
    public boolean getValid() {
        return valid;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceSensorValue [sensorType=" + sensorType + ", sensorIndex=" + sensorIndex + ", floatValue="
                + floatValue + ", dsValue=" + dsValue + ", timestamp=" + timestamp + ", valid=" + valid + "]";
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
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DeviceSensorValue other = (DeviceSensorValue) obj;
        if (sensorType != other.sensorType) {
            return false;
        }
        return true;
    }
}
