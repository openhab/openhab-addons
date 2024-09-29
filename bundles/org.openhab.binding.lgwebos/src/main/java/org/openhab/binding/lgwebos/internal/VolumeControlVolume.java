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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles TV Control Volume Commands. Allows to set a volume to an absolute number or increment and decrement the
 * volume. If used with On Off type commands it will mute volume when receiving OFF and unmute when receiving ON.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class VolumeControlVolume extends BaseChannelHandler<Float> {
    private final Logger logger = LoggerFactory.getLogger(VolumeControlVolume.class);

    private final ResponseListener<CommandConfirmation> objResponseListener = createResponseListener();

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        final PercentType percent;
        if (RefreshType.REFRESH == command) {
            handler.getSocket().getVolume(createResponseListener(channelId, handler));
            return;
        }
        if (command instanceof PercentType percentCommand) {
            percent = percentCommand;
        } else if (command instanceof DecimalType decimalCommand) {
            percent = new PercentType(decimalCommand.toBigDecimal());
        } else if (command instanceof StringType stringCommand) {
            percent = new PercentType(stringCommand.toString());
        } else {
            percent = null;
        }

        if (percent != null) {
            handler.getSocket().setVolume(percent.floatValue() / 100.0f, objResponseListener);
        } else if (IncreaseDecreaseType.INCREASE == command) {
            handler.getSocket().volumeUp(objResponseListener);
        } else if (IncreaseDecreaseType.DECREASE == command) {
            handler.getSocket().volumeDown(objResponseListener);
        } else if (OnOffType.OFF == command || OnOffType.ON == command) {
            handler.getSocket().setMute(OnOffType.OFF == command, objResponseListener);
        } else {
            logger.info("Only accept PercentType, DecimalType, StringType, RefreshType. Type was {}.",
                    command.getClass());
        }
    }

    @Override
    protected Optional<ServiceSubscription<Float>> getSubscription(String channelUID, LGWebOSHandler handler) {
        return Optional.of(handler.getSocket().subscribeVolume(createResponseListener(channelUID, handler)));
    }

    private ResponseListener<Float> createResponseListener(String channelUID, LGWebOSHandler handler) {
        return new ResponseListener<>() {

            @Override
            public void onError(@Nullable String error) {
                logger.debug("Error in retrieving volume: {}.", error);
            }

            @Override
            public void onSuccess(@Nullable Float value) {
                if (value != null && !Float.isNaN(value)) {
                    handler.postUpdate(channelUID, new PercentType(Math.round(value * 100)));
                }
            }
        };
    }
}
