/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ElkAlarmBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class ElkM1BindingConstants {

    public static final String BINDING_ID = "elkm1";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public final static ThingTypeUID THING_TYPE_AREA = new ThingTypeUID(BINDING_ID, "area");

    // List of all Channel ids (zone)
    public final static String CHANNEL_ZONE_AREA = "area";
    public final static String CHANNEL_ZONE_CONFIG = "config";
    public final static String CHANNEL_ZONE_STATUS = "status";
    public final static String CHANNEL_ZONE_DEFINITION = "definition";

    // List of all Channel ids (area)
    public static final String CHANNEL_AREA_STATE = "state";
    public static final String CHANNEL_AREA_ARMUP = "armup";
    public static final String CHANNEL_AREA_ARMED = "armed";

    // The properties associated with the thing
    public final static String PROPERTY_VERSION = "version";
    public final static String PROPERTY_ZONE_NUM = "zone";
    public final static String PROPERTY_TYPE_ID = "type";

}
