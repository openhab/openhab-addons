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
import org.openhab.core.library.types.OnOffType;
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
            switch (requestUrl) {
                case "/cgi-bin/api.cgi?cmd=Login":
                    ipCameraHandler.token = Helper.searchString(content, "\"name\" : \"");
                    ipCameraHandler.logger.info(
                            "Please report that your Reolink camera gave a login token:{}, in response:{}",
                            ipCameraHandler.token, content);
                    break;
                default:
                    if (requestUrl.startsWith("/api.cgi?cmd=GetAiState&channel=")) {
                        ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
                        if (content.contains("\"dog_cat\" : {\r\n" + "            \"alarm_state\" : 1")) {
                            ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.OFF);
                        }
                        if (content.contains("\"face\" : {\r\n" + "            \"alarm_state\" : 1")) {
                            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.OFF);
                        }
                        if (content.contains("\"people\" : {\r\n" + "            \"alarm_state\" : 1")) {
                            ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.OFF);
                        }
                        if (content.contains("\"vehicle\" : {\r\n" + "            \"alarm_state\" : 1")) {
                            ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.OFF);
                        }
                    } else if (requestUrl.startsWith("/api.cgi?cmd=GetMdState&channel=")) {
                        if (content.contains("\"state\" : 0")) {
                            ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.OFF);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.ON);
                        }
                    }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This handles the commands that come from the openHAB event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetMdState&token=" + ipCameraHandler.token);
                    break;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAudioAlarm&token=" + ipCameraHandler.token);
                    break;
                case CHANNEL_AUTO_LED:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetIrLights&token=" + ipCameraHandler.token);
                    break;
            }
            return;
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetMdAlarm&token=" + ipCameraHandler.token);
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetMdAlarm&token=" + ipCameraHandler.token);
                }
                break;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm&token=" + ipCameraHandler.token, "[\r\n"
                            + "    {\r\n" + "        \"cmd\": \" SetAudioAlarm\",\r\n" + "        \"param\": {\r\n"
                            + "            \"Audio\": {\r\n" + "                \"schedule\": {\r\n"
                            + "                    \"enable\": 1,\r\n"
                            + "                    \"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"\r\n"
                            + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }\r\n" + "]\r\n"
                            + "");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm&token=" + ipCameraHandler.token, "[\r\n"
                            + "    {\r\n" + "        \"cmd\": \" SetAudioAlarm\",\r\n" + "        \"param\": {\r\n"
                            + "            \"Audio\": {\r\n" + "                \"schedule\": {\r\n"
                            + "                    \"enable\": 0,\r\n"
                            + "                    \"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"\r\n"
                            + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }\r\n" + "]\r\n"
                            + "");
                }
                break;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights&token=" + ipCameraHandler.token,
                            "[{\r\n" + "    \"cmd\": \"SetIrLights\",\r\n" + "    \"action\": 0,\r\n"
                                    + "    \"param\": {\r\n" + "        \"IrLights\": {\r\n"
                                    + "            \"channel\": 0,\r\n" + "            \"state\": \"Auto\"\r\n" + "\r\n"
                                    + "        }\r\n" + "    }\r\n" + "\r\n" + "}]\r\n" + "");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights&token=" + ipCameraHandler.token,
                            "[{\r\n" + "    \"cmd\": \"SetIrLights\",\r\n" + "    \"action\": 0,\r\n"
                                    + "    \"param\": {\r\n" + "        \"IrLights\": {\r\n"
                                    + "            \"channel\": 0,\r\n" + "            \"state\": \"Off\"\r\n" + "\r\n"
                                    + "        }\r\n" + "    }\r\n" + "\r\n" + "}]\r\n" + "");
                }
                break;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public List<String> getLowPriorityRequests() {
        return List.of();
    }
}
