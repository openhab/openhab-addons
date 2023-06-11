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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link DoorBirdHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class DoorBirdHandler extends ChannelDuplexHandler {
    private IpCameraHandler ipCameraHandler;

    public DoorBirdHandler(ThingHandler handler) {
        ipCameraHandler = (IpCameraHandler) handler;
    }

    // This handles the incoming http replies back from the camera.
    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        try {
            String content = msg.toString();
            ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
            if (content.contains("doorbell:L")) {
                ipCameraHandler.setChannelState(CHANNEL_DOORBELL, OnOffType.OFF);
            } else if (content.contains("doorbell:H")) {
                ipCameraHandler.setChannelState(CHANNEL_DOORBELL, OnOffType.ON);
            }
            if (content.contains("motionsensor:L")) {
                ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
            } else if (content.contains("motionsensor:H")) {
                ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/bha-api/open-door.cgi");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT2:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/bha-api/open-door.cgi?r=2");
                }
                return;
            case CHANNEL_EXTERNAL_LIGHT:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/bha-api/light-on.cgi");
                }
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
