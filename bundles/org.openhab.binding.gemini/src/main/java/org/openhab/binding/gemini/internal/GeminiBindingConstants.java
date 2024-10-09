/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gemini.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link GeminiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class GeminiBindingConstants {

    private static final String BINDING_ID = "gemini";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // List of all Channel ids
    public static final String CHANNEL_CHAT = "chat";

    public static final ChannelTypeUID CHANNEL_TYPE_UID_CHAT = new ChannelTypeUID(BINDING_ID, CHANNEL_CHAT);
}
