/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link ReolinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class ReolinkHandler extends ChannelDuplexHandler {
    private IpCameraHandler ipCameraHandler;
    private String requestUrl = "Empty";

    public ReolinkHandler(IpCameraHandler thingHandler) {
        ipCameraHandler = thingHandler;
    }

    public void setURL(String url) {
        requestUrl = url;
    }

    // This handles the incoming http replies back from the camera.
    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        try {
            String content = msg.toString();
            ipCameraHandler.logger.trace("HTTP Result from {} contains \t:{}:", requestUrl, content);
            if (requestUrl.startsWith("/api.cgi?cmd=GetAiState&channel=")) {
                ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
                ipCameraHandler.logger
                        .debug("AI/object detection not implemented yet, report the cameras output from TRACE logs.");
            } else if (requestUrl.startsWith("/api.cgi?cmd=GetMdState&channel=")) {
                ipCameraHandler.logger
                        .debug("Motion detection not implemented yet, report the cameras output from TRACE logs.");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This handles the commands that come from the openHAB event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_ENABLE_MOTION_ALARM:
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public List<String> getLowPriorityRequests() {
        return List.of();
    }
}
