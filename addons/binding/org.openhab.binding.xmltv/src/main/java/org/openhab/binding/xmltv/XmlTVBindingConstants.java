/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link XmlTVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class XmlTVBindingConstants {

    private static final String BINDING_ID = "xmltv";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID XMLTV_FILE_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "XmlTVFile");
    public static final ThingTypeUID XMLTV_CHANNEL_THING_TYPE = new ThingTypeUID(BINDING_ID, "Channel");

    // Channel groups
    public static final String GROUP_CURRENT_PROGRAMME = "currentprog";
    public static final String GROUP_NEXT_PROGRAMME = "nextprog";
    public static final String GROUP_CHANNEL_PROPERTIES = "channelprops";

    // List of all Channel ids
    public static final String CHANNEL_CHANNEL_URL = "iconUrl";
    public static final String CHANNEL_ICON = "icon";

    public static final String CHANNEL_PROGRAM_START = "progStart";
    public static final String CHANNEL_PROGRAM_END = "progEnd";
    public static final String CHANNEL_PROGRAM_TITLE = "progTitle";
    public static final String CHANNEL_PROGRAM_CATEGORY = "progCategory";
    public static final String CHANNEL_PROGRAM_ICON = "progIcon";

    public static final String CHANNEL_PROGRAM_ELAPSED = "elapsedTime";
    public static final String CHANNEL_PROGRAM_REMAINING = "remainingTime";
    public static final String CHANNEL_PROGRAM_PROGRESS = "progress";
    public static final String CHANNEL_PROGRAM_TIMELEFT = "timeLeft";

    // Supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(XMLTV_FILE_BRIDGE_TYPE,
            XMLTV_CHANNEL_THING_TYPE);
}
