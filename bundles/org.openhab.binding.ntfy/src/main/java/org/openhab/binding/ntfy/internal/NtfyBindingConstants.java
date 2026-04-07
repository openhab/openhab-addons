/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NtfyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyBindingConstants {

    private static final String BINDING_ID = "ntfy";

    // List of all Thing Type UIDs
    public static final ThingTypeUID NTFY_CONNECTION_THING = new ThingTypeUID(BINDING_ID, "ntfyConnection");
    public static final ThingTypeUID NTFY_TOPIC_THING = new ThingTypeUID(BINDING_ID, "ntfyTopic");

    // List of all Channel ids
    public static final String CHANNEL_NTFY_LASTMESSAGE = "lastMessage";
    public static final String CHANNEL_NTFY_LASTMESSAGETIME = "lastMessageTime";
    public static final String CHANNEL_NTFY_LASTMESSAGEID = "lastMessageMessageId";
}
