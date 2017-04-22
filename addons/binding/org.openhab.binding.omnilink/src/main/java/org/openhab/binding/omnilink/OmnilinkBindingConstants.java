/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link OmnilinkBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Craig - Initial contribution
 */
public class OmnilinkBindingConstants {

    public static final String BINDING_ID = "omnilink";

    // List of all Channel ids
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";
    public final static String CHANNEL_CONTACTSENSOR = "status";

    public final static String CHANNEL_AREAMODE = "mode";
    public final static String CHANNEL_AREAALARM = "alarm";
    public final static String CHANNEL_FLAG = "value";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "omnilinkBridge");
    public final static ThingTypeUID THING_TYPE_AREA = new ThingTypeUID(BINDING_ID, "area");
    public final static ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public final static ThingTypeUID THING_TYPE_UNIT = new ThingTypeUID(BINDING_ID, "unit");
    public final static ThingTypeUID THING_TYPE_FLAG = new ThingTypeUID(BINDING_ID, "flag");

    public final static ChannelTypeUID CHANNEL_TYPE_FLAG = new ChannelTypeUID(BINDING_ID, CHANNEL_FLAG);

}
