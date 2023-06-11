/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.leapmotion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

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
