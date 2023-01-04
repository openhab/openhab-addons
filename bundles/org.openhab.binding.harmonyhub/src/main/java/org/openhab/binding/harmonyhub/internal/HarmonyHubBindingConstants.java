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
package org.openhab.binding.harmonyhub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HarmonyHubBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add device properties
 */
@NonNullByDefault
public class HarmonyHubBindingConstants {

    public static final String BINDING_ID = "harmonyhub";

    // List of all Thing Type UIDs
    public static final ThingTypeUID HARMONY_HUB_THING_TYPE = new ThingTypeUID(BINDING_ID, "hub");
    public static final ThingTypeUID HARMONY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel IDs
    public static final String CHANNEL_CURRENT_ACTIVITY = "currentActivity";
    public static final String CHANNEL_ACTIVITY_STARTING_TRIGGER = "activityStarting";
    public static final String CHANNEL_ACTIVITY_STARTED_TRIGGER = "activityStarted";

    public static final String CHANNEL_BUTTON_PRESS = "buttonPress";
    public static final String CHANNEL_PLAYER = "player";

    public static final String DEVICE_PROPERTY_ID = "id";
    public static final String DEVICE_PROPERTY_NAME = "name";

    public static final String HUB_PROPERTY_ID = "uuid";
    public static final String HUB_PROPERTY_HOST = "host";
    public static final String HUB_PROPERTY_NAME = "name";
}
