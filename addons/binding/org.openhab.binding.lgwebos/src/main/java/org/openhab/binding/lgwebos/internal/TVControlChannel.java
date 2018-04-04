/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import java.util.List;
import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.ChannelInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TVControl.ChannelListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

/**
 * Handles TV Control Channel Command.
 * Allows to set a channel to an absolute channel number.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class TVControlChannel extends BaseChannelHandler<ChannelListener> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannel.class);

    private TVControl getControl(ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        if (device == null) {
            return;
        }

        if (IncreaseDecreaseType.INCREASE == command) {
            if (device.hasCapabilities(TVControl.Channel_Up)) {
                getControl(device).channelUp(createDefaultResponseListener());
            }
        } else if (IncreaseDecreaseType.DECREASE == command) {
            if (device.hasCapabilities(TVControl.Channel_Down)) {
                getControl(device).channelDown(createDefaultResponseListener());
            }
        } else if (device.hasCapabilities(TVControl.Channel_List, TVControl.Channel_Set)) {
            final String value = command.toString();
            final TVControl control = getControl(device);
            control.getChannelList(new TVControl.ChannelListListener() {
                @Override
                public void onError(ServiceCommandError error) {
                    logger.warn("error requesting channel list: {}.", error.getMessage());
                }

                @Override
                public void onSuccess(List<ChannelInfo> channels) {
                    if (logger.isDebugEnabled()) {
                        channels.forEach(c -> logger.debug("Channel {} - {}", c.getNumber(), c.getName()));
                    }
                    Optional<ChannelInfo> channelInfo = channels.stream().filter(c -> c.getNumber().equals(value))
                            .findFirst();
                    if (channelInfo.isPresent()) {
                        control.setChannel(channelInfo.get(), createDefaultResponseListener());
                    } else {
                        logger.warn("TV does not have a channel: {}.", value);
                    }
                }
            });
        }
    }

    @Override
    protected Optional<ServiceSubscription<ChannelListener>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        if (device.hasCapability(TVControl.Channel_Subscribe)) {
            return Optional.of(getControl(device).subscribeCurrentChannel(new ChannelListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.debug("error: {} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(ChannelInfo channelInfo) {
                    handler.postUpdate(channelId, new DecimalType(channelInfo.getNumber()));
                }
            }));
        } else {
            return null;
        }
    }
}
