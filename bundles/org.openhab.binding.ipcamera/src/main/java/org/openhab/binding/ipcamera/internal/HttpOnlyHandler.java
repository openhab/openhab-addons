/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link HttpOnlyHandler} is responsible for handling commands for httponly and Onvif thingtypes.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class HttpOnlyHandler extends ChannelDuplexHandler {
    IpCameraHandler ipCameraHandler;

    public HttpOnlyHandler(IpCameraHandler handler) {
        ipCameraHandler = handler;
    }

    // This handles the incoming http replies back from the camera.
    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        ReferenceCountUtil.release(msg);
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    return;
            }
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.audioAlarmEnabled = true;
                } else if ("OFF".equals(command.toString()) || "0".equals(command.toString())) {
                    ipCameraHandler.audioAlarmEnabled = false;
                } else {
                    ipCameraHandler.audioAlarmEnabled = true;
                    ipCameraHandler.audioThreshold = Integer.valueOf(command.toString());
                }
                ipCameraHandler.setupFfmpegFormat("RTSPHELPER");
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public ArrayList<String> getLowPriorityRequests() {
        ArrayList<String> lowPriorityRequests = new ArrayList<String>(1);
        return lowPriorityRequests;
    }
}
