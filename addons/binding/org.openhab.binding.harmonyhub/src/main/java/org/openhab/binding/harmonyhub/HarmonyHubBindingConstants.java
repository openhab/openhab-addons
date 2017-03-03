/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HarmonyHubBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class HarmonyHubBindingConstants {

    public static final String BINDING_ID = "harmonyhub";

    // List of all Thing Type UIDs
    public final static ThingTypeUID HARMONY_HUB_THING_TYPE = new ThingTypeUID(BINDING_ID, "hub");
    public final static ThingTypeUID HARMONY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public final static String CHANNEL_CURRENT_ACTIVITY = "currentActivity";

    public final static String CHANNEL_BUTTON_PRESS = "buttonPress";

    public final static String HUB_PROPERTY_SESSIONID = "sessionId";
    public final static String HUB_PROPERTY_ACCOUNTID = "accountId";
    public final static String HUB_PROPERTY_HOST = "host";
    public final static String HUB_PROPERTY_ID = "id";

}
