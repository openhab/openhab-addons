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
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.ChannelInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles TV Control Channel State. This is read only.
 * Subscribes to to current channel name.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class TVControlChannelName extends BaseChannelHandler<ChannelInfo> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannelName.class);

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        // nothing to do, this is read only.
    }

    @Override
    protected Optional<ServiceSubscription<ChannelInfo>> getSubscription(String channelId, LGWebOSHandler handler) {
        return Optional.of(handler.getSocket().subscribeCurrentChannel(new ResponseListener<ChannelInfo>() {
            @Override
            public void onError(@Nullable String error) {
                logger.debug("Error in listening to channel name changes: {}.", error);
            }

            @Override
            public void onSuccess(@Nullable ChannelInfo channelInfo) {
                if (channelInfo == null) {
                    return;
                }
                handler.postUpdate(channelId, new StringType(channelInfo.getName()));
            }
        }));

    }
}
