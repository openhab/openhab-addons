/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

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
    public static final ThingTypeUID XMLTV_FILE_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "xmltvfile");
    public static final ThingTypeUID XMLTV_CHANNEL_THING_TYPE = new ThingTypeUID(BINDING_ID, "channel");

    // Channel groups
    public static final String GROUP_NEXT_PROGRAMME = "nextprog";
    public static final String GROUP_CHANNEL_PROPERTIES = "channelprops";

    // List of all Channel ids
    public static final String CHANNEL_CHANNEL_URL = "iconUrl";
    public static final String CHANNEL_ICON = "icon";

    public static final String CHANNEL_PROGRAMME_START = "progStart";
    public static final String CHANNEL_PROGRAMME_END = "progEnd";
    public static final String CHANNEL_PROGRAMME_TITLE = "progTitle";
    public static final String CHANNEL_PROGRAMME_CATEGORY = "progCategory";
    public static final String CHANNEL_PROGRAMME_ICON = "progIconUrl";

    public static final String CHANNEL_PROGRAMME_ELAPSED = "elapsedTime";
    public static final String CHANNEL_PROGRAMME_REMAINING = "remainingTime";
    public static final String CHANNEL_PROGRAMME_PROGRESS = "progress";
    public static final String CHANNEL_PROGRAMME_TIMELEFT = "timeLeft";

    // Supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(XMLTV_FILE_BRIDGE_TYPE,
            XMLTV_CHANNEL_THING_TYPE);
}
