/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.StringType;
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
 * Handles TV Control Channel State. This is read only.
 * Subscribes to to current channel name.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class TVControlChannelName extends BaseChannelHandler<ChannelListener> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannelName.class);

    private TVControl getControl(ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onReceiveCommand(ConnectableDevice device, String channelId, LGWebOSHandler handler, Command command) {
        // nothing to do, this is read only.
    }

    @Override
    protected Optional<ServiceSubscription<ChannelListener>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        if (device.hasCapability(TVControl.Channel_Subscribe)) {
            return Optional.of(getControl(device).subscribeCurrentChannel(new ChannelListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.debug("{} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(ChannelInfo channelInfo) {
                    handler.postUpdate(channelId, new StringType(channelInfo.getName()));
                }
            }));
        } else {
            return null;
        }
    }
}
