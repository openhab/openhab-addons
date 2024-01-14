/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_THRESHOLD_AUDIO_ALARM;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link HttpOnlyHandler} is responsible for handling commands for generic and onvif thing types.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class HttpOnlyHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler ipCameraHandler;

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
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.ffmpegAudioAlarmEnabled = true;
                } else if (OnOffType.OFF.equals(command) || DecimalType.ZERO.equals(command)) {
                    ipCameraHandler.ffmpegAudioAlarmEnabled = false;
                } else {
                    ipCameraHandler.ffmpegAudioAlarmEnabled = true;
                    try {
                        ipCameraHandler.audioThreshold = Integer.valueOf(command.toString());
                    } catch (NumberFormatException e) {
                        logger.warn("Audio Threshold recieved an unexpected command, was it a number?");
                    }
                }
                ipCameraHandler.setupFfmpegFormat(FFmpegFormat.RTSP_ALARMS);
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list and sends 1 every 8 seconds.
    public ArrayList<String> getLowPriorityRequests() {
        return new ArrayList<>(0);
    }
}
