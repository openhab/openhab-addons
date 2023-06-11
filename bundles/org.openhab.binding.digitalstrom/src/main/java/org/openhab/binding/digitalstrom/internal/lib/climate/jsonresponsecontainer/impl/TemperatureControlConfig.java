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

import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseTemperatureControl;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link TemperatureControlConfig} acts as container for the digitalSTROM json-method
 * <i>getTemperatureControlConfig</i>. So the {@link TemperatureControlConfig} contains all heating control
 * configurations of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlConfig extends BaseTemperatureControl {

    private Integer referenceZone;
    private Float ctrlOffset;
    private Float manualValue;
    private Float emergencyValue;
    private Float ctrlKp;
    private Float ctrlTs;
    private Float ctrlTi;
    private Float ctrlKd;
    private Float ctrlImin;
    private Float ctrlImax;
    private Float ctrlYmin;
    private Float ctrlYmax;
    private Boolean ctrlAntiWindUp;
    private Boolean ctrlKeepFloorWarm;

    /**
     * Creates a new {@link TemperatureControlConfig} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public TemperatureControlConfig(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    /**
     * Creates a new {@link TemperatureControlConfig} through the {@link JsonObject} which will be returned by an zone
     * call.<br>
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public TemperatureControlConfig(JsonObject jObject, Integer zoneID, String zoneName) {
        super(jObject, zoneID, zoneName);
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (isNotSetOff()) {
            if (controlMode == 1) {
                if (jObject.get(JSONApiResponseKeysEnum.EMERGENCY_VALUE.getKey()) != null) {
                    this.emergencyValue = jObject.get(JSONApiResponseKeysEnum.EMERGENCY_VALUE.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KP.getKey()) != null) {
                    this.ctrlKp = jObject.get(JSONApiResponseKeysEnum.CTRL_KP.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_TS.getKey()) != null) {
                    this.ctrlTs = jObject.get(JSONApiResponseKeysEnum.CTRL_TS.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_TI.getKey()) != null) {
                    this.ctrlTi = jObject.get(JSONApiResponseKeysEnum.CTRL_TI.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KD.getKey()) != null) {
                    this.ctrlKd = jObject.get(JSONApiResponseKeysEnum.CTRL_KD.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_MIN.getKey()) != null) {
                    this.ctrlImin = jObject.get(JSONApiResponseKeysEnum.CTRL_MIN.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_MAX.getKey()) != null) {
                    this.ctrlImax = jObject.get(JSONApiResponseKeysEnum.CTRL_MAX.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MIN.getKey()) != null) {
                    this.ctrlYmin = jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MIN.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MAX.getKey()) != null) {
                    this.ctrlYmax = jObject.get(JSONApiResponseKeysEnum.CTRL_Y_MAX.getKey()).getAsFloat();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_KEEP_FLOOR_WARM.getKey()) != null) {
                    this.ctrlKeepFloorWarm = jObject.get(JSONApiResponseKeysEnum.CTRL_KEEP_FLOOR_WARM.getKey())
                            .getAsBoolean();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()) != null) {
                    this.ctrlAntiWindUp = jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey())
                            .getAsBoolean();
                }
            }
            if (controlMode == 2) {
                if (jObject.get(JSONApiResponseKeysEnum.REFERENCE_ZONE.getKey()) != null) {
                    this.referenceZone = jObject.get(JSONApiResponseKeysEnum.REFERENCE_ZONE.getKey()).getAsInt();
                }
                if (jObject.get(JSONApiResponseKeysEnum.CTRL_OFFSET.getKey()) != null) {
                    this.ctrlOffset = jObject.get(JSONApiResponseKeysEnum.CTRL_OFFSET.getKey()).getAsFloat();
                }
            }
        }
    }

    /**
     * Returns the refenceZone, if control-mode is {@link ControlModes#ZONE_FOLLOWER}, otherwise null.
     *
     * @return the referenceZone
     */
    public Integer getReferenceZone() {
        return referenceZone;
    }

    /**
     * Returns the ctrlOffset, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlOffset
     */
    public Float getCtrlOffset() {
        return ctrlOffset;
    }

    /**
     * Returns the manualValue, if control-mode is {@link ControlModes#MANUAL}, otherwise null.
     *
     * @return the manualValue
     */
    public Float getManualValue() {
        return manualValue;
    }

    /**
     * Returns the emergencyValue, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the emergencyValue
     */
    public Float getEmergencyValue() {
        return emergencyValue;
    }

    /**
     * Returns the ctrlKp, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlKp
     */
    public Float getCtrlKp() {
        return ctrlKp;
    }

    /**
     * Returns the ctrlTs, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlTs
     */
    public Float getCtrlTs() {
        return ctrlTs;
    }

    /**
     * Returns the ctrlTi, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlTi
     */
    public Float getCtrlTi() {
        return ctrlTi;
    }

    /**
     * Returns the ctrlKd, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlKd
     */
    public Float getCtrlKd() {
        return ctrlKd;
    }

    /**
     * Returns the ctrlImin, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlImin
     */
    public Float getCtrlImin() {
        return ctrlImin;
    }

    /**
     * Returns the ctrlImax, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlImax
     */
    public Float getCtrlImax() {
        return ctrlImax;
    }

    /**
     * Returns the ctrlYmin, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlYmin
     */
    public Float getCtrlYmin() {
        return ctrlYmin;
    }

    /**
     * Returns the ctrlYmax, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlYmax
     */
    public Float getCtrlYmax() {
        return ctrlYmax;
    }

    /**
     * Returns the ctrlAntiWindUp, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlAntiWindUp
     */
    public Boolean getCtrlAntiWindUp() {
        return ctrlAntiWindUp;
    }

    /**
     * Returns the ctrlKeepFloorWarm, if control-mode is {@link ControlModes#PID_CONTROL}, otherwise null.
     *
     * @return the ctrlKeepFloorWarm
     */
    public Boolean getCtrlKeepFloorWarm() {
        return ctrlKeepFloorWarm;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlConfig [referenceZone=" + referenceZone + ", ctrlOffset=" + ctrlOffset
                + ", manualValue=" + manualValue + ", emergencyValue=" + emergencyValue + ", ctrlKp=" + ctrlKp
                + ", ctrlTs=" + ctrlTs + ", ctrlTi=" + ctrlTi + ", ctrlKd=" + ctrlKd + ", ctrlImin=" + ctrlImin
                + ", ctrlImax=" + ctrlImax + ", ctrlYmin=" + ctrlYmin + ", ctrlYmax=" + ctrlYmax + ", ctrlAntiWindUp="
                + ctrlAntiWindUp + ", ctrlKeepFloorWarm=" + ctrlKeepFloorWarm + "]";
    }
}
