/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.globalcache;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link GlobalCacheBindingConstants} class defines common constants that are
 * used by the globalcache binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class GlobalCacheBindingConstants {

    public static final String BINDING_ID = "globalcache";

    /*
     * GlobalCache thing definitions
     */
    // GlobalCache Thing Type UIDs -- IR = Infrared, CC = Contact Closure, SL = Serial
    public static final ThingTypeUID THING_TYPE_ITACH_IR = new ThingTypeUID(BINDING_ID, "itachIR");
    public static final ThingTypeUID THING_TYPE_ITACH_CC = new ThingTypeUID(BINDING_ID, "itachCC");
    public static final ThingTypeUID THING_TYPE_ITACH_SL = new ThingTypeUID(BINDING_ID, "itachSL");
    public static final ThingTypeUID THING_TYPE_ITACH_FLEX = new ThingTypeUID(BINDING_ID, "itachFlex");
    public static final ThingTypeUID THING_TYPE_GC_100_06 = new ThingTypeUID(BINDING_ID, "gc100_06");
    public static final ThingTypeUID THING_TYPE_GC_100_12 = new ThingTypeUID(BINDING_ID, "gc100_12");
    public static final ThingTypeUID THING_TYPE_ZMOTE = new ThingTypeUID(BINDING_ID, "zmote");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ITACH_IR,
            THING_TYPE_ITACH_CC, THING_TYPE_ITACH_SL, THING_TYPE_ITACH_FLEX, THING_TYPE_GC_100_06, THING_TYPE_GC_100_12,
            THING_TYPE_ZMOTE);

    // GlobalCache-specific thing properties
    public static final String THING_PROPERTY_UID = "uid";
    public static final String THING_PROPERTY_MAC = "macAddress";

    /*
     * GlobalCache thing configuration items
     */
    // Network address of the device
    public static final String THING_PROPERTY_IP = "ipAddress";

    // MAP file containing mappings from command to IR and SL string
    public static final String THING_CONFIG_MAP_FILENAME = "mapFilename";

    // Flex "Current Active Cable"
    public static final String THING_CONFIG_ACTIVECABLE = "activeCable";

    public static final String ACTIVE_CABLE_INFRARED = "FLEX_INFRARED";
    public static final String ACTIVE_CABLE_SERIAL = "FLEX_SERIAL";
    public static final String ACTIVE_CABLE_RELAY = "FLEX_RELAY";

    // Serial readers
    public static final String CONFIG_ENABLE_TWO_WAY_PORT_1 = "enableTwoWay1";
    public static final String CONFIG_END_OF_MESSAGE_DELIMITER_PORT_1 = "eomDelimiter1";
    public static final String CONFIG_ENABLE_TWO_WAY_PORT_2 = "enableTwoWay2";
    public static final String CONFIG_END_OF_MESSAGE_DELIMITER_PORT_2 = "eomDelimiter2";

    // Indicates TCP connection over which the command will be sent
    public enum CommandType {
        COMMAND,
        SERIAL1,
        SERIAL2
    }

    /*
     * Channel constants
     */
    // GlobalCache Channel Types
    public static final String CHANNEL_TYPE_IR = "channel-type-ir";
    public static final String CHANNEL_TYPE_CC = "channel-type-cc";
    public static final String CHANNEL_TYPE_SL = "channel-type-sl";
    public static final String CHANNEL_TYPE_SL_DIRECT = "channel-type-sl-direct";
    public static final String CHANNEL_TYPE_SL_FEEDBACK = "channel-type-sl-receive";

    // Channels for handing feedback from serial devices
    public static final String CHANNEL_SL_M1_RECEIVE = "sl-m1#c1-receive";
    public static final String CHANNEL_SL_M2_RECEIVE = "sl-m2#c1-receive";

    // Channel properties that are used to specify module number and connector number
    public static final String CHANNEL_PROPERTY_MODULE = "module";
    public static final String CHANNEL_PROPERTY_CONNECTOR = "connector";

    // List of iTach model strings returned in the device discovery beacon
    public static final String GC_MODEL_ITACH = "iTach";
    public static final String GC_MODEL_ITACHIP2IR = "iTachIP2IR";
    public static final String GC_MODEL_ITACHWF2IR = "iTachWF2IR";
    public static final String GC_MODEL_ITACHIP2CC = "iTachIP2CC";
    public static final String GC_MODEL_ITACHWF2CC = "iTachWF2CC";
    public static final String GC_MODEL_ITACHIP2SL = "iTachIP2SL";
    public static final String GC_MODEL_ITACHWF2SL = "iTachWF2SL";
    public static final String GC_MODEL_ITACHFLEXETH = "iTachFlexEthernet";
    public static final String GC_MODEL_ITACHFLEXETHPOE = "iTachFlexEthernetPoE";
    public static final String GC_MODEL_ITACHFLEXWIFI = "iTachFlexWiFi";

    // List of GC-100 model strings returned in the device discovery beacon
    public static final String GC_MODEL_GC_100 = "GC-100";
    public static final String GC_MODEL_GC_100_06 = "GC-100-06";
    public static final String GC_MODEL_GC_100_12 = "GC-100-12";

    // List of Zmote strings returned in the device discovery beacon
    public static final String GC_MODEL_ZMOTE = "ZV-2";
}
