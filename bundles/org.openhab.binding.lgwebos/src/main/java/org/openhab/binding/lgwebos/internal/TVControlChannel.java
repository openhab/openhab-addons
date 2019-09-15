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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.ChannelInfo;
import org.openhab.binding.lgwebos.internal.handler.core.CommandConfirmation;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles TV Control Channel Command.
 * Allows to set a channel to an absolute channel number.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class TVControlChannel extends BaseChannelHandler<ChannelInfo> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannel.class);
    private final Map<ThingUID, @Nullable List<ChannelInfo>> channelListCache = new HashMap<>();
    private final ResponseListener<CommandConfirmation> objResponseListener = createResponseListener();

    @Override
    public void onDeviceReady(@NonNull String channelId, @NonNull LGWebOSHandler handler) {
        super.onDeviceReady(channelId, handler);
        handler.getSocket().getChannelList(new ResponseListener<List<ChannelInfo>>() {
            @Override
            public void onError(@Nullable String error) {
                logger.warn("error requesting channel list: {}.", error);
            }

            @Override
            @NonNullByDefault({})
            public void onSuccess(List<ChannelInfo> channels) {
                if (logger.isDebugEnabled()) {
                    channels.forEach(c -> logger.debug("Channel {} - {}", c.getChannelNumber(), c.getName()));
                }
                channelListCache.put(handler.getThing().getUID(), channels);
            }
        });

    }

    @Override
    public void onDeviceRemoved(@NonNull String channelId, @NonNull LGWebOSHandler handler) {
        super.onDeviceRemoved(channelId, handler);
        channelListCache.remove(handler.getThing().getUID());
    }

    @Override
    public void onReceiveCommand(String channelId, LGWebOSHandler handler, Command command) {
        final String value = command.toString();

        List<ChannelInfo> channels = channelListCache.get(handler.getThing().getUID());
        if (channels == null) {
            logger.warn("No channel list cached for this device {}, ignoring command.",
                    handler.getThing().getUID().toString());
        } else {
            Optional<ChannelInfo> channelInfo = channels.stream().filter(c -> c.getChannelNumber().equals(value))
                    .findFirst();
            if (channelInfo.isPresent()) {
                handler.getSocket().setChannel(channelInfo.get(), objResponseListener);
            } else {
                logger.warn("TV does not have a channel: {}.", value);
            }
        }

    }

    @Override
    protected Optional<ServiceSubscription<ChannelInfo>> getSubscription(String channelId, LGWebOSHandler handler) {
        return Optional.of(handler.getSocket().subscribeCurrentChannel(new ResponseListener<ChannelInfo>() {

            @Override
            public void onError(@Nullable String error) {
                logger.debug("Error in listening to channel changes: {}.", error);
            }

            @Override
            public void onSuccess(@Nullable ChannelInfo channelInfo) {
                if (channelInfo == null) {
                    return;
                }
                handler.postUpdate(channelId, new StringType(channelInfo.getChannelNumber()));
            }
        }));

    }
}
