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
 * The {@link AmcrestHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class AmcrestHandler extends ChannelDuplexHandler {
    private String requestUrl = "Empty";
    private IpCameraHandler ipCameraHandler;

    public AmcrestHandler(ThingHandler handler) {
        ipCameraHandler = (IpCameraHandler) handler;
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
            if (content.contains("Error: No Events")) {
                if ("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=VideoMotion".equals(requestUrl)) {
                    ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                } else if ("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=AudioMutation".equals(requestUrl)) {
                    ipCameraHandler.noAudioDetected();
                }
            } else if (content.contains("channels[0]=0")) {
                if ("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=VideoMotion".equals(requestUrl)) {
                    ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                } else if ("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=AudioMutation".equals(requestUrl)) {
                    ipCameraHandler.audioDetected();
                }
            }

            if (content.contains("table.MotionDetect[0].Enable=false")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
            } else if (content.contains("table.MotionDetect[0].Enable=true")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
            }
            // determine if the audio alarm is turned on or off.
            if (content.contains("table.AudioDetect[0].MutationDetect=true")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
            } else if (content.contains("table.AudioDetect[0].MutationDetect=false")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
            }
            // Handle AudioMutationThreshold alarm
            if (content.contains("table.AudioDetect[0].MutationThreold=")) {
                String value = ipCameraHandler.returnValueFromString(content, "table.AudioDetect[0].MutationThreold=");
                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.valueOf(value));
            }
            // Privacy Mode on/off
            if (content.contains("Code=LensMaskOpen;") || content.contains("table.LeLensMask[0].Enable=true")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.ON);
            } else if (content.contains("Code=LensMaskClose;")
                    || content.contains("table.LeLensMask[0].Enable=false")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_PRIVACY_MODE, OnOffType.OFF);
            }
        } finally {
            ReferenceCountUtil.release(msg);
            ctx.close();
        }
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=AudioDetect[0]");
                    return;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=AudioDetect[0]");
                    return;
                case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=CrossLineDetection[0]");
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=MotionDetect[0]");
                    return;
                case CHANNEL_ENABLE_PRIVACY_MODE:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=LeLensMask[0]");
                    return;
            }
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_TEXT_OVERLAY:
                String text = Helper.encodeSpecialChars(command.toString());
                if (text.isEmpty()) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoWidget[0].CustomTitle[1].EncodeBlend=false");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoWidget[0].CustomTitle[1].EncodeBlend=true&VideoWidget[0].CustomTitle[1].Text="
                                    + text);
                }
                return;
            case CHANNEL_ENABLE_LED:
                ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                if (DecimalType.ZERO.equals(command) || OnOffType.OFF.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Off");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Manual");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Manual&Lighting[0][0].MiddleLight[0].Light="
                                    + command.toString());
                }
                return;
            case CHANNEL_AUTO_LED:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, UnDefType.UNDEF);
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Auto");
                }
                return;
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                int threshold = Math.round(Float.valueOf(command.toString()));

                if (threshold == 0) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationThreold=1");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationThreold=" + threshold);
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationDetect=true&AudioDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationDetect=false");
                }
                return;
            case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoAnalyseRule[0][1].Enable=true");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoAnalyseRule[0][1].Enable=false");
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=true&MotionDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=false");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if (OnOffType.ON.equals(command)) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[0].Mode=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[0].Mode=0");
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
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&LeLensMask[0].Enable=false");
                } else if (OnOffType.ON.equals(command)) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&LeLensMask[0].Enable=true");
                }
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public ArrayList<String> getLowPriorityRequests() {
        return new ArrayList<>(1);
    }
}
