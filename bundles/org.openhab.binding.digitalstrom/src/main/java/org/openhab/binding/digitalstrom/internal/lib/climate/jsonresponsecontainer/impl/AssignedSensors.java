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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.climate.datatypes.AssignSensorType;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseZoneIdentifier;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link AssignedSensors} acts as container for the digitalSTROM json-method <i>getAssignedSensors</i>. So the
 * {@link AssignedSensors} contains all {@link AssignSensorType}s of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class AssignedSensors extends BaseZoneIdentifier {

    private List<AssignSensorType> sensors;

    /**
     * Creates a new {@link AssignedSensors} through the {@link JsonObject} that will be returned by an apartment call.
     *
     * @param jObject must not be null
     */
    public AssignedSensors(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    /**
     * Creates a new {@link AssignedSensors} through the {@link JsonObject} which will be returned by a zone call.
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public AssignedSensors(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()) != null
                && jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()).isJsonArray()) {
            JsonArray jArray = jObject.get(JSONApiResponseKeysEnum.SENSORS.getKey()).getAsJsonArray();
            if (jArray.size() != 0) {
                sensors = new LinkedList<>();
                Iterator<JsonElement> iter = jArray.iterator();
                while (iter.hasNext()) {
                    JsonObject assignedSensor = iter.next().getAsJsonObject();
                    Short sensorType = null;
                    String meterDSUID = null;
                    if (assignedSensor.get(JSONApiResponseKeysEnum.SENSOR_TYPE.getKey()) != null) {
                        sensorType = assignedSensor.get(JSONApiResponseKeysEnum.SENSOR_TYPE.getKey()).getAsShort();
                    }
                    if (assignedSensor.get(JSONApiResponseKeysEnum.DSUID_LOWER_CASE.getKey()) != null) {
                        meterDSUID = assignedSensor.get(JSONApiResponseKeysEnum.DSUID_LOWER_CASE.getKey())
                                .getAsString();
                    }
                    sensors.add(new AssignSensorType(SensorEnum.getSensor(sensorType), meterDSUID));
                }
            }
        }
    }

    /**
     * Returns a {@link List} of all assigned sensor types as {@link SensorEnum} of a zone.
     *
     * @return list of all assigned sensor types
     */
    public List<SensorEnum> getAssignedZoneSensorTypes() {
        List<SensorEnum> sensorTypes = new LinkedList<>();
        if (sensors != null) {
            for (AssignSensorType aSensorValue : sensors) {
                sensorTypes.add(aSensorValue.getSensorType());
            }
        }
        return sensorTypes;
    }

    /**
     * Returns a {@link List} of all {@link AssignSensorType} of a zone.
     *
     * @return list of {@link AssignSensorType}s
     */
    public List<AssignSensorType> getAssignedSensorTypes() {
        return sensors;
    }

    /**
     * Returns the {@link AssignSensorType} of the given sensorType or null, if the given sensorType does not exist.
     *
     * @param sensorType can be null
     * @return the {@link AssignSensorType} of the given sensorType or null
     */
    public AssignSensorType getAssignedSensorType(SensorEnum sensorType) {
        if (sensorType != null && sensors != null) {
            for (AssignSensorType aSensorValue : sensors) {
                if (aSensorValue.getSensorType().equals(sensorType)) {
                    return aSensorValue;
                }
            }
        }
        return null;
    }

    /**
     * Returns true, if the given sensorType exists at the zone, otherwise false.
     *
     * @param sensorType can be null
     * @return true, if sensorType exists, otherwise false
     */
    public boolean existSensorType(SensorEnum sensorType) {
        return getAssignedSensorType(sensorType) != null;
    }

    /**
     * Returns true, if sensors exists at the zone, otherwise false.
     *
     * @return true, if sensors exists, otherwise false
     */
    public boolean existsAssignedSensors() {
        return sensors != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AssignedSensors [sensors=" + sensors + "]";
    }
}
