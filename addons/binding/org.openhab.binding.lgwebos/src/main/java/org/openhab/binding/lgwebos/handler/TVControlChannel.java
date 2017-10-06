/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.handler;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.ChannelInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TVControl.ChannelListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.google.common.collect.Iterables;

/**
 * Handles TV Control Channel Command.
 * Allows to set a channel to an absolute channel number.
 *
 * @author Sebastian Prehn
 * @since 1.8.0
 */
public class TVControlChannel extends BaseChannelHandler<ChannelListener> {
    private Logger logger = LoggerFactory.getLogger(TVControlChannel.class);

    private TVControl getControl(final ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onReceiveCommand(final ConnectableDevice d, Command command) {
        if (d.hasCapabilities(TVControl.Channel_List, TVControl.Channel_Set)) {
            final String value = command.toString();
            final TVControl control = getControl(d);
            control.getChannelList(new TVControl.ChannelListListener() {
                @Override
                public void onError(ServiceCommandError error) {
                    logger.warn("error requesting channel list: {}.", error.getMessage());
                }

                @Override
                public void onSuccess(List<ChannelInfo> channels) {
                    if (logger.isDebugEnabled()) {
                        for (ChannelInfo c : channels) {
                            logger.debug("Channel {} - {}", c.getNumber(), c.getName());
                        }
                    }
                    try {
                        ChannelInfo channelInfo = Iterables.find(channels, c -> c.getNumber().equals(value));
                        control.setChannel(channelInfo, createDefaultResponseListener());
                    } catch (NoSuchElementException ex) {
                        logger.warn("TV does not have a channel: {}.", value);
                    }
                }
            });
        }
    }

    @Override
    protected ServiceSubscription<ChannelListener> getSubscription(final ConnectableDevice device,
            final String channelId, final LGWebOSHandler handler) {
        if (device.hasCapability(TVControl.Channel_Subscribe)) {
            return getControl(device).subscribeCurrentChannel(new ChannelListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.debug("error: {} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(ChannelInfo channelInfo) {
                    handler.postUpdate(channelId, new StringType(channelInfo.getNumber()));
                }
            });
        } else {
            return null;
        }
    }
}
