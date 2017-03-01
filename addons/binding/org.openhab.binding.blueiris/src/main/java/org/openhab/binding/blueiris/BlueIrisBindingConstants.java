/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link blueirisBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class BlueIrisBindingConstants {

    public static final String BINDING_ID = "blueiris";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");

    // List of all Channel ids
    public final static String CHANNEL_NAME = "name";
    public final static String CHANNEL_ENABLED = "enabled";

    // Properties.
    public final static String PROPERTY_SHORT_NAME = "short_name";
}
