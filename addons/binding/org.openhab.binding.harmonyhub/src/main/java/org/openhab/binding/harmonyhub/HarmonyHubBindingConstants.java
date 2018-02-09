/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HarmonyHubBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HarmonyHubBindingConstants {

    public static final String BINDING_ID = "harmonyhub";

    // List of all Thing Type UIDs
    public static final ThingTypeUID HARMONY_HUB_THING_TYPE = new ThingTypeUID(BINDING_ID, "hub");
    public static final ThingTypeUID HARMONY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_ACTIVITY = "currentActivity";
    public static final String CHANNEL_ACTIVITY_STARTING_TRIGGER = "activityStarting";
    public static final String CHANNEL_ACTIVITY_STARTED_TRIGGER = "activityStarted";

    public static final String CHANNEL_BUTTON_PRESS = "buttonPress";

    public static final String HUB_PROPERTY_SESSIONID = "sessionId";
    public static final String HUB_PROPERTY_ACCOUNTID = "accountId";
    public static final String HUB_PROPERTY_HOST = "host";
    public static final String HUB_PROPERTY_ID = "id";

}
