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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles TV Control Mute Command.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class VolumeControlMute extends BaseChannelHandler<Boolean> {
    private final Logger logger = LoggerFactory.getLogger(VolumeControlMute.class);

    private final ResponseListener<CommandConfirmation> objResponseListener = createResponseListener();

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        if (RefreshType.REFRESH == command) {
            handler.getSocket().getMute(createResponseListener(channelId, handler));
        } else if (OnOffType.ON == command || OnOffType.OFF == command) {
            handler.getSocket().setMute(OnOffType.ON == command, objResponseListener);
        } else {
            logger.info("Only accept OnOffType, RefreshType. Type was {}.", command.getClass());
        }
    }

    @Override
    protected Optional<ServiceSubscription<Boolean>> getSubscription(String channelId, LGWebOSHandler handler) {
        return Optional.of(handler.getSocket().subscribeMute(createResponseListener(channelId, handler)));
    }

    private ResponseListener<Boolean> createResponseListener(String channelId, LGWebOSHandler handler) {
        return new ResponseListener<>() {

            @Override
            public void onError(@Nullable String error) {
                logger.debug("Error in retrieving mute: {}.", error);
            }

            @Override
            public void onSuccess(@Nullable Boolean value) {
                if (value == null) {
                    return;
                }
                handler.postUpdate(channelId, OnOffType.from(value));
            }
        };
    }
}
