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
package org.openhab.binding.heos.internal.json.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum to reference the different HEOS events
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public enum HeosEvent {
    SOURCES_CHANGED,
    PLAYERS_CHANGED,
    GROUPS_CHANGED,
    PLAYER_STATE_CHANGED,
    PLAYER_NOW_PLAYING_CHANGED,
    PLAYER_NOW_PLAYING_PROGRESS,
    PLAYER_PLAYBACK_ERROR,
    PLAYER_QUEUE_CHANGED,
    PLAYER_VOLUME_CHANGED,
    REPEAT_MODE_CHANGED,
    SHUFFLE_MODE_CHANGED,
    GROUP_VOLUME_CHANGED,
    USER_CHANGED;

    private static final Logger LOGGER = LoggerFactory.getLogger(HeosEvent.class);

    @Nullable
    public static HeosEvent valueOfString(@Nullable String eventCommand) {
        if (eventCommand == null) {
            return null;
        }
        try {
            String command = eventCommand.substring(6);
            return HeosEvent.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unsupported event {}", eventCommand);
            return null;
        }
    }
}
