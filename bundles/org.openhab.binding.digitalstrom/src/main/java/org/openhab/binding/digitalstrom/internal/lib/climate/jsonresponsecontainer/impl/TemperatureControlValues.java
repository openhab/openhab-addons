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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.lib.climate.constants.OperationModes;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseZoneIdentifier;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link TemperatureControlValues} acts as container for the digitalSTROM json-method
 * <i>getTemperatureControlValues</i>. So the {@link TemperatureControlValues} contains all temperature
 * control values information of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlValues extends BaseZoneIdentifier {

    private Map<OperationModes, Float> temperatureControlValues;
    private String controlDSUID;
    private Boolean isConfigured;

    /**
     * Creates a new {@link TemperatureControlValues} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public TemperatureControlValues(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    /**
     * Creates a new {@link TemperatureControlValues} through the {@link JsonObject} which will be returned by a zone
     * call.<br>
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public TemperatureControlValues(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()) != null) {
            this.isConfigured = jObject.get(JSONApiResponseKeysEnum.IS_CONFIGURED.getKey()).getAsBoolean();
        }
        if (isConfigured) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
                this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
            }
            temperatureControlValues = new HashMap<>(OperationModes.values().length);
            for (OperationModes opMode : OperationModes.values()) {
                if (jObject.get(opMode.getKey()) != null) {
                    temperatureControlValues.put(opMode, jObject.get(opMode.getKey()).getAsFloat());
                }
            }
        }
    }

    /**
     * @see TemperatureControlStatus#getControlDSUID()
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * @see TemperatureControlStatus#getIsConfigured()
     * @return the isConfigured
     */
    public Boolean getIsConfigured() {
        return isConfigured;
    }

    /**
     * Returns the set temperature of the given operation mode.
     *
     * @param operationMode must not be null
     * @return temperature of the operation mode
     */
    public Float getTemperation(OperationModes operationMode) {
        return temperatureControlValues.get(operationMode);
    }

    /**
     * Returns the available operation modes as {@link Set}.
     *
     * @return available operation modes
     */
    public Set<OperationModes> getOperationModes() {
        return temperatureControlValues.keySet();
    }

    /**
     * Returns a {@link Map} that maps the available operation modes to the set values.
     *
     * @return Map with operation modes and their values
     */
    public Map<OperationModes, Float> getTemperatureControlValues() {
        return temperatureControlValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlValues [temperatureControlValues=" + temperatureControlValues + ", controlDSUID="
                + controlDSUID + ", isConfigured=" + isConfigured + "]";
    }
}
