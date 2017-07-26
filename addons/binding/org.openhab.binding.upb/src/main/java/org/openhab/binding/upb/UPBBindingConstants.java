/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UPBBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Van Orman - Initial contribution
 * @since 2.2.0
 */
public class UPBBindingConstants {

    public static final String BINDING_ID = "upb";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_LINK = new ThingTypeUID(BINDING_ID, "link");

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

    // List of device or link properties
    public static final String DEVICE_ID = "id";
    public static final String DUPLICATE_TIMEOUT = "duplicateTimeout";

}
