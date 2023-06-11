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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseTemperatureControl;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link TemperatureControlStatus} acts as container for the digitalSTROM json-method
 * <i>getTemperatureControlStatus</i>. So the {@link TemperatureControlStatus} contains all heating
 * control status information of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlStatus extends BaseTemperatureControl {

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    private Short controlState;
    private Short operationMode;
    private Float temperature;
    private String temperatureTime;
    private Float nominalValue;
    private String nominalValueTime;
    private Float controlValue;
    private String controlValueTime;

    /**
     * Creates a new {@link TemperatureControlStatus} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public TemperatureControlStatus(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    /**
     * Creates a new {@link TemperatureControlStatus} through the {@link JsonObject} which will be returned by a zone
     * call.<br>
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public TemperatureControlStatus(JsonObject jObject, Integer zoneID, String zoneName) {
        super(jObject, zoneID, zoneName);
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (isNotSetOff()) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()) != null) {
                this.controlState = jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.OPERATION_MODE.getKey()) != null) {
                this.operationMode = jObject.get(JSONApiResponseKeysEnum.OPERATION_MODE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()) != null) {
                this.temperature = jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE.getKey()) != null) {
                this.nominalValue = jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE.getKey()) != null) {
                this.controlValue = jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey()) != null) {
                this.temperatureTime = jObject.get(JSONApiResponseKeysEnum.TEMPERATION_VALUE_TIME.getKey())
                        .getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE_TIME.getKey()) != null) {
                this.nominalValueTime = jObject.get(JSONApiResponseKeysEnum.NOMINAL_VALUE_TIME.getKey()).getAsString();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE_TIME.getKey()) != null) {
                this.controlValueTime = jObject.get(JSONApiResponseKeysEnum.CONTROL_VALUE_TIME.getKey()).getAsString();
            }
        }
    }

    /**
     * Returns the controleState for heating of the zone.
     *
     * @return the controlState
     */
    public Short getControlState() {
        return controlState;
    }

    /**
     * Returns the operationMode for heating of the zone.
     *
     * @return the operationMode
     */
    public Short getOperationMode() {
        return operationMode;
    }

    /**
     * Returns the current temperature of the zone.
     *
     * @return the temperature
     */
    public Float getTemperature() {
        return temperature;
    }

    /**
     * Returns the timestamp when the temperature was read out as {@link Date}.
     *
     * @return the temperatureTime
     * @throws ParseException see {@link DateFormat#parse(String)}
     */
    public Date getTemperatureTimeAsDate() throws ParseException {
        return formatter.parse(temperatureTime);
    }

    /**
     * Returns the timestamp when the temperature was read out as {@link String}.
     *
     * @return the temperatureTime
     */
    public String getTemperatureTimeAsString() {
        return temperatureTime;
    }

    /**
     * Returns the nominal value for heating of the zone.
     *
     * @return the nominalValue
     */
    public Float getNominalValue() {
        return nominalValue;
    }

    /**
     * Returns the timestamp as {@link Date} for the nominal value of the zone.
     *
     * @return the nominalValueTime
     * @throws ParseException see {@link DateFormat#parse(String)}
     */
    public Date getNominalValueTimeAsDate() throws ParseException {
        return formatter.parse(nominalValueTime);
    }

    /**
     * Returns the timestamp as {@link String} for the nominal value of the zone.
     *
     * @return the nominalValueTime
     */
    public String getNominalValueTimeAsString() {
        return nominalValueTime;
    }

    /**
     * Returns the control value for heating of the zone.
     *
     * @return the controlValue
     */
    public Float getControlValue() {
        return controlValue;
    }

    /**
     * Returns timestamp as {@link Date} for the control value for heating of the zone.
     *
     * @return the controlValueTime
     * @throws ParseException see {@link DateFormat#parse(String)}
     */
    public Date getControlValueTimeAsDate() throws ParseException {
        return formatter.parse(controlValueTime);
    }

    /**
     * Returns timestamp as {@link String} for the control value for heating of the zone.
     *
     * @return the controlValueTime
     */
    public String getControlValueTimeAsString() {
        return controlValueTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlStatus [controlMode=" + controlMode + ", controlState=" + controlState
                + ", operationMode=" + operationMode + ", temperature=" + temperature + ", temperatureTime="
                + temperatureTime + ", nominalValue=" + nominalValue + ", nominalValueTime=" + nominalValueTime
                + ", controlValue=" + controlValue + ", controlValueTime=" + controlValueTime + "]";
    }
}
