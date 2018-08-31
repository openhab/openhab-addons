/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal;


import com.google.common.collect.Sets;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import java.util.Set;

/**
 * Binding constants
 *
 * @author Gabor Bicskei - - Initial contribution
 */
public abstract class BindingConstants {
    private static final String BINDING_ID = "gpstracker";
    static final String CONFIG_PID = "binding." + BINDING_ID;
    private static final String THING_TYPE = "tracker";
    public static final ThingTypeUID THING_TYPE_TRACKER = new ThingTypeUID(BINDING_ID, THING_TYPE);

    //channels
    public static final String CHANNEL_REGION_PRESENCE = "regionPresence";
    public static final String CHANNEL_REGION_ENTER_TRIGGER = "regionEnterTrigger";
    public static final String CHANNEL_REGION_LEAVE_TRIGGER = "regionLeaveTrigger";
    public static final String CHANNEL_LAST_REPORT = "lastReport";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_DISTANCE = "distance";

    public static final ChannelTypeUID CHANNEL_TYPE_PRESENCE = new ChannelTypeUID(BINDING_ID, CHANNEL_REGION_PRESENCE);
    public static final ChannelTypeUID CHANNEL_TYPE_ENTER_TRIGGER = new ChannelTypeUID(BINDING_ID, CHANNEL_REGION_ENTER_TRIGGER);
    public static final ChannelTypeUID CHANNEL_TYPE_LEAVE_TRIGGER = new ChannelTypeUID(BINDING_ID, CHANNEL_REGION_LEAVE_TRIGGER);
    public static final ChannelTypeUID CHANNEL_TYPE_DISTANCE = new ChannelTypeUID(BINDING_ID, CHANNEL_DISTANCE);

    //config
    static final String CONFIG_LOCATION = "location";
    static final String CONFIG_NAME = "name";
    static final String CONFIG_RADIUS = "radius";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_TRACKER);

    //translation
    public static final String TRANSLATION_PRESENCE = "channel.gpstracker.presence.label";
    public static final String TRANSLATION_DISTANCE = "channel.gpstracker.distance.label";
}
