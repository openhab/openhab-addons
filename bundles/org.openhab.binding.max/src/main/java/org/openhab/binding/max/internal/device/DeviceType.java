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
package org.openhab.binding.max.internal.device;

/**
 * This enumeration represents the different message types provided by the MAX! Cube protocol.
 *
 * @author Andreas Heil - Initial contribution
 */
public enum DeviceType {
    Invalid(256),
    Cube(0),
    HeatingThermostat(1),
    HeatingThermostatPlus(2),
    WallMountedThermostat(3),
    ShutterContact(4),
    EcoSwitch(5);

    private final int value;

    private DeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeviceType create(int value) {
        switch (value) {
            case 0:
                return Cube;
            case 1:
                return HeatingThermostat;
            case 2:
                return HeatingThermostatPlus;
            case 3:
                return WallMountedThermostat;
            case 4:
                return ShutterContact;
            case 5:
                return EcoSwitch;
            default:
                return Invalid;
        }
    }

    @Override
    public String toString() {
        switch (value) {
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
