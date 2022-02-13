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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link FoscamHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class FoscamHandler extends ChannelDuplexHandler {
    private IpCameraHandler ipCameraHandler;
    private String password, username;

    public FoscamHandler(ThingHandler handler, String username, String password) {
        ipCameraHandler = (IpCameraHandler) handler;
        this.username = username;
        this.password = password;
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
            ////////////// Motion Alarm //////////////
            if (content.contains("<motionDetectAlarm>")) {
                if (content.contains("<motionDetectAlarm>0</motionDetectAlarm>")) {
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
                } else if (content.contains("<motionDetectAlarm>1</motionDetectAlarm>")) { // Enabled but no alarm
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                } else if (content.contains("<motionDetectAlarm>2</motionDetectAlarm>")) {// Enabled, alarm on
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                }
            }

            ////////////// Sound Alarm //////////////
            if (content.contains("<soundAlarm>0</soundAlarm>")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
                ipCameraHandler.setChannelState(CHANNEL_AUDIO_ALARM, OnOffType.OFF);
            }
            if (content.contains("<soundAlarm>1</soundAlarm>")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                ipCameraHandler.noAudioDetected();
            }
            if (content.contains("<soundAlarm>2</soundAlarm>")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                ipCameraHandler.audioDetected();
            }

            ////////////// Sound Threshold //////////////
            if (content.contains("<sensitivity>0</sensitivity>")) {
                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.ZERO);
            }
            if (content.contains("<sensitivity>1</sensitivity>")) {
                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.valueOf("50"));
            }
            if (content.contains("<sensitivity>2</sensitivity>")) {
                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.HUNDRED);
            }

            //////////////// Infrared LED /////////////////////
            if (content.contains("<infraLedState>0</infraLedState>")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, OnOffType.OFF);
            }
            if (content.contains("<infraLedState>1</infraLedState>")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, OnOffType.ON);
            }

            if (content.contains("</CGI_Result>")) {
                ctx.close();
                ipCameraHandler.logger.debug("End of FOSCAM handler reached, so closing the channel to the camera now");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=getAudioAlarmConfig&usr=" + username + "&pwd=" + password);
                    return;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=getAudioAlarmConfig&usr=" + username + "&pwd=" + password);
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=getDevState&usr=" + username + "&pwd=" + password);
                    return;
            }
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_ENABLE_LED:
                // Disable the auto mode first
                ipCameraHandler.sendHttpGET(
                        "/cgi-bin/CGIProxy.fcgi?cmd=setInfraLedConfig&mode=1&usr=" + username + "&pwd=" + password);
                ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                if (DecimalType.ZERO.equals(command) || OnOffType.OFF.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=closeInfraLed&usr=" + username + "&pwd=" + password);
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=openInfraLed&usr=" + username + "&pwd=" + password);
                }
                return;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, UnDefType.UNDEF);
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=setInfraLedConfig&mode=0&usr=" + username + "&pwd=" + password);
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/CGIProxy.fcgi?cmd=setInfraLedConfig&mode=1&usr=" + username + "&pwd=" + password);
                }
                return;
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                int value = Math.round(Float.valueOf(command.toString()));
                if (value == 0) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=0&usr="
                            + username + "&pwd=" + password);
                } else if (value <= 33) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=1&sensitivity=0&usr="
                                    + username + "&pwd=" + password);
                } else if (value <= 66) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=1&sensitivity=1&usr="
                                    + username + "&pwd=" + password);
                } else {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=1&sensitivity=2&usr="
                                    + username + "&pwd=" + password);
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.cameraConfig.getCustomAudioAlarmUrl().isEmpty()) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=1&usr="
                                + username + "&pwd=" + password);
                    } else {
                        ipCameraHandler.sendHttpGET(ipCameraHandler.cameraConfig.getCustomAudioAlarmUrl());
                    }
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setAudioAlarmConfig&isEnable=0&usr="
                            + username + "&pwd=" + password);
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    if (ipCameraHandler.cameraConfig.getCustomMotionAlarmUrl().isEmpty()) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig&isEnable=1&usr="
                                + username + "&pwd=" + password);
                        ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig1&isEnable=1&usr="
                                + username + "&pwd=" + password);
                    } else {
                        ipCameraHandler.sendHttpGET(ipCameraHandler.cameraConfig.getCustomMotionAlarmUrl());
                    }
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig&isEnable=0&usr="
                            + username + "&pwd=" + password);
                    ipCameraHandler.sendHttpGET("/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig1&isEnable=0&usr="
                            + username + "&pwd=" + password);
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
