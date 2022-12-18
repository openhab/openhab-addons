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

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link InstarHandler} is responsible for handling commands, which are
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
            ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
            if (requestUrl.startsWith("/api.cgi?cmd=GetAiState&channel=")) {
                ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
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
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=0");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=1");
                } else if (command instanceof PercentType) {
                    int value = ((PercentType) command).toBigDecimal().divide(BigDecimal.TEN).intValue();
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=1&threshold=" + value);
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=1");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=0");
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setalarmattr&armed=1");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setalarmattr&armed=0");
                }
                return;
            case CHANNEL_TEXT_OVERLAY:
                String text = Helper.encodeSpecialChars(command.toString());
                if (text.isEmpty()) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setoverlayattr&-region=1&-show=0");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setoverlayattr&-region=1&-show=1&-name=" + text);
                }
                return;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setinfrared&-infraredstat=2");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setinfrared&-infraredstat=0");
                }
                return;
            case CHANNEL_ENABLE_PIR_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setpirattr&enable=1");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setpirattr&enable=0");
                }
                return;
            case CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setioattr&enable=1");
                } else {
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=setioattr&enable=0");
                }
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public List<String> getLowPriorityRequests() {
        return List.of();
    }
}
