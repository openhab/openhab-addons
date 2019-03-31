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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
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
@NonNullByDefault
public class TVControlChannelName extends BaseChannelHandler<ChannelListener, Object> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannelName.class);

    private TVControl getControl(ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        // nothing to do, this is read only.
    }

    @Override
    protected Optional<ServiceSubscription<ChannelListener>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        if (hasCapability(device, TVControl.Channel_Subscribe)) {
            return Optional.of(getControl(device).subscribeCurrentChannel(new ChannelListener() {

                @Override
                public void onError(@Nullable ServiceCommandError error) {
                    logger.debug("Error in listening to channel name changes: {}.",
                            error == null ? "" : error.getMessage());
                }

                @Override
                public void onSuccess(@Nullable ChannelInfo channelInfo) {
                    if (channelInfo == null) {
                        return;
                    }
                    handler.postUpdate(channelId, new StringType(channelInfo.getName()));
                }
            }));
        } else {
            return Optional.empty();
        }
    }
}
