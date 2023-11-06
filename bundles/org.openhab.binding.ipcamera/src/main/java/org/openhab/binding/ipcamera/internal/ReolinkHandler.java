/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAiStateResponse;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

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
    protected final Gson gson = new Gson();
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
            int afterCommand = requestUrl.indexOf("&");
            String cutDownURL;
            if (afterCommand < 0) {
                cutDownURL = requestUrl;
            } else {
                cutDownURL = requestUrl.substring(0, afterCommand);
            }
            switch (cutDownURL) {// Use a cutdown URL as we can not use variables in a switch()
                case "/api.cgi?cmd=Login":
                    ipCameraHandler.reolinkAuth = "&token=" + Helper.searchString(content, "\"name\" : \"");
                    if (ipCameraHandler.reolinkAuth.length() > 7) {
                        ipCameraHandler.logger.debug("Your Reolink camera gave a login:{}",
                                ipCameraHandler.reolinkAuth);
                        ipCameraHandler.snapshotUri = "/cgi-bin/api.cgi?cmd=Snap&channel="
                                + ipCameraHandler.cameraConfig.getNvrChannel() + "&rs=openHAB"
                                + ipCameraHandler.reolinkAuth;
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAbility" + ipCameraHandler.reolinkAuth,
                                "[{ \"cmd\":\"GetAbility\", \"param\":{ \"User\":{ \"userName\":\""
                                        + ipCameraHandler.cameraConfig.getUser() + "\" }}}]");
                    } else {
                        ipCameraHandler.logger.info("Your Reolink camera gave a bad login response:{}", content);
                    }
                    break;
                case "/api.cgi?cmd=GetAbility": // Used to check what channels the camera supports
                    List<org.openhab.core.thing.Channel> removeChannels = new ArrayList<>();
                    org.openhab.core.thing.Channel channel;
                    if (content.contains("\"supportFtpEnable\": { \"permit\": 0")) {
                        ipCameraHandler.logger.debug("Camera has no Enable FTP support.");
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_FTP);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    }
                    if (content.contains("\"supportRecordEnable\": { \"permit\": 0")) {
                        ipCameraHandler.logger.debug("Camera has no enable recording support.");
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_RECORDINGS);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    }
                    if (content.contains("\"floodLight\": { \"permit\": 0")) {
                        ipCameraHandler.logger.debug("Camera has no Flood light support.");
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_LED);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    }
                    ipCameraHandler.removeChannels(removeChannels);
                    break;
                case "/api.cgi?cmd=GetAiState":
                    ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
                    GetAiStateResponse[] aiResponse = gson.fromJson(content, GetAiStateResponse[].class);
                    if (aiResponse == null) {
                        ipCameraHandler.logger.debug("The GetAiStateResponse could not be parsed");
                        return;
                    }
                    if (aiResponse[0].value.dog_cat.alarm_state == 1) {
                        ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.OFF);
                    }
                    if (aiResponse[0].value.face.alarm_state == 1) {
                        ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.OFF);
                    }
                    if (aiResponse[0].value.people.alarm_state == 1) {
                        ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.OFF);
                    }
                    if (aiResponse[0].value.vehicle.alarm_state == 1) {
                        ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/api.cgi?cmd=GetAudioAlarmV20":
                    if (content.contains("\"enable\" : 1")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/api.cgi?cmd=GetIrLights":
                    if (content.contains("\"state\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetMdState":
                    if (content.contains("\"state\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.ON);
                    }
                    break;
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
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetMdState" + ipCameraHandler.reolinkAuth);
                    break;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAudioAlarmV20" + ipCameraHandler.reolinkAuth,
                            "[{ \"cmd\":\"GetAudioAlarmV20\", \"action\":1, \"param\":{ \"channel\": 0}}]");
                    break;
                case CHANNEL_AUTO_LED:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{ \"cmd\":\"GetIrLights\"}]");
                    break;
            }
            return;
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_ACTIVATE_ALARM_OUTPUT: // cameras built in siren
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=AudioAlarmPlay" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"AudioAlarmPlay\", \"param\": {\"alarm_mode\": \"manul\", \"manual_switch\": 1, \"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + " }}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=AudioAlarmPlay" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"AudioAlarmPlay\", \"param\": {\"alarm_mode\": \"manul\", \"manual_switch\": 0, \"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + " }}]");
                }
                break;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetIrLights\",\"action\": 0,\"param\": {\"IrLights\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"state\": \"Auto\"}}}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetIrLights\",\"action\": 0,\"param\": {\"IrLights\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"state\": \"Off\"}}}]");
                }
                break;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \" SetAudioAlarm\",\"param\": {\"Audio\": {\"schedule\": {\"enable\": 1,\"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"}}}}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \" SetAudioAlarm\",\"param\": {\"Audio\": {\"schedule\": {\"enable\": 0,\"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"}}}}]");
                }
                break;
            case CHANNEL_ENABLE_FTP:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtp" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetFtp\",\"param\":{\"Rec\" : {\"channel\" : "
                                    + ipCameraHandler.cameraConfig.getNvrChannel()
                                    + ",\"schedule\" : {\"enable\" : 1}}}}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtp" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetFtp\",\"param\":{\"Rec\" : {\"channel\" : "
                                    + ipCameraHandler.cameraConfig.getNvrChannel()
                                    + ",\"schedule\" : {\"enable\" : 0}}}}]");
                }
                break;
            case CHANNEL_ENABLE_LED:
                if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 0,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 1}}}]");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 1,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 1}}}]");
                } else if (command instanceof PercentType percentCommand) {
                    int value = percentCommand.toBigDecimal().intValue();
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 1,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 1,\"bright\": " + value
                                    + "}}}]");
                }
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetMdAlarm" + ipCameraHandler.reolinkAuth);
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetMdAlarm" + ipCameraHandler.reolinkAuth);
                }
                break;
            case CHANNEL_ENABLE_RECORDINGS:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRec" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetRec\",\"param\":{\"Rec\" : {\"channel\" : "
                                    + ipCameraHandler.cameraConfig.getNvrChannel()
                                    + ",\"schedule\" : {\"enable\" : 1}}}}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRec" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetRec\",\"param\":{\"Rec\" : {\"channel\" : "
                                    + ipCameraHandler.cameraConfig.getNvrChannel()
                                    + ",\"schedule\" : {\"enable\" : 0}}}}]");
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
