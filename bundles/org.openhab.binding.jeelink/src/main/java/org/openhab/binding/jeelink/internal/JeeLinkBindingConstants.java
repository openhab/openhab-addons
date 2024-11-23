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
package org.openhab.binding.jeelink.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Volker Bier - Initial contribution
 */
@NonNullByDefault
public class JeeLinkBindingConstants {

    private JeeLinkBindingConstants() {
    }

    public static final String BINDING_ID = "jeelink";

    // List of all Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    public static final Set<ThingTypeUID> SUPPORTED_SENSOR_THING_TYPES_UIDS = new HashSet<>();

    public static final ThingTypeUID JEELINK_USB_STICK_THING_TYPE = new ThingTypeUID(BINDING_ID, "jeelinkUsb");
    public static final ThingTypeUID JEELINK_TCP_STICK_THING_TYPE = new ThingTypeUID(BINDING_ID, "jeelinkTcp");
    public static final ThingTypeUID LGW_USB_STICK_THING_TYPE = new ThingTypeUID(BINDING_ID, "lgwUsb");
    public static final ThingTypeUID LGW_TCP_STICK_THING_TYPE = new ThingTypeUID(BINDING_ID, "lgwTcp");
    public static final ThingTypeUID LACROSSE_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "lacrosse");
    public static final ThingTypeUID EC3000_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "ec3k");
    public static final ThingTypeUID PCA301_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "pca301");
    public static final ThingTypeUID EMT7110_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "emt7110");
    public static final ThingTypeUID TX22_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "tx22");
    public static final ThingTypeUID REVOLT_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "revolt");
    public static final ThingTypeUID LGW_SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "lgw");

    // List of all channel ids for lacrosse sensor things
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String BATTERY_NEW_CHANNEL = "batteryNew";
    public static final String BATTERY_LOW_CHANNEL = "batteryLow";

    public static final String PROPERTY_SENSOR_ID = "sensorId";

    // List of all additional channel ids for ec3k sensor things
    public static final String CURRENT_POWER_CHANNEL = "currentPower";
    public static final String MAX_POWER_CHANNEL = "maxPower";
    public static final String CONSUMPTION_CHANNEL = "consumptionTotal";
    public static final String APPLIANCE_TIME_CHANNEL = "applianceTime";
    public static final String SENSOR_TIME_CHANNEL = "sensorTime";
    public static final String RESETS_CHANNEL = "resets";

    // List of all additional channel ids for pca301 sensor things
    public static final String SWITCHING_STATE_CHANNEL = "switchingState";

    // List of all additional channel ids for revolt sensor things
    public static final String POWER_FACTOR_CHANNEL = "powerFactor";
    public static final String ELECTRIC_CURRENT_CHANNEL = "electricCurrent";
    public static final String ELECTRIC_POTENTIAL_CHANNEL = "electricPotential";
    public static final String FREQUENCY_CHANNEL = "powerFrequency";

    // List of all additional channel ids for tx22 sensor things
    public static final String PRESSURE_CHANNEL = "pressure";
    public static final String RAIN_CHANNEL = "rain";
    public static final String WIND_STENGTH_CHANNEL = "windStrength";
    public static final String WIND_ANGLE_CHANNEL = "windAngle";
    public static final String GUST_STRENGTH_CHANNEL = "gustStrength";

    static {
        for (SensorDefinition<?> def : SensorDefinition.getDefinitions()) {
            SUPPORTED_SENSOR_THING_TYPES_UIDS.add(def.getThingTypeUID());
        }

        SUPPORTED_THING_TYPES_UIDS.add(JeeLinkBindingConstants.JEELINK_USB_STICK_THING_TYPE);
        SUPPORTED_THING_TYPES_UIDS.add(JeeLinkBindingConstants.JEELINK_TCP_STICK_THING_TYPE);
        SUPPORTED_THING_TYPES_UIDS.add(JeeLinkBindingConstants.LGW_USB_STICK_THING_TYPE);
        SUPPORTED_THING_TYPES_UIDS.add(JeeLinkBindingConstants.LGW_TCP_STICK_THING_TYPE);
        SUPPORTED_THING_TYPES_UIDS.addAll(SUPPORTED_SENSOR_THING_TYPES_UIDS);
    }
}
