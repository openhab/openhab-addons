/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.jeelink.internal.SensorDefinition;

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

    // List of all channel ids for lacrosse sensor things
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String BATTERY_NEW_CHANNEL = "batteryNew";
    public static final String BATTERY_LOW_CHANNEL = "batteryLow";

    public static final String PROPERTY_SENSOR_ID = "sensorId";

    // List of all additional channel ids for ec3k sensor things
    public static final String CURRENT_WATT_CHANNEL = "currentWatt";
    public static final String MAX_WATT_CHANNEL = "maxWatt";
    public static final String CONSUMPTION_CHANNEL = "consumptionTotal";
    public static final String APPLIANCE_TIME_CHANNEL = "applianceTime";
    public static final String SENSOR_TIME_CHANNEL = "sensorTime";
    public static final String RESETS_CHANNEL = "resets";

    // List of all additional channel ids for pca301 sensor things
    public static final String SWITCHING_STATE_CHANNEL = "switchingState";

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
