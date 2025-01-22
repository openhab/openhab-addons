/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.AppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.binding.lgwebos.internal.handler.core.MediaAppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands of a Player Item.
 *
 *
 * @author Sebastian Prehn - Initial contribution
 * @author Jimmy Tanagra - Add play/pause state subscription
 */
@NonNullByDefault
public class MediaControlPlayer extends BaseChannelHandler<MediaAppInfo> {
    private final Logger logger = LoggerFactory.getLogger(MediaControlPlayer.class);
    private final ResponseListener<CommandConfirmation> commandConfirmationResponseListener = createResponseListener();

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        if (RefreshType.REFRESH == command) {
            handler.getSocket().getMediaState(createResponseListener(channelId, handler));
        } else if (PlayPauseType.PLAY == command) {
            handler.getSocket().play(commandConfirmationResponseListener);
        } else if (PlayPauseType.PAUSE == command) {
            handler.getSocket().pause(commandConfirmationResponseListener);
        } else if (RewindFastforwardType.FASTFORWARD == command) {
            handler.getSocket().fastForward(commandConfirmationResponseListener);
        } else if (RewindFastforwardType.REWIND == command) {
            handler.getSocket().rewind(commandConfirmationResponseListener);
        } else {
            logger.info("Only accept PlayPauseType, RewindFastforwardType, RefreshType. Type was {}.",
                    command.getClass());
        }
    }

    @Override
    protected Optional<ServiceSubscription<MediaAppInfo>> getSubscription(String channelId, LGWebOSHandler handler) {
        return Optional.of(handler.getSocket().subscribeMediaState(createResponseListener(channelId, handler)));
    }

    private ResponseListener<MediaAppInfo> createResponseListener(String channelId, LGWebOSHandler handler) {
        return new ResponseListener<>() {
            @Override
            public void onError(@Nullable String error) {
                logger.debug("Error in retrieving application: {}.", error);
            }

            @Override
            public void onSuccess(@Nullable MediaAppInfo mediaAppInfo) {
                logger.trace("Received media app info: {}.", mediaAppInfo);
                if (mediaAppInfo == null || mediaAppInfo.getForegroundAppInfo() == null) {
                    return;
                }

                if (mediaAppInfo.getForegroundAppInfo().isEmpty()) {
                    handler.postUpdate(channelId, UnDefType.UNDEF);
                    return;
                }

                AppInfo appInfo = mediaAppInfo.getForegroundAppInfo().get(0);

                if (appInfo == null || appInfo.getPlayState() == null) {
                    return;
                }

                switch (appInfo.getPlayState()) {
                    case "playing":
                        handler.postUpdate(channelId, PlayPauseType.PLAY);
                        break;
                    case "paused":
                        handler.postUpdate(channelId, PlayPauseType.PAUSE);
                        break;
                    default:
                        handler.postUpdate(channelId, UnDefType.UNDEF);
                }
            }
        };
    }
}
