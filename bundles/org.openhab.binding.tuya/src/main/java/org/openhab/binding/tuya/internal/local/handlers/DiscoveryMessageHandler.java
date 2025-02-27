/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.local.handlers;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.DeviceInfoSubscriber;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.dto.DeviceInfo;
import org.openhab.binding.tuya.internal.local.dto.DiscoveryMessage;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * The {@link DiscoveryMessageHandler} is used for handling UDP discovery messages
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DiscoveryMessageHandler extends ChannelDuplexHandler {
    private final Map<String, DeviceInfo> deviceInfos;
    private final Map<String, DeviceInfoSubscriber> deviceListeners;

    public DiscoveryMessageHandler(Map<String, DeviceInfo> deviceInfos,
            Map<String, DeviceInfoSubscriber> deviceListeners) {
        this.deviceInfos = deviceInfos;
        this.deviceListeners = deviceListeners;
    }

    @Override
    public void channelRead(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object msg)
            throws Exception {
        if (msg instanceof MessageWrapper<?> messageWrapper) {
            if ((messageWrapper.commandType == CommandType.UDP_NEW || messageWrapper.commandType == CommandType.UDP
                    || messageWrapper.commandType == CommandType.BROADCAST_LPV34)) {
                DiscoveryMessage discoveryMessage = (DiscoveryMessage) Objects.requireNonNull(messageWrapper.content);
                DeviceInfo deviceInfo = new DeviceInfo(discoveryMessage.ip, discoveryMessage.version);
                if (!deviceInfo.equals(deviceInfos.put(discoveryMessage.deviceId, deviceInfo))) {
                    DeviceInfoSubscriber subscriber = deviceListeners.get(discoveryMessage.deviceId);

                    if (subscriber != null) {
                        subscriber.deviceInfoChanged(deviceInfo);
                    }
                }
            }
        }
    }
}
