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
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands of a Player Item.
 *
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class MediaControlPlayer extends BaseChannelHandler<CommandConfirmation> {
    private final Logger logger = LoggerFactory.getLogger(MediaControlPlayer.class);

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        if (RefreshType.REFRESH == command) {
            // nothing to do
        } else if (PlayPauseType.PLAY == command) {
            handler.getSocket().play(getDefaultResponseListener());
        } else if (PlayPauseType.PAUSE == command) {
            handler.getSocket().pause(getDefaultResponseListener());
        } else if (RewindFastforwardType.FASTFORWARD == command) {
            handler.getSocket().fastForward(getDefaultResponseListener());
        } else if (RewindFastforwardType.REWIND == command) {
            handler.getSocket().rewind(getDefaultResponseListener());
        } else {
            logger.info("Only accept PlayPauseType, RewindFastforwardType, RefreshType. Type was {}.",
                    command.getClass());
        }
    }

    // TODO: playstatesubscription
}
