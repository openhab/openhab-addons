/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.VolumeControl.VolumeListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

/**
 * Handles TV Control Volume Commands. Allows to set a volume to an absolute number or increment and decrement the
 * volume. If used with On Off type commands it will mute volume when receiving OFF and unmute when receiving ON.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class VolumeControlVolume extends BaseChannelHandler<VolumeListener, Object> {
    private final Logger logger = LoggerFactory.getLogger(VolumeControlVolume.class);

    private VolumeControl getControl(ConnectableDevice device) {
        return device.getCapability(VolumeControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        PercentType percent = null;
        if (command instanceof PercentType) {
            percent = (PercentType) command;
        } else if (command instanceof DecimalType) {
            percent = new PercentType(((DecimalType) command).toBigDecimal());
        } else if (command instanceof StringType) {
            percent = new PercentType(((StringType) command).toString());
        }
        if (percent != null) {
            if (hasCapability(device, VolumeControl.Volume_Set)) {
                getControl(device).setVolume(percent.floatValue() / 100.0f, getDefaultResponseListener());
            }
        } else if (IncreaseDecreaseType.INCREASE == command) {
            if (hasCapability(device, VolumeControl.Volume_Up_Down)) {
                getControl(device).volumeUp(getDefaultResponseListener());
            }
        } else if (IncreaseDecreaseType.DECREASE == command) {
            if (hasCapability(device, VolumeControl.Volume_Up_Down)) {
                getControl(device).volumeDown(getDefaultResponseListener());
            }
        } else if (OnOffType.OFF == command || OnOffType.ON == command) {
            if (hasCapability(device, VolumeControl.Mute_Set)) {
                getControl(device).setMute(OnOffType.OFF == command, getDefaultResponseListener());
            }
        } else {
            logger.warn("Only accept PercentType, DecimalType, StringType command. Type was {}.", command.getClass());
        }
    }

    @Override
    protected Optional<ServiceSubscription<VolumeListener>> getSubscription(ConnectableDevice device, String channelUID,
            LGWebOSHandler handler) {
        if (hasCapability(device, VolumeControl.Volume_Subscribe)) {
            return Optional.of(getControl(device).subscribeVolume(new VolumeListener() {

                @Override
                public void onError(@Nullable ServiceCommandError error) {
                    logger.debug("Error in listening to volume changes: {}.", error == null ? "" : error.getMessage());
                }

                @Override
                public void onSuccess(@Nullable Float value) {
                    if (value != null) {
                        handler.postUpdate(channelUID, new PercentType(Math.round(value * 100)));
                    }
                }
            }));
        } else {
            return Optional.empty();
        }
    }
}
