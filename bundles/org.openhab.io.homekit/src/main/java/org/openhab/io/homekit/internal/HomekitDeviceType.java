/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum of the possible device types. The defined tag string can be used
 * as a tag on an item to enable it for Homekit.
 *
 * @author Andy Lintner - Initial contribution
 */
public enum HomekitDeviceType {
    DIMMABLE_LIGHTBULB("DimmableLighting"),
    HUMIDITY_SENSOR("CurrentHumidity"),
    LIGHTBULB("Lighting"),
    SWITCH("Switchable"),
    TEMPERATURE_SENSOR("CurrentTemperature"),
    THERMOSTAT("Thermostat"),
    COLORFUL_LIGHTBULB("ColorfulLighting"),
    CONTACT_SENSOR("ContactSensor");

    private static final Map<String, HomekitDeviceType> TAG_MAP = new HashMap<>();

    static {
        for (HomekitDeviceType type : HomekitDeviceType.values()) {
            TAG_MAP.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitDeviceType(String tag) {
        this.tag = tag;
    }

    public static HomekitDeviceType valueOfTag(String tag) {
        return TAG_MAP.get(tag);
    }
}
