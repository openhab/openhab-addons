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
package org.openhab.binding.elkm1.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ElkAlarmBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */

@NonNullByDefault
public class ElkM1BindingConstants {

    public static final String BINDING_ID = "elkm1";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_AREA = new ThingTypeUID(BINDING_ID, "area");

    // List of all Channel ids (zone)
    public static final String CHANNEL_ZONE_AREA = "area";
    public static final String CHANNEL_ZONE_CONFIG = "config";
    public static final String CHANNEL_ZONE_STATUS = "status";
    public static final String CHANNEL_ZONE_DEFINITION = "definition";

    // List of all Channel ids (area)
    public static final String CHANNEL_AREA_STATE = "state";
    public static final String CHANNEL_AREA_ARMUP = "armup";
    public static final String CHANNEL_AREA_ARMED = "armed";
    public static final String CHANNEL_AREA_COMMAND = "command"; // mlm

    // The properties associated with the thing
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_ZONE_NUM = "zone";
    public static final String PROPERTY_TYPE_ID = "type";

}
