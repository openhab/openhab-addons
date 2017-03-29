/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link TellstickBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author jarlebh - Initial contribution
 */
public class TellstickBindingConstants {

    public static final String BINDING_ID = "tellstick";

    public static final String CONFIGPATH_ID = "location";
    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_PROTOCOL = "protocol";
    public static final String DEVICE_MODEL = "model";
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_RESEND_COUNT = "repeat";
    public static final String DEVICE_ISDIMMER = "dimmer";
    public static final String BRIDGE_TELLDUS_CORE = "telldus-core";
    public static final String BRIDGE_TELLDUS_LIVE = "telldus-live";
    public static final String DEVICE_SENSOR = "sensor";
    public static final String DEVICE_WINDSENSOR = "windsensor";
    public static final String DEVICE_RAINSENSOR = "rainsensor";
    public static final String DEVICE_DIMMER = "dimmer";
    public static final String DEVICE_SWITCH = "switch";
    // List of all Thing Type UIDs
    public final static ThingTypeUID DIMMER_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DIMMER);
    public final static ThingTypeUID SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SWITCH);
    public final static ThingTypeUID SENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SENSOR);
    public final static ThingTypeUID RAINSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_RAINSENSOR);
    public final static ThingTypeUID WINDSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_WINDSENSOR);

    public final static ThingTypeUID TELLDUSBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_CORE);
    public final static ThingTypeUID TELLDUSCOREBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_CORE);
    public final static ThingTypeUID TELLDUSLIVEBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_TELLDUS_LIVE);
    // List of all Channel ids
    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_STATE = "state";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_RAINTOTAL = "raintotal";
    public final static String CHANNEL_RAINRATE = "rainrate";
    public final static String CHANNEL_WINDAVERAGE = "windaverage";
    public final static String CHANNEL_WINDDIRECTION = "winddirection";
    public final static String CHANNEL_WINDGUST = "windgust";

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Sets
            .newHashSet(TELLDUSCOREBRIDGE_THING_TYPE, TELLDUSLIVEBRIDGE_THING_TYPE);
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Sets.newHashSet(DIMMER_THING_TYPE,
            SWITCH_THING_TYPE, SENSOR_THING_TYPE, RAINSENSOR_THING_TYPE, WINDSENSOR_THING_TYPE);
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(DIMMER_THING_TYPE,
            SWITCH_THING_TYPE, SENSOR_THING_TYPE, RAINSENSOR_THING_TYPE, WINDSENSOR_THING_TYPE,
            TELLDUSCOREBRIDGE_THING_TYPE, TELLDUSLIVEBRIDGE_THING_TYPE);

}
