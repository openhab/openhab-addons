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
package org.openhab.binding.plex.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PlexPlayerState} is the class used to map the
 * player states for the player things.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public enum PlexPlayerState {
    STOPPED,
    BUFFERING,
    PLAYING,
    PAUSED;

    public static @Nullable PlexPlayerState of(String state) {
        for (PlexPlayerState playerState : values()) {
            if (playerState.toString().toLowerCase().equals(state)) {
                return playerState;
            }
        }
        return null;
    }
}
