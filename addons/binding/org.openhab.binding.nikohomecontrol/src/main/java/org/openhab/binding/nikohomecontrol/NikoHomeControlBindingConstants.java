/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link NikoHomeControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlBindingConstants {

    public static final String BINDING_ID = "nikohomecontrol";

    // List of all Thing Type UIDs

    // bridge
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "onOff");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_BLIND = new ThingTypeUID(BINDING_ID, "blind");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_ON_OFF_LIGHT,
            THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_BLIND);

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";

    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_NOTICE = "notice";

    // Bridge config properties
    public static final String CONFIG_HOST_NAME = "addr";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_REFRESH = "refresh";

    // Thing config properties
    public static final String CONFIG_ACTION_ID = "actionId";
    public static final String CONFIG_STEP_VALUE = "step";
}
