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
 * The {@link TemperatureControlInternals} acts as container for the digitalSTROM json-method
 * <i>getTemperatureControlInternals</i>. So the {@link TemperatureControlInternals} contains all internal heating
 * control configurations of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlInternals extends BaseTemperatureControl {

    private Short controlState;
    private Float ctrlTRecent;
    private Float ctrlTReference;
    private Float ctrlTError;
    private Float ctrlTErrorPrev;
    private Float ctrlIntegral;
    private Float ctrlYp;
    private Float ctrlYi;
    private Float ctrlYd;
    private Float ctrlY;
    private Short ctrlAntiWindUp;

    /**
     * Creates a new {@link TemperatureControlInternals} through the {@link JsonObject} which will be returned by an
     * zone
     * call.<br>
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public TemperatureControlInternals(JsonObject jObject, Integer zoneID, String zoneName) {
        super(jObject, zoneID, zoneName);
        if (isNotSetOff()) {
            if (jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()) != null) {
                this.controlState = jObject.get(JSONApiResponseKeysEnum.CONTROL_STATE.getKey()).getAsShort();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_RECENT.getKey()) != null) {
                this.ctrlTRecent = jObject.get(JSONApiResponseKeysEnum.CTRL_T_RECENT.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_REFERENCE.getKey()) != null) {
                this.ctrlTReference = jObject.get(JSONApiResponseKeysEnum.CTRL_T_REFERENCE.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR.getKey()) != null) {
                this.ctrlTError = jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR_PREV.getKey()) != null) {
                this.ctrlTErrorPrev = jObject.get(JSONApiResponseKeysEnum.CTRL_T_ERROR_PREV.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_INTEGRAL.getKey()) != null) {
                this.ctrlIntegral = jObject.get(JSONApiResponseKeysEnum.CTRL_INTEGRAL.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YP.getKey()) != null) {
                this.ctrlY = jObject.get(JSONApiResponseKeysEnum.CTRL_YP.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YI.getKey()) != null) {
                this.ctrlYi = jObject.get(JSONApiResponseKeysEnum.CTRL_YI.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_YD.getKey()) != null) {
                this.ctrlYd = jObject.get(JSONApiResponseKeysEnum.CTRL_YD.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_Y.getKey()) != null) {
                this.ctrlY = jObject.get(JSONApiResponseKeysEnum.CTRL_Y.getKey()).getAsFloat();
            }
            if (jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()) != null) {
                this.ctrlAntiWindUp = jObject.get(JSONApiResponseKeysEnum.CTRL_ANTI_WIND_UP.getKey()).getAsShort();
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
     * Returns the ctrlTRecent for heating of the zone.
     *
     * @return the ctrlTRecent
     */
    public Float getCtrlTRecent() {
        return ctrlTRecent;
    }

    /**
     * Returns the ctrlTReference for heating of the zone.
     *
     * @return the ctrlTReference
     */
    public Float getCtrlTReference() {
        return ctrlTReference;
    }

    /**
     * Returns the ctrlTError for heating of the zone.
     *
     * @return the ctrlTError
     */
    public Float getCtrlTError() {
        return ctrlTError;
    }

    /**
     * Returns the ctrlTErrorPrev for heating of the zone.
     *
     * @return the ctrlTErrorPrev
     */
    public Float getCtrlTErrorPrev() {
        return ctrlTErrorPrev;
    }

    /**
     * Returns the ctrlIntegral for heating of the zone.
     *
     * @return the ctrlIntegral
     */
    public Float getCtrlIntegral() {
        return ctrlIntegral;
    }

    /**
     * Returns the ctrlYp for heating of the zone.
     *
     * @return the ctrlYp
     */
    public Float getCtrlYp() {
        return ctrlYp;
    }

    /**
     * Returns the ctrlYi for heating of the zone.
     *
     * @return the ctrlYi
     */
    public Float getCtrlYi() {
        return ctrlYi;
    }

    /**
     * Returns the ctrlYd for heating of the zone.
     *
     * @return the ctrlYd
     */
    public Float getCtrlYd() {
        return ctrlYd;
    }

    /**
     * Returns the ctrlY for heating of the zone.
     *
     * @return the ctrlY
     */
    public Float getCtrlY() {
        return ctrlY;
    }

    /**
     * Returns the ctrlAntiWindUp for heating of the zone.
     *
     * @return the ctrlAntiWindUp
     */
    public Short getCtrlAntiWindUp() {
        return ctrlAntiWindUp;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TemperatureControlInternals [controlState=" + controlState + ", ctrlTRecent=" + ctrlTRecent
                + ", ctrlTReference=" + ctrlTReference + ", ctrlTError=" + ctrlTError + ", ctrlTErrorPrev="
                + ctrlTErrorPrev + ", ctrlIntegral=" + ctrlIntegral + ", ctrlYp=" + ctrlYp + ", ctrlYi=" + ctrlYi
                + ", ctrlYd=" + ctrlYd + ", ctrlY=" + ctrlY + ", ctrlAntiWindUp=" + ctrlAntiWindUp + "]";
    }
}
