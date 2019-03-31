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
import org.eclipse.smarthome.core.library.types.DecimalType;
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
 * Handles TV Control Channel Command.
 * Allows to set a channel to an absolute channel number.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class TVControlChannel extends BaseChannelHandler<ChannelListener, Object> {
    private final Logger logger = LoggerFactory.getLogger(TVControlChannel.class);
    private final Map<String, List<ChannelInfo>> channelListCache = new HashMap<>();

    private TVControl getControl(ConnectableDevice device) {
        return device.getCapability(TVControl.class);
    }

    @Override
    public void onDeviceReady(@NonNull ConnectableDevice device, @NonNull String channelId,
            @NonNull LGWebOSHandler handler) {
        super.onDeviceReady(device, channelId, handler);

        if (device.hasCapabilities(TVControl.Channel_List)) {
            final TVControl control = getControl(device);
            control.getChannelList(new TVControl.ChannelListListener() {
                @Override
                public void onError(@Nullable ServiceCommandError error) {
                    logger.warn("error requesting channel list: {}.", error == null ? "" : error.getMessage());
                }

                @Override
                @NonNullByDefault({})
                public void onSuccess(List<ChannelInfo> channels) {
                    if (logger.isDebugEnabled()) {
                        channels.forEach(c -> logger.debug("Channel {} - {}", c.getNumber(), c.getName()));
                    }
                    channelListCache.put(device.getId(), channels);
                }
            });
        }

    }

    @Override
    public void onDeviceRemoved(@NonNull ConnectableDevice device, @NonNull String channelId,
            @NonNull LGWebOSHandler handler) {
        super.onDeviceRemoved(device, channelId, handler);
        channelListCache.remove(device.getId());
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        if (hasCapability(device, TVControl.Channel_Set)) {
            final String value = command.toString();
            final TVControl control = getControl(device);
            List<ChannelInfo> channels = channelListCache.get(device.getId());
            if (channels == null) {
                logger.warn("No channel list cached for this device {}, ignoring command.", device.getId());
            } else {
                Optional<ChannelInfo> channelInfo = channels.stream().filter(c -> c.getNumber().equals(value))
                        .findFirst();
                if (channelInfo.isPresent()) {
                    control.setChannel(channelInfo.get(), getDefaultResponseListener());
                } else {
                    logger.warn("TV does not have a channel: {}.", value);
                }
            }
        }
    }

    @Override
    protected Optional<ServiceSubscription<ChannelListener>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        if (hasCapability(device, TVControl.Channel_Subscribe)) {
            return Optional.of(getControl(device).subscribeCurrentChannel(new ChannelListener() {

                @Override
                public void onError(@Nullable ServiceCommandError error) {
                    logger.debug("Error in listening to channel changes: {}.", error == null ? "" : error.getMessage());
                }

                @Override
                public void onSuccess(@Nullable ChannelInfo channelInfo) {
                    if (channelInfo == null) {
                        return;
                    }
                    handler.postUpdate(channelId, new DecimalType(channelInfo.getNumber()));
                }
            }));
        } else {
            return Optional.empty();
        }
    }
}
