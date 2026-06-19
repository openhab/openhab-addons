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
package org.openhab.binding.gemini.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link GeminiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiBindingConstants {
    public static final String BINDING_ID = "gemini";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // List of all Channel ids
    public static final String CHANNEL_CHAT = "chat";
    public static final ChannelTypeUID CHANNEL_TYPE_UID_CHAT = new ChannelTypeUID(BINDING_ID, CHANNEL_CHAT);

    // Default values for configuration parameters
    public static final String DEFAULT_MODEL = "gemini-2.5-flash";
    public static final double DEFAULT_TEMPERATURE = 1.0;
    public static final double DEFAULT_TOP_P = 1.0;
    public static final int DEFAULT_MAX_OUTPUT_TOKENS = 2048;
    public static final String DEFAULT_SYSTEM_MESSAGE = "You are a helpful assistant.";
    public static final int DEFAULT_REQUEST_TIMEOUT = 30;

    // Default values as string needed for annotations
    public static final String DEFAULT_TEMPERATURE_STR = "1.0";
    public static final String DEFAULT_TOP_P_STR = "1.0";
    public static final String MAX_OUTPUT_TOKENS_STR = "2048";
}
