/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.openhab.binding.digitalstrom.internal.lib.climate.constants.ControlModes;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link BaseTemperatureControl} is a base implementation for temperature controls status and configurations. For
 * that it extends the {@link BaseZoneIdentifier}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public abstract class BaseTemperatureControl extends BaseZoneIdentifier {

    protected String controlDSUID;
    protected Short controlMode;

    /**
     * Creates a new {@link BaseTemperatureControl} through the {@link JsonObject} which will be returned by a zone
     * call.<br>
     * Because zone calls do not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public BaseTemperatureControl(JsonObject jObject, Integer zoneID, String zoneName) {
        super(zoneID, zoneName);
        init(jObject);
    }

    /**
     * Creates a new {@link BaseTemperatureControl} through the {@link JsonObject} which will be returned by an
     * apartment call.
     *
     * @param jObject must not be null
     */
    public BaseTemperatureControl(JsonObject jObject) {
        super(jObject);
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()) != null) {
            this.controlMode = jObject.get(JSONApiResponseKeysEnum.CONTROL_MODE.getKey()).getAsShort();
        }
        if (jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()) != null) {
            this.controlDSUID = jObject.get(JSONApiResponseKeysEnum.CONTROL_DSUID.getKey()).getAsString();
        }
    }

    /**
     * Returns the dSUID of the control sensor for heating of the zone.
     *
     * @return the controlDSUID
     */
    public String getControlDSUID() {
        return controlDSUID;
    }

    /**
     * Returns controlMode for heating of the zone.
     *
     * @return the controlMode
     */
    public Short getControlMode() {
        return controlMode;
    }

    /**
     * Returns true, if heating for this zone is not set off (set {@link ControlModes} = {@link ControlModes#OFF}),
     * otherwise false.
     *
     * @return true, if the set {@link ControlModes} is not {@link ControlModes#OFF}
     */
    public Boolean isNotSetOff() {
        return !ControlModes.OFF.getID().equals(controlMode);
    }
}
