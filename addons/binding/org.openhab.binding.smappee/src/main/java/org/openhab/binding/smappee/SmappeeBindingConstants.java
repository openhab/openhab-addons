/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SmappeeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeBindingConstants {

    public static final String BINDING_ID = "smappee";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SMAPPEE = new ThingTypeUID(BINDING_ID, "smappee");

    // List of all Channel ids
    public final static String CHANNEL_CONSUMPTION = "channelconsumption";
    public final static String CHANNEL_SOLAR = "channelsolar";
    public final static String CHANNEL_ALWAYSON = "channelalwayson";

    // List of all Parameters
    public final static String PARAMETER_CLIENT_ID = "client_id";
    public final static String PARAMETER_CLIENT_SECRET = "client_secret";
    public final static String PARAMETER_USERNAME = "username";
    public final static String PARAMETER_PASSWORD = "password";
    public final static String PARAMETER_SERVICE_LOCATION_NAME = "servicelocationname";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SMAPPEE);
}
