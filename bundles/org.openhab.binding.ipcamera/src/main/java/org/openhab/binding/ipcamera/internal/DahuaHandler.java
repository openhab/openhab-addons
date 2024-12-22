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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ACCEPTED_CARD_NUMBER;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ACTIVATE_ALARM_OUTPUT;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ACTIVATE_ALARM_OUTPUT2;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_AUTO_LED;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_AUTO_WHITE_LED;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_CAR_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_DOOR_CONTACT;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_DOOR_UNLOCK;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ENABLE_AUDIO_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ENABLE_LED;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ENABLE_LINE_CROSSING_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ENABLE_MOTION_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ENABLE_PRIVACY_MODE;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_EXIT_BUTTON;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_EXIT_BUTTON_ENABLED;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_EXTERNAL_ALARM_INPUT;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_EXTERNAL_ALARM_INPUT2;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_FACE_DETECTED;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_FIELD_DETECTION_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_HUMAN_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ITEM_LEFT;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_ITEM_TAKEN;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_LAST_EVENT_DATA;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_LINE_CROSSING_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_MAGNETIC_LOCK_WARNING;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_MOTION_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_MOTION_DETECTION_LEVEL;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_PARKING_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_SCENE_CHANGE_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_TEXT_OVERLAY;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_THRESHOLD_AUDIO_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_TOO_BLURRY_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_TOO_DARK_ALARM;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_UNACCEPTED_CARD_NUMBER;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_WHITE_LED;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.binding.ipcamera.internal.onvif.OnvifConnection.RequestType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link DahuaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class DahuaHandler extends ChannelDuplexHandler {
    private IpCameraHandler ipCameraHandler;
    private int nvrChannelAdjusted;
    private Pattern boundaryPattern;

    public DahuaHandler(IpCameraHandler handler, int nvrChannel) {
        ipCameraHandler = handler;
        // Most of the API is the NVR channel -1, but some of it is not, like streams and snapshot URLS.
        nvrChannelAdjusted = nvrChannel - 1;
        boundaryPattern = Pattern.compile("^-- ?myboundary$", Pattern.MULTILINE);
    }

    private void processEvent(String content) {
        int startIndex = content.indexOf("Code=") + 5;// skip Code=
        int endIndex = content.indexOf(";", startIndex + 1);
        if (startIndex == -1 || endIndex == -1) {
            ipCameraHandler.logger.debug("Code= not found in Dahua event. Content was:{}", content);
            return;
        }
        String code = content.substring(startIndex, endIndex);
        startIndex = endIndex + 8;// skip ;action=
        endIndex = content.indexOf(";", startIndex);
        if (startIndex == -1 || endIndex == -1) {
            ipCameraHandler.logger.debug(";action= not found in Dahua event. Content was:{}", content);
            return;
        }
        String action = content.substring(startIndex, endIndex);
        startIndex = content.indexOf(";data=", startIndex);
        if (startIndex > 0) {
            endIndex = content.lastIndexOf("}");
            if (endIndex > 0) {
                String data = content.substring(startIndex + 6, endIndex + 1);
                ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(data));
            }
        }
        switch (code) {
            case "VideoMotion":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                }
                break;
            case "TakenAwayDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_ITEM_TAKEN);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_ITEM_TAKEN);
                }
                break;
            case "LeftDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_ITEM_LEFT);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_ITEM_LEFT);
                }
                break;
            case "SmartMotionVehicle":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_CAR_ALARM);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_CAR_ALARM);
                }
                break;
            case "SmartMotionHuman":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_HUMAN_ALARM);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_HUMAN_ALARM);
                }
                break;
            case "CrossLineDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_LINE_CROSSING_ALARM);
                }
                break;
            case "AudioAnomaly":
            case "AudioMutation":
                if ("Start".equals(action)) {
                    ipCameraHandler.audioDetected();
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noAudioDetected();
                }
                break;
            case "FaceDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_FACE_DETECTED);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_FACE_DETECTED);
                }
                break;
            case "ParkingDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_PARKING_ALARM, OnOffType.ON);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_PARKING_ALARM, OnOffType.OFF);
                }
                break;
            case "CrossRegionDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                }
                break;
            case "VideoLoss":
            case "VideoBlind":
                if ("Start".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_TOO_DARK_ALARM, OnOffType.ON);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_TOO_DARK_ALARM, OnOffType.OFF);
                }
                break;
            case "SceneChange":
            case "VideoAbnormalDetection":
                if ("Start".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_SCENE_CHANGE_ALARM, OnOffType.ON);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_SCENE_CHANGE_ALARM, OnOffType.OFF);
                }
                break;
            case "VideoUnFocus":
                if ("Start".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_TOO_BLURRY_ALARM, OnOffType.ON);
                } else if ("Stop".equals(action)) {
                    ipCameraHandler.setChannelState(CHANNEL_TOO_BLURRY_ALARM, OnOffType.OFF);
                }
                break;
            case "AccessControl":
                if ("Pulse".equals(action)) {
                    if (content.contains("\"Method\" : 1")) {
                        if (content.contains("\"ErrorCode\" : 0")) {
                            startIndex = content.indexOf("CardNo", startIndex) + 11;
                            if (startIndex > 0) {
                                endIndex = content.indexOf(",", startIndex) - 1;
                                String cardNo = content.substring(startIndex, endIndex);
                                ipCameraHandler.setChannelState(CHANNEL_ACCEPTED_CARD_NUMBER, new StringType(cardNo));
                                ipCameraHandler.setChannelState(CHANNEL_DOOR_UNLOCK, OnOffType.ON);
                            }
                        }
                    } else if (content.contains("\"Method\" : 5")) {
                        ipCameraHandler.setChannelState(CHANNEL_DOOR_UNLOCK, OnOffType.ON);
                        ipCameraHandler.logger.debug("Door opened from button");
                    } else if (content.contains("\"Method\" : 4")) {
                        ipCameraHandler.setChannelState(CHANNEL_DOOR_UNLOCK, OnOffType.ON);
                        ipCameraHandler.logger.debug("Door opened remotely");
                    }
                } else {
                    ipCameraHandler.logger.debug("Unrecognised Access control Dahua event, content={}", content);
                }
                break;
            case "DoorCard":
                if ("Pulse".equals(action)) {
                    if (content.contains("\"Number\"")) {
                        startIndex = content.indexOf("Number", startIndex) + 11;
                        if (startIndex > 0) {
                            endIndex = content.indexOf(",", startIndex) - 1;
                            String cardNo = content.substring(startIndex, endIndex);
                            ipCameraHandler.setChannelState(CHANNEL_UNACCEPTED_CARD_NUMBER, new StringType(cardNo));
                        }
                    }
                } else {
                    ipCameraHandler.logger.debug("Unrecognised Access control Dahua event, content={}", content);
                }
                break;
            case "ProfileAlarmTransmit":
                if ("Start".equals(action)) {
                    if (content.contains("DoorMagnetism")) {
                        ipCameraHandler.setChannelState(CHANNEL_MAGNETIC_LOCK_WARNING, OnOffType.ON);
                    }
                } else if ("Stop".equals(action)) {
                    if (content.contains("DoorMagnetism")) {
                        ipCameraHandler.setChannelState(CHANNEL_MAGNETIC_LOCK_WARNING, OnOffType.OFF);
                    }
                } else {
                    ipCameraHandler.logger.debug("Unrecognised Alarm Dahua event, content={}", content);
                }
                break;
            case "DoorStatus":
                if ("Pulse".equals(action)) {
                    if (content.contains("\"Relay\" : true")) {
                        ipCameraHandler.setChannelState(CHANNEL_DOOR_UNLOCK, OnOffType.OFF);
                    } else if (content.contains("\"Status\" : \"Close\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_DOOR_CONTACT, OpenClosedType.CLOSED);
                    } else if (content.contains("\"Status\" : \"Open\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_DOOR_CONTACT, OpenClosedType.OPEN);
                    } else {
                        ipCameraHandler.logger.debug("Unrecognised Door status Dahua event, content={}", content);
                    }
                }
                break;
            case "AlarmLocal":
                if ("Start".equals(action)) {
                    if (content.contains("index=0")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.ON);
                    } else if (content.contains("index=3")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXIT_BUTTON, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT2, OnOffType.ON);
                        ipCameraHandler.logger.trace("External alarm Dahua event, content={}", content);
                    }
                } else if ("Stop".equals(action)) {
                    if (content.contains("index=0")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.OFF);
                    } else if (content.contains("index=3")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXIT_BUTTON, OnOffType.OFF);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT2, OnOffType.OFF);
                        ipCameraHandler.logger.trace("External alarm Dahua event, content={}", content);
                    }
                }
                break;
            case "LensMaskOpen":
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.ON);
                break;
            case "LensMaskClose":
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.OFF);
                break;
            case "TimeChange":
                // Check updated time matches openHAB's and store the offset which is needed for ONVIF
                ipCameraHandler.onvifCamera.sendOnvifRequest(RequestType.GetSystemDateAndTime,
                        ipCameraHandler.onvifCamera.deviceXAddr);
                break;
            // Skip these so they are not logged.
            case "NTPAdjustTime": // will trigger a TimeChange event no need to check twice
            case "IntelliFrame":
            case "StorageChange":
            case "Reboot":
            case "NewFile":
            case "VideoMotionInfo":
            case "RtspSessionDisconnect":
            case "LeFunctionStatusSync":
            case "RecordDelete":
            case "InterVideoAccess":
            case "SIPRegisterResult":
                break;
            default:
                ipCameraHandler.logger.debug("Unrecognised Dahua event, Code={}, action={}", code, action);
        }
    }

    private void processSettings(String content) {
        // determine if the motion detection is turned on or off.
        if (content.contains("table.MotionDetect[" + nvrChannelAdjusted + "].Enable=true")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
        } else if (content.contains("table.MotionDetect[" + nvrChannelAdjusted + "].Enable=false")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
        }

        // Handle MotionDetectLevel alarm
        if (content.contains("table.MotionDetect[0].Level=")) {
            String value = ipCameraHandler.returnValueFromString(content, "table.MotionDetect[0].Level=");
            ipCameraHandler.setChannelState(CHANNEL_MOTION_DETECTION_LEVEL, DecimalType.valueOf(value));
        }

        // determine if the audio alarm is turned on or off.
        if (content.contains("table.AudioDetect[" + nvrChannelAdjusted + "].MutationDetect=true")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
        } else if (content.contains("table.AudioDetect[" + nvrChannelAdjusted + "].MutationDetect=false")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
        }

        // Handle AudioMutationThreshold alarm
        if (content.contains("table.AudioDetect[" + nvrChannelAdjusted + "].MutationThreold=")) {
            String value = ipCameraHandler.returnValueFromString(content,
                    "table.AudioDetect[" + nvrChannelAdjusted + "].MutationThreold=");
            ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.valueOf(value));
        }

        // CrossLineDetection alarm on/off
        if (content.contains("table.VideoAnalyseRule[" + nvrChannelAdjusted + "][1].Enable=true")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.ON);
        } else if (content.contains("table.VideoAnalyseRule[" + nvrChannelAdjusted + "][1].Enable=false")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.OFF);
        }
        // Privacy Mode on/off
        if (content.contains("table.LeLensMask[" + nvrChannelAdjusted + "].Enable=true")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.ON);
        } else if (content.contains("table.LeLensMask[" + nvrChannelAdjusted + "].Enable=false")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.OFF);
        }

        // determine if exit button is enabled
        if (content.contains("table.AccessControlGeneral.ButtonExitEnable=true")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.ON);
        } else if (content.contains("table.AccessControlGeneral.ButtonExitEnable=false")) {
            ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.OFF);
        }
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
            String[] events = boundaryPattern.split(content);
            if (events.length > 1) {
                for (int i = 1; i < events.length; i++) {
                    processEvent(events[i]);
                }
            } else {
                processSettings(content);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This handles the commands that come from the openHAB event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=getConfig&name=AudioDetect[" + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=VideoAnalyseRule["
                            + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=MotionDetect["
                            + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_ENABLE_PRIVACY_MODE:
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=getConfig&name=LeLensMask[" + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_AUTO_LED:
                case CHANNEL_ENABLE_LED:
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=getConfig&name=Light[" + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_AUTO_WHITE_LED:
                case CHANNEL_WHITE_LED:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=Lighting_V2["
                            + nvrChannelAdjusted + "][0][1].Mode");
                    return;
                case CHANNEL_MOTION_DETECTION_LEVEL:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=MotionDetect["
                            + nvrChannelAdjusted + "]");
                    return;
                case CHANNEL_EXIT_BUTTON_ENABLED:
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=AccessControlGeneral");
                    return;
            }
            return;
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_TEXT_OVERLAY:
                String text = Helper.encodeSpecialChars(command.toString());
                if (text.isEmpty()) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&VideoWidget["
                            + nvrChannelAdjusted + "].CustomTitle[1].EncodeBlend=false");
                } else {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&VideoWidget[" + nvrChannelAdjusted
                                    + "].CustomTitle[1].EncodeBlend=true&VideoWidget[0].CustomTitle[1].Text=" + text);
                }
                return;
            case CHANNEL_WHITE_LED:
                if (DecimalType.ZERO.equals(command) || OnOffType.OFF.equals(command)) {
                    // IR to auto and white light off.
                    ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.ON);
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting_V2[" + nvrChannelAdjusted
                                    + "][0][1].Mode=Off&Lighting_V2[" + nvrChannelAdjusted + "][0][0].Mode=Auto");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting_V2["
                            + nvrChannelAdjusted + "][0][1].Mode=Manual");
                } else if (command instanceof PercentType percentCommand) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting_V2["
                            + nvrChannelAdjusted + "][0][1].Mode=Manual&Lighting_V2[" + nvrChannelAdjusted
                            + "][0][1].NearLight[0].Light=" + percentCommand.toString());
                }
                return;
            case CHANNEL_AUTO_WHITE_LED:
                if (OnOffType.ON.equals(command)) {
                    // we do not know the state anymore as it now will turns on and off via motion
                    ipCameraHandler.setChannelState(CHANNEL_WHITE_LED, UnDefType.UNDEF);
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AlarmLighting[" + nvrChannelAdjusted
                                    + "][0].Enable=true&Alarm[2].EventHandler.LightingLink.LightDuration=300");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AlarmLighting[" + nvrChannelAdjusted
                                    + "][0].Enable=false&Alarm[2].EventHandler.LightingLink.LightDuration=0");
                }
                return;
            case CHANNEL_ENABLE_LED:
                ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                if (DecimalType.ZERO.equals(command) || OnOffType.OFF.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting["
                            + nvrChannelAdjusted + "][0].Mode=Off");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting["
                            + nvrChannelAdjusted + "][0].Mode=Manual");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting["
                            + nvrChannelAdjusted + "][0].Mode=Manual&Lighting[" + nvrChannelAdjusted
                            + "][0].MiddleLight[0].Light=" + command.toString());
                }
                return;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, UnDefType.UNDEF);
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting["
                            + nvrChannelAdjusted + "][0].Mode=Auto");
                }
                return;
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                if (command instanceof PercentType percentCommand) {
                    if (PercentType.ZERO.equals(command)) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AudioDetect["
                                + nvrChannelAdjusted + "].MutationThreold=1");
                    } else {
                        ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AudioDetect["
                                + nvrChannelAdjusted + "].MutationThreold=" + percentCommand.intValue());
                    }
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AudioDetect["
                            + nvrChannelAdjusted + "].MutationDetect=true&AudioDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AudioDetect["
                            + nvrChannelAdjusted + "].MutationDetect=false");
                }
                return;
            case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&VideoAnalyseRule["
                            + nvrChannelAdjusted + "][1].Enable=true");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&VideoAnalyseRule["
                            + nvrChannelAdjusted + "][1].Enable=false");
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&MotionDetect["
                            + nvrChannelAdjusted + "].Enable=true&MotionDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&MotionDetect["
                            + nvrChannelAdjusted + "].Enable=false");
                }
                return;
            case CHANNEL_MOTION_DETECTION_LEVEL:
                if (command instanceof DecimalType decimalCommand) {
                    if (DecimalType.ZERO.equals(command)) {
                        ipCameraHandler.sendHttpGET(
                                "/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=false&MotionDetect[0].Level="
                                        + decimalCommand.intValue());
                    } else {
                        ipCameraHandler.sendHttpGET(
                                "/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=true&MotionDetect[0].EventHandler.Dejitter=1&MotionDetect[0].Level="
                                        + decimalCommand.intValue());
                    }
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[" + nvrChannelAdjusted + "].Mode=1");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[" + nvrChannelAdjusted + "].Mode=0");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT2:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[1].Mode=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[1].Mode=0");
                }
                return;
            case CHANNEL_ENABLE_PRIVACY_MODE:
                if (OnOffType.OFF.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&LeLensMask["
                            + nvrChannelAdjusted + "].Enable=false");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&LeLensMask["
                            + nvrChannelAdjusted + "].Enable=true");
                }
                return;
            case CHANNEL_DOOR_UNLOCK:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/accessControl.cgi?action=openDoor&channel=1&UserID=101&Type=Remote");
                }
                return;
            case CHANNEL_EXIT_BUTTON_ENABLED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AccessControlGeneral.ButtonExitEnable=true");
                } else if (OnOffType.OFF.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AccessControlGeneral.ButtonExitEnable=false");
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
