/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

    // Maximum number of GC devices supported by the binding
    public final static int MAX_GC_DEVICES = 10;

    /*
     * GlobalCache thing definitions
     */
    // GlobalCache Thing Type UIDs -- IR = Infrared, CC = Contact Closure, SL = Serial
    public final static ThingTypeUID THING_TYPE_ITACH_IR = new ThingTypeUID(BINDING_ID, "itachIR");
    public final static ThingTypeUID THING_TYPE_ITACH_CC = new ThingTypeUID(BINDING_ID, "itachCC");
    public final static ThingTypeUID THING_TYPE_ITACH_SL = new ThingTypeUID(BINDING_ID, "itachSL");
    public final static ThingTypeUID THING_TYPE_ITACH_FLEX = new ThingTypeUID(BINDING_ID, "itachFlex");
    public final static ThingTypeUID THING_TYPE_GC_100_06 = new ThingTypeUID(BINDING_ID, "gc100_06");
    public final static ThingTypeUID THING_TYPE_GC_100_12 = new ThingTypeUID(BINDING_ID, "gc100_12");
    public final static ThingTypeUID THING_TYPE_UNKNOWN = new ThingTypeUID(BINDING_ID, "unknown");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ITACH_IR,
            THING_TYPE_ITACH_CC, THING_TYPE_ITACH_SL, THING_TYPE_ITACH_FLEX, THING_TYPE_GC_100_06, THING_TYPE_GC_100_12,
            THING_TYPE_UNKNOWN);

    // GlobalCache-specific thing properties
    public final static String THING_PROPERTY_UID = "uid";
    public final static String THING_PROPERTY_MAC = "macAddress";

    /*
     * GlobalCache thing configuration items
     */
    // Network address of the device
    public final static String THING_PROPERTY_IP = "ipAddress";

    // MAP file containing mappings from command to IR and SL string
    public final static String THING_CONFIG_MAP_FILENAME = "mapFilename";

    // Flex "Current Active Cable"
    public final static String THING_CONFIG_ACTIVECABLE = "activeCable";

    public final static String ACTIVE_CABLE_INFRARED = "FLEX_INFRARED";
    public final static String ACTIVE_CABLE_SERIAL = "FLEX_SERIAL";
    public final static String ACTIVE_CABLE_RELAY = "FLEX_RELAY";

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
    public final static String CHANNEL_TYPE_IR = "channel-type-ir";
    public final static String CHANNEL_TYPE_CC = "channel-type-cc";
    public final static String CHANNEL_TYPE_SL = "channel-type-sl";
    public final static String CHANNEL_TYPE_SL_DIRECT = "channel-type-sl-direct";

    // Channel properties that are used to specify module number and connector number
    public final static String CHANNEL_PROPERTY_MODULE = "module";
    public final static String CHANNEL_PROPERTY_CONNECTOR = "connector";

    // List of iTach model strings returned in the device discovery beacon
    public final static String GC_MODEL_ITACH = "iTach";
    public final static String GC_MODEL_ITACHIP2IR = "iTachIP2IR";
    public final static String GC_MODEL_ITACHWF2IR = "iTachWF2IR";
    public final static String GC_MODEL_ITACHIP2CC = "iTachIP2CC";
    public final static String GC_MODEL_ITACHWF2CC = "iTachWF2CC";
    public final static String GC_MODEL_ITACHIP2SL = "iTachIP2SL";
    public final static String GC_MODEL_ITACHWF2SL = "iTachWF2SL";
    public final static String GC_MODEL_ITACHFLEXETH = "iTachFlexEthernet";
    public final static String GC_MODEL_ITACHFLEXETHPOE = "iTachFlexEthernetPoE";
    public final static String GC_MODEL_ITACHFLEXWIFI = "iTachFlexWiFi";

    // List of GC-100 model strings returned in the device discovery beacon
    public final static String GC_MODEL_GC_100 = "GC-100";
    public final static String GC_MODEL_GC_100_06 = "GC-100-06";
    public final static String GC_MODEL_GC_100_12 = "GC-100-12";
}
