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
package org.openhab.binding.vdr.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VDRBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class VDRBindingConstants {

    private static final String BINDING_ID = "vdr";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VDR = new ThingTypeUID(BINDING_ID, "vdr");

    public static final String CHANNEL_UID_POWER = "power";
    public static final String CHANNEL_UID_MESSAGE = "message";
    public static final String CHANNEL_UID_CHANNEL = "channel";
    public static final String CHANNEL_UID_CHANNEL_NAME = "channelName";
    public static final String CHANNEL_UID_VOLUME = "volume";
    public static final String CHANNEL_UID_KEYCODE = "keyCode";
    public static final String CHANNEL_UID_RECORDING = "recording";
    public static final String CHANNEL_UID_DISKUSAGE = "diskUsage";
    public static final String CHANNEL_UID_CURRENT_EVENT_TITLE = "currentEventTitle";
    public static final String CHANNEL_UID_CURRENT_EVENT_SUBTITLE = "currentEventSubTitle";
    public static final String CHANNEL_UID_CURRENT_EVENT_DURATION = "currentEventDuration";
    public static final String CHANNEL_UID_CURRENT_EVENT_BEGIN = "currentEventBegin";
    public static final String CHANNEL_UID_CURRENT_EVENT_END = "currentEventEnd";
    public static final String CHANNEL_UID_NEXT_EVENT_TITLE = "nextEventTitle";
    public static final String CHANNEL_UID_NEXT_EVENT_SUBTITLE = "nextEventSubTitle";
    public static final String CHANNEL_UID_NEXT_EVENT_DURATION = "nextEventDuration";
    public static final String CHANNEL_UID_NEXT_EVENT_BEGIN = "nextEventBegin";
    public static final String CHANNEL_UID_NEXT_EVENT_END = "nextEventEnd";

    public static final String KEY_CODE_POWER = "Power";

    public static final String PROPERTY_VERSION = "version";
}
