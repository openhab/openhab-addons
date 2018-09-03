/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leapmotion;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link LeapMotionBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class LeapMotionBindingConstants {

    public static final String BINDING_ID = "leapmotion";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");

    // List of all Channel ids
    public static final String CHANNEL_GESTURE = "gesture";
    public static final ChannelTypeUID CHANNEL_GESTURE_UID = new ChannelTypeUID(BINDING_ID, CHANNEL_GESTURE);

    // List of all gestures
    public static final String GESTURE_TAP = "tap";
    public static final String GESTURE_CLOCKWISE = "clockwise";
    public static final String GESTURE_ANTICLOCKWISE = "anticlockwise";
    public static final String GESTURE_FINGERS = "fingers";
    public static final String GESTURE_NOHAND = "nohand";

}
