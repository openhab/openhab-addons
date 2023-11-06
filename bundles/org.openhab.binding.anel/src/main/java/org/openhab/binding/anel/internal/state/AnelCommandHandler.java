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
package org.openhab.binding.anel.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anel.internal.IAnelConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert an openhab command to an ANEL UDP command message.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(AnelCommandHandler.class);

    public @Nullable State getLockedState(@Nullable AnelState state, String channelId) {
        if (IAnelConstants.CHANNEL_RELAY_STATE.contains(channelId)) {
            if (state == null) {
                return null; // assume unlocked
            }

            final int index = IAnelConstants.getIndexFromChannel(channelId);

            final @Nullable Boolean locked = state.relayLocked[index];
            if (locked == null || !locked.booleanValue()) {
                return null; // no lock information or unlocked
            }

            final @Nullable Boolean lockedState = state.relayState[index];
            if (lockedState == null) {
                return null; // no state information available
            }

            return OnOffType.from(lockedState.booleanValue());
        }

        if (IAnelConstants.CHANNEL_IO_STATE.contains(channelId)) {
            if (state == null) {
                return null; // assume unlocked
            }

            final int index = IAnelConstants.getIndexFromChannel(channelId);

            final @Nullable Boolean isInput = state.ioIsInput[index];
            if (isInput == null || !isInput.booleanValue()) {
                return null; // no direction infmoration or output port
            }

            final @Nullable Boolean ioState = state.ioState[index];
            if (ioState == null) {
                return null; // no state information available
            }
            return OnOffType.from(ioState.booleanValue());
        }
        return null; // all other channels are read-only!
    }

    public @Nullable String toAnelCommandAndUnsetState(@Nullable AnelState state, String channelId, Command command,
            String authentication) {
        if (!(command instanceof OnOffType)) {
            // only relay states and io states can be changed, all other channels are read-only
            logger.warn("Anel binding only support ON/OFF and Refresh commands, not {}: {}",
                    command.getClass().getSimpleName(), command);
        } else if (IAnelConstants.CHANNEL_RELAY_STATE.contains(channelId)) {
            final int index = IAnelConstants.getIndexFromChannel(channelId);

            // unset anel state which enforces a channel state update
            if (state != null) {
                state.relayState[index] = null;
            }

            @Nullable
            final Boolean locked = state == null ? null : state.relayLocked[index];
            if (locked == null || !locked.booleanValue()) {
                return String.format("Sw_%s%d%s", command.toString().toLowerCase(), index + 1, authentication);
            } else {
                logger.warn("Relay {} is locked; skipping command {}.", index + 1, command);
            }
        } else if (IAnelConstants.CHANNEL_IO_STATE.contains(channelId)) {
            final int index = IAnelConstants.getIndexFromChannel(channelId);

            // unset anel state which enforces a channel state update
            if (state != null) {
                state.ioState[index] = null;
            }

            @Nullable
            final Boolean isInput = state == null ? null : state.ioIsInput[index];
            if (isInput == null || !isInput.booleanValue()) {
                return String.format("IO_%s%d%s", command.toString().toLowerCase(), index + 1, authentication);
            } else {
                logger.warn("IO {} has direction input, not output; skipping command {}.", index + 1, command);
            }
        }

        return null; // all other channels are read-only
    }
}
