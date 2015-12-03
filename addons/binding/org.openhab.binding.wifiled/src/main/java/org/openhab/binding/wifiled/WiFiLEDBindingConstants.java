/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WiFiLEDBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
public class WiFiLEDBindingConstants {

    public static final String BINDING_ID = "wifiled";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WIFILED = new ThingTypeUID(BINDING_ID, "wifiled");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_WIFILED);

    // List of all Channel IDs
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_WHITE = "white";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_PROGRAM_SPEED = "programSpeed";

}
