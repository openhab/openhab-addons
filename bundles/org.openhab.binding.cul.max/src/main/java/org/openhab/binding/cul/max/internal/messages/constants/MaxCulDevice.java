/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages.constants;

import static org.openhab.binding.cul.max.internal.MaxCulBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Define device types
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public enum MaxCulDevice {
    CUBE(0),
    RADIATOR_THERMOSTAT(1),
    RADIATOR_THERMOSTAT_PLUS(2),
    WALL_THERMOSTAT(3),
    SHUTTER_CONTACT(4),
    PUSH_BUTTON(5),
    UNKNOWN(0xff); // not official MAX!

    private final int devType;

    private MaxCulDevice(int idx) {
        devType = idx;
    }

    public int getDeviceTypeInt() {
        return devType;
    }

    public static MaxCulDevice getDeviceTypeFromInt(int idx) {
        for (int i = 0; i < MaxCulDevice.values().length; i++) {
            if (MaxCulDevice.values()[i].getDeviceTypeInt() == idx) {
                return MaxCulDevice.values()[i];
            }
        }
        return UNKNOWN;
    }

    public static MaxCulDevice getDeviceTypeFromThingTypeUID(ThingTypeUID thingTypeUID) {
        if (HEATINGTHERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return RADIATOR_THERMOSTAT;
        } else if (HEATINGTHERMOSTATPLUS_THING_TYPE.equals(thingTypeUID)) {
            return RADIATOR_THERMOSTAT_PLUS;
        } else if (WALLTHERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return WALL_THERMOSTAT;
        } else if (ECOSWITCH_THING_TYPE.equals(thingTypeUID)) {
            return PUSH_BUTTON;
        } else if (SHUTTERCONTACT_THING_TYPE.equals(thingTypeUID)) {
            return SHUTTER_CONTACT;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        switch (devType) {
            case 0:
                return "Cube";
            case 1:
                return "Thermostat";
            case 2:
                return "Thermostat+";
            case 3:
                return "Wallmounted Thermostat";
            case 4:
                return "Shutter Contact";
            case 5:
                return "Eco Switch";
            default:
                return "Invalid";
        }
    }
}
