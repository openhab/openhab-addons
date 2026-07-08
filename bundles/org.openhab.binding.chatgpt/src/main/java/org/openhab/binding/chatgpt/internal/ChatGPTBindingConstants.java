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
package org.openhab.binding.chatgpt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link ChatGPTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ChatGPTBindingConstants {
    private static final String BINDING_ID = "chatgpt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // List of all Channel ids
    public static final String CHANNEL_CHAT = "chat";
    public static final ChannelTypeUID CHANNEL_TYPE_UID_CHAT = new ChannelTypeUID(BINDING_ID, CHANNEL_CHAT);

    // Default values for configuration parameters
    public static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    public static final String DEFAULT_MODEL = "gpt-4o-mini";
    public static final double DEFAULT_TEMPERATURE = 1.0;
    public static final double DEFAULT_TOP_P = 1.0;
    public static final int DEFAULT_MAX_TOKENS = 1000;
    public static final int DEFAULT_MAX_TOOL_CALLS = 10;
    public static final String DEFAULT_SYSTEM_MESSAGE = "You are a helpful assistant.";
    public static final int DEFAULT_REQUEST_TIMEOUT = 10;
}
