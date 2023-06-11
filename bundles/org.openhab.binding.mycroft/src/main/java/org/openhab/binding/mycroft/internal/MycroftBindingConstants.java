/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mycroft.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MycroftBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class MycroftBindingConstants {

    private static final String BINDING_ID = "mycroft";

    // List of all Thing Type UIDs
    public static final ThingTypeUID MYCROFT = new ThingTypeUID(BINDING_ID, "mycroft");

    // List of all Channel ids
    public static final String LISTEN_CHANNEL = "listen";
    public static final String SPEAK_CHANNEL = "speak";
    public static final String PLAYER_CHANNEL = "player";
    public static final String VOLUME_CHANNEL = "volume";
    public static final String VOLUME_MUTE_CHANNEL = "volume_mute";
    public static final String UTTERANCE_CHANNEL = "utterance";
    public static final String FULL_MESSAGE_CHANNEL = "full_message";

    // Channel property :
    public static final String FULL_MESSAGE_CHANNEL_MESSAGE_TYPE_PROPERTY = "messageTypes";
}
