/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.Set;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;

/**
 * Enum of the possible device types. The defined tag string can be used
 * as a tag on an item to enable it for Homekit.
 *
 * @author Andy Lintner - Initial contribution
 */
public enum HomekitAccessoryType {
    DIMMABLE_LIGHTBULB("DimmableLighting"),
    HUMIDITY_SENSOR("CurrentHumidity"),
    LIGHTBULB("Lighting"),
    SWITCH("Switchable"),
    TEMPERATURE_SENSOR("CurrentTemperature"),
    THERMOSTAT("Thermostat"),
    COLORFUL_LIGHTBULB("ColorfulLighting"),
    CONTACT_SENSOR("ContactSensor"),
    VALVE("Valve"),
    LEAK_SENSOR("LeakSensor"),
    MOTION_SENSOR("MotionSensor"),
    OCCUPANCY_SENSOR("OccupancySensor"),
    WINDOW_COVERING("WindowCovering"),
    SMOKE_SENSOR("SmokeSensor"),
    CARBON_MONOXIDE_SENSOR("CarbonMonoxideSensor"),
    @Deprecated()
    BLINDS("Blinds"),
    LOCK("Lock");

    private static final Map<String, HomekitAccessoryType> TAG_MAP = new HashMap<>();

    static {
        for (HomekitAccessoryType type : HomekitAccessoryType.values()) {
            TAG_MAP.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitAccessoryType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * get accessoryType from String
     *
     * @param tag the tag string
     * @return accessoryType or null if not found
     */
    public static HomekitAccessoryType valueOfTag(String tag) {
        return TAG_MAP.get(tag);
    }

    /**
     * get accessoryType for a given Item
     *
     * @param item the item
     * @return accessoryType or null if not found
     */
    public static HomekitAccessoryType fromItem(Item item) {
        Set<String> tags = item.getTags();
        HomekitAccessoryType accessoryType = tags.stream().map(tag -> TAG_MAP.get(tag)).filter(Objects::nonNull)
                .findFirst().orElse(null);
        if (item instanceof ColorItem && accessoryType == LIGHTBULB) {
            return COLORFUL_LIGHTBULB;
        } else if (item instanceof DimmerItem && accessoryType == LIGHTBULB) {
            return DIMMABLE_LIGHTBULB;
        } else {
            return accessoryType;
        }
    }
}
