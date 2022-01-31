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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer;

import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.climate.datatypes.CachedSensorValue;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;

import com.google.gson.JsonObject;

/**
 * The {@link BaseSensorValues} is a base implementation of sensor response of the digitalSTROM-json-API.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public abstract class BaseSensorValues {

    private List<CachedSensorValue> sensorValues;

    /**
     * Adds a sensor value through the digitalSTROM-API response as {@link JsonObject}. The boolean outdoor has to be
     * set to indicate that it is an outdoor or indoor value (needed to choose the right sensor type).
     *
     * @param jObject must not be null
     * @param outdoor (true = outdoor; false = indoor)
     */
    protected void addSensorValue(JsonObject jObject, boolean outdoor) {
        if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.TEMPERATURE_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.TEMPERATURE_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.RELATIVE_HUMIDITY_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.RELATIVE_HUMIDITY_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.HUMIDITY_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE.getKey()) != null) {
            addSensorValue(new CachedSensorValue(SensorEnum.CARBON_DIOXIDE,
                    jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.CO2_CONCENTRATION_VALUE_TIME.getKey()).getAsString()));
        }
        if (jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE.getKey()) != null) {
            SensorEnum sensorType = SensorEnum.BRIGHTNESS_INDOORS;
            if (outdoor) {
                sensorType = SensorEnum.BRIGHTNESS_OUTDOORS;
            }
            addSensorValue(new CachedSensorValue(sensorType,
                    jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE.getKey()).getAsFloat(),
                    jObject.get(JSONApiResponseKeysEnum.BRIGHTNESS_VALUE_TIME.getKey()).getAsString()));
        }
    }

    private void addSensorValue(CachedSensorValue cachedSensorValue) {
        if (sensorValues == null) {
            sensorValues = new LinkedList<>();
            sensorValues.add(cachedSensorValue);
        } else {
            sensorValues.add(cachedSensorValue);
        }
    }

    /**
     * Returns the available sensor types.
     *
     * @return available sensor types
     */
    public List<SensorEnum> getAvailableSensorTypes() {
        List<SensorEnum> sensorTypes = new LinkedList<>();
        if (sensorValues != null) {
            for (CachedSensorValue cSensorValue : sensorValues) {
                sensorTypes.add(cSensorValue.getSensorType());
            }
        }
        return sensorTypes;
    }

    /**
     * Returns the {@link CachedSensorValue}'s as {@link List}.
     *
     * @return list of {@link CachedSensorValue}'s
     */
    public List<CachedSensorValue> getCachedSensorValues() {
        return sensorValues;
    }

    /**
     * Returns the {@link CachedSensorValue} of the given sensor type or null, if no {@link CachedSensorValue} for the
     * given sensor type exists.
     *
     * @param sensorType can be null
     * @return the {@link CachedSensorValue} of the given sensorType or null
     */
    public CachedSensorValue getCachedSensorValue(SensorEnum sensorType) {
        if (sensorType != null && sensorValues != null) {
            for (CachedSensorValue cSensorValue : sensorValues) {
                if (cSensorValue.getSensorType().equals(sensorType)) {
                    return cSensorValue;
                }
            }
        }
        return null;
    }

    /**
     * Returns true, if the given sensor type exist, otherwise false.
     *
     * @param sensorType can be null
     * @return true, if the given sensor type exist, otherwise false
     */
    public boolean existSensorValue(SensorEnum sensorType) {
        return getCachedSensorValue(sensorType) != null;
    }

    /**
     * Returns true, is sensor values exists, otherwise false.
     *
     * @return true, if sensor values exists, otherwise false
     */
    public boolean existSensorValues() {
        return sensorValues != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SensorValues [sensorValues=" + sensorValues + "]";
    }
}
