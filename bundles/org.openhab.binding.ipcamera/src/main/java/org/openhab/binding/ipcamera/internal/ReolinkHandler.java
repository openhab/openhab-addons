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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAbilityResponse;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAiStateResponse;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

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
                        // admin user in case username in config is a restricted user account. This may cause channels
                        // to be removed due to restricted user, causing missing channels to be falsely reported as a
                        // bug.
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAbility" + ipCameraHandler.reolinkAuth,
                                "[{ \"cmd\":\"GetAbility\", \"param\":{ \"User\":{ \"userName\":\"admin\" }}}]");
                    } else {
                        ipCameraHandler.cameraConfigError(
                                "Check your user and password are correct as the Reolink camera gave a bad login response");
                    }
                    break;
                case "/api.cgi?cmd=GetAbility": // Used to check what channels the camera supports
                    List<org.openhab.core.thing.Channel> removeChannels = new ArrayList<>();
                    org.openhab.core.thing.Channel channel = null;
                    try {
                        GetAbilityResponse[] getAbilityResponse = gson.fromJson(content, GetAbilityResponse[].class);
                        if (getAbilityResponse == null) {
                            return;
                        }
                        if (getAbilityResponse[0].value == null || getAbilityResponse[0].value.ability == null) {
                            ipCameraHandler.logger.warn("The GetAbilityResponse could not be parsed: {}",
                                    getAbilityResponse[0].error.detail);
                            return;
                        }
                        if (getAbilityResponse[0].value.ability.scheduleVersion == null) {
                            ipCameraHandler.logger.debug("Camera has no Schedule support.");
                        } else {
                            ipCameraHandler.reolinkScheduleVersion = getAbilityResponse[0].value.ability.scheduleVersion.ver;
                        }
                        if (getAbilityResponse[0].value.ability.supportFtpEnable == null
                                || getAbilityResponse[0].value.ability.supportFtpEnable.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no Enable FTP support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_FTP);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.supportRecordEnable == null
                                || getAbilityResponse[0].value.ability.supportRecordEnable.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no enable recording support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_RECORDINGS);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.abilityChn[0].supportAiDogCat == null
                                || getAbilityResponse[0].value.ability.abilityChn[0].supportAiDogCat.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AiDogCat support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ANIMAL_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.abilityChn[0].supportAiTrackClassify == null
                                || getAbilityResponse[0].value.ability.abilityChn[0].supportAiTrackClassify.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AiTrackClassify support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_AUTO_TRACKING);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.abilityChn[0].supportAiPeople == null
                                || getAbilityResponse[0].value.ability.abilityChn[0].supportAiPeople.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AiPeople support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_HUMAN_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.abilityChn[0].supportAiVehicle == null
                                || getAbilityResponse[0].value.ability.abilityChn[0].supportAiVehicle.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AiVehicle support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_CAR_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.supportEmailEnable == null
                                || getAbilityResponse[0].value.ability.supportEmailEnable.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no EmailEnable support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_EMAIL);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.push == null
                                || getAbilityResponse[0].value.ability.push.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no Push support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_PUSH);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.supportAudioAlarm == null
                                || getAbilityResponse[0].value.ability.supportAudioAlarm.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AudioAlarm support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_AUDIO_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.supportAudioAlarmEnable == null
                                || getAbilityResponse[0].value.ability.supportAudioAlarmEnable.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no support for controlling AudioAlarms.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_THRESHOLD_AUDIO_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_AUDIO_ALARM);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                        if (getAbilityResponse[0].value.ability.abilityChn[0].supportAiFace == null
                                || getAbilityResponse[0].value.ability.abilityChn[0].supportAiFace.permit == 0) {
                            ipCameraHandler.logger.debug("Camera has no AiFace support.");
                            channel = ipCameraHandler.getThing().getChannel(CHANNEL_FACE_DETECTED);
                            if (channel != null) {
                                removeChannels.add(channel);
                            }
                        }
                    } catch (JsonParseException e) {
                        ipCameraHandler.logger.warn("API command GetAbility may not be supported by the camera");
                    }
                    if (channel != null) {
                        ipCameraHandler.removeChannels(removeChannels);
                    }
                    break;
                case "/api.cgi?cmd=GetAiState":
                    ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
                    try {
                        GetAiStateResponse[] aiResponse = gson.fromJson(content, GetAiStateResponse[].class);
                        if (aiResponse == null) {
                            return;
                        }
                        if (aiResponse[0].value == null) {
                            ipCameraHandler.logger.debug("The GetAiStateResponse could not be parsed: {}",
                                    aiResponse[0].error.detail);
                            return;
                        }
                        if (aiResponse[0].value.dogCat.alarmState == 1) {
                            ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_ANIMAL_ALARM, OnOffType.OFF);
                        }
                        if (aiResponse[0].value.face.alarmState == 1) {
                            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.OFF);
                        }
                        if (aiResponse[0].value.people.alarmState == 1) {
                            ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_HUMAN_ALARM, OnOffType.OFF);
                        }
                        if (aiResponse[0].value.vehicle.alarmState == 1) {
                            ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.ON);
                        } else {
                            ipCameraHandler.setChannelState(CHANNEL_CAR_ALARM, OnOffType.OFF);
                        }
                    } catch (JsonParseException e) {
                        ipCameraHandler.logger.debug("API GetAiState is not supported by the camera.");
                    }
                    break;
                case "/api.cgi?cmd=GetAudioAlarm":
                case "/api.cgi?cmd=GetAudioAlarmV20":
                    if (content.contains("\"enable\" : 1")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/api.cgi?cmd=GetIrLights":
                    if (content.contains("\"state\" : \"Off\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetMdAlarm":
                    if (content.contains("00000")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetMdState":
                    if (content.contains("\"state\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetEmail":
                case "/api.cgi?cmd=GetEmailV20":
                    if (content.contains("\"enable\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EMAIL, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EMAIL, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetPush":
                case "/api.cgi?cmd=GetPushV20":
                    if (content.contains("\"enable\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_PUSH, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_PUSH, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetFtpV20":
                    if (content.contains("\"enable\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_FTP, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_FTP, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetWhiteLed":
                    if (content.contains("\"state\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_WHITE_LED, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_WHITE_LED, OnOffType.ON);
                    }
                    break;
                case "/cgi-bin/api.cgi?cmd=Snap":
                    break;
                case "/api.cgi?cmd=GetAiCfg":
                    if (content.contains("\"bSmartTrack\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_TRACKING, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_TRACKING, OnOffType.ON);
                    }
                    break;
                case "/api.cgi?cmd=GetRecV20":
                    if (content.contains("\"enable\" : 0")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_RECORDINGS, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_RECORDINGS, OnOffType.ON);
                    }
                    break;
                default:
                    if (!cutDownURL.startsWith("/cgi-bin/api.cgi?cmd=Set")) {// ignore responses from all Setxx commands
                        ipCameraHandler.logger.warn(
                                "URL {} is not handled currently by the binding, please report this message",
                                cutDownURL);
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
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetMdAlarm" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetMdAlarm\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAudioAlarmV20" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetAudioAlarmV20\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_AUTO_LED:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetIrLights\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_AUTO_WHITE_LED:
                case CHANNEL_WHITE_LED:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetWhiteLed\",\"action\": 0,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_ENABLE_EMAIL:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetEmailV20" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetEmailV20\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_ENABLE_PUSH:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetPushV20" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetPushV20\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_ENABLE_FTP:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetFtpV20" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetFtpV20\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;

                case CHANNEL_AUTO_TRACKING:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetAiCfg" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetAiCfg\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                    break;
                case CHANNEL_ENABLE_RECORDINGS:
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=GetRecV20" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"GetRecV20\", \"action\": 1,\"param\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + "}}]");
                default:
                    ipCameraHandler.logger.trace("REFRESH command is not implemented for channel:{}", channelUID);
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
            case CHANNEL_AUTO_WHITE_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.setChannelState(CHANNEL_WHITE_LED, UnDefType.UNDEF);
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetWhiteLed\",\"param\":{\"WhiteLed\":{\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ", \"mode\": 1}}}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetWhiteLed\",\"param\":{\"WhiteLed\":{\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ", \"mode\": 0}}}]");
                }
                break;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarmV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetAudioAlarmV20\",\"param\":{\"Audio\" : {\"enable\" : 1}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\": \" SetAudioAlarm\",\"param\": {\"Audio\": {\"schedule\": {\"enable\": 1,\"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"}}}}]");
                    }
                } else {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarmV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetAudioAlarmV20\",\"param\":{\"Audio\" : {\"enable\" : 0}}}]");

                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAudioAlarm" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\": \" SetAudioAlarm\",\"param\": {\"Audio\": {\"schedule\": {\"enable\": 0,\"table\": \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"}}}}]");
                    }
                }
                break;
            case CHANNEL_ENABLE_FTP:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtpV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetFtpV20\",\"param\":{\"Ftp\" : {\"enable\" : 1}}}]");

                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtp" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetFtp\",\"param\":{\"Ftp\" : {\"schedule\" : {\"enable\" : 1}}}}]");
                    }
                } else {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtpV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetFtpV20\",\"param\":{\"Ftp\" : {\"enable\" : 0}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetFtp" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetFtp\",\"param\":{\"Ftp\" : {\"schedule\" : {\"enable\" : 0}}}}]");
                    }
                }
                break;
            case CHANNEL_ENABLE_EMAIL:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetEmailV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetEmailV20\",\"param\":{\"Email\" : {\"enable\" : 1}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetEmail" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetEmail\",\"param\":{\"Email\" : {\"schedule\" : {\"enable\" : 1}}}}]");
                    }
                } else {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetEmailV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetEmailV20\",\"param\":{\"Email\" : {\"enable\" : 0}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetEmail" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetEmail\",\"param\":{\"Email\" : {\"schedule\" : {\"enable\" : 0}}}}]");
                    }
                }
                break;
            case CHANNEL_ENABLE_PUSH:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetPushV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetPushV20\",\"param\":{\"Push\":{\"enable\":1}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetPush" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetPush\",\"param\":{\"Push\" : {\"schedule\" : {\"enable\" : 1}}}}]");
                    }
                } else {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetPushV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetPushV20\",\"param\":{\"Push\":{\"enable\":0}}}]");
                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetPush" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetPush\",\"param\":{\"Push\" : {\"schedule\" : {\"enable\" : 0}}}}]");
                    }
                }
                break;
            case CHANNEL_ENABLE_LED:
                ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetIrLights\", \"value\" : { \"IrLights\" : { \"state\" : \"Off\" } } } ]");
                } else if (OnOffType.ON.equals(command) || command instanceof PercentType) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetIrLights" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetIrLights\", \"value\" : {\"IrLights\": {\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"state\": \"Auto\"}}}]");
                } else {
                    ipCameraHandler.logger.warn("Unsupported command sent to enableLED channel");
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
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRecV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetRecV20\",\"param\":{\"Rec\":{\"enable\":1}}}]");

                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRec" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetRec\",\"param\":{\"Rec\" : {\"channel\" : "
                                        + ipCameraHandler.cameraConfig.getNvrChannel()
                                        + ",\"schedule\" : {\"enable\" : 1}}}}]");
                    }
                } else {
                    if (ipCameraHandler.reolinkScheduleVersion == 1) {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRecV20" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetRecV20\",\"param\":{\"Rec\":{\"enable\":0}}}]");

                    } else {
                        ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetRec" + ipCameraHandler.reolinkAuth,
                                "[{\"cmd\":\"SetRec\",\"param\":{\"Rec\" : {\"channel\" : "
                                        + ipCameraHandler.cameraConfig.getNvrChannel()
                                        + ",\"schedule\" : {\"enable\" : 0}}}}]");
                    }
                }
                break;
            case CHANNEL_WHITE_LED:
                ipCameraHandler.setChannelState(CHANNEL_AUTO_WHITE_LED, OnOffType.OFF);
                if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 0,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 0}}}]");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 1,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 2}}}]");
                } else if (command instanceof PercentType percentCommand) {
                    int value = percentCommand.toBigDecimal().intValue();
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetWhiteLed" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\": \"SetWhiteLed\",\"param\": {\"WhiteLed\": {\"state\": 1,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + ",\"mode\": 2,\"bright\": " + value
                                    + "}}}]");
                }
                break;
            case CHANNEL_AUTO_TRACKING:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAiCfg" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetAiCfg\",\"action\":0,\"param\":{\"bSmartTrack\":1,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + " }}]");
                } else {
                    ipCameraHandler.sendHttpPOST("/api.cgi?cmd=SetAiCfg" + ipCameraHandler.reolinkAuth,
                            "[{\"cmd\":\"SetAiCfg\",\"action\":0,\"param\":{\"bSmartTrack\":0,\"channel\": "
                                    + ipCameraHandler.cameraConfig.getNvrChannel() + " }}]");
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
