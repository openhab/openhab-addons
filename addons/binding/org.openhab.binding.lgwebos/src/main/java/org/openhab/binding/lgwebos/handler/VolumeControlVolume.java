/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
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
 * @author Sebastian Prehn
 * @since 1.8.0
 */
public class VolumeControlVolume extends BaseChannelHandler<VolumeListener> {
    private Logger logger = LoggerFactory.getLogger(VolumeControlVolume.class);

    private VolumeControl getControl(final ConnectableDevice device) {
        return device.getCapability(VolumeControl.class);
    }

    @Override
    public void onReceiveCommand(final ConnectableDevice d, Command command) {
        PercentType percent = null;
        if (command instanceof PercentType) {
            percent = (PercentType) command;
        } else if (command instanceof DecimalType) {
            percent = new PercentType(((DecimalType) command).toBigDecimal());
        } else if (command instanceof StringType) {
            percent = new PercentType(((StringType) command).toString());
        }
        if (percent != null) {
            if (d.hasCapabilities(VolumeControl.Volume_Set)) {
                getControl(d).setVolume(percent.floatValue() / 100.0f, createDefaultResponseListener());
            } else {
                logger.warn("Device does not have the capability to set volume. Ignoring command {}.", command);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            if (d.hasCapabilities(VolumeControl.Volume_Up_Down)) {
                if (IncreaseDecreaseType.INCREASE.equals(command)) {
                    getControl(d).volumeUp(createDefaultResponseListener());
                }
                if (IncreaseDecreaseType.DECREASE.equals(command)) {
                    getControl(d).volumeDown(createDefaultResponseListener());
                }
            } else {
                logger.warn("Device does not have the capability to increase or decrease volume. Ignoring command {}.",
                        command);
            }
        } else if (command instanceof OnOffType) {
            if (d.hasCapabilities(VolumeControl.Mute_Set)) {
                getControl(d).setMute(OnOffType.OFF.equals(command), createDefaultResponseListener());
            } else {
                logger.warn("Device does not have the capability to set mute. Ignoring command {}.", command);
            }
        } else {
            logger.warn("Only accept PercentType, DecimalType, StringType, OnOffType. Type was {}.",
                    command.getClass());
        }
    }

    @Override
    protected ServiceSubscription<VolumeListener> getSubscription(final ConnectableDevice device,
            final String channelUID, final LGWebOSHandler handler) {
        if (device.hasCapability(VolumeControl.Volume_Subscribe)) {
            return getControl(device).subscribeVolume(new VolumeListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.warn("{} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(Float value) {
                    handler.postUpdate(channelUID, new PercentType(Math.round(value * 100)));
                }
            });
        } else {
            return null;
        }
    }
}
