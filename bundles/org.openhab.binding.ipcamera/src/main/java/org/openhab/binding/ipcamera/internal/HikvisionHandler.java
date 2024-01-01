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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link HikvisionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class HikvisionHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler ipCameraHandler;
    private int nvrChannel;
    private int lineCount, vmdCount, leftCount, takenCount, faceCount, pirCount, fieldCount;
    private String requestUrl = "";

    public HikvisionHandler(ThingHandler handler, int nvrChannel) {
        ipCameraHandler = (IpCameraHandler) handler;
        this.nvrChannel = nvrChannel;
    }

    private void processEvent(String content) {
        // some cameras use <dynChannelID> or <channelID> and NVRs use channel 0 to say all channels
        if (content.contains("hannelID>" + nvrChannel) || content.contains("<channelID>0</channelID>")) {
            final int debounce = 3;
            String eventType = Helper.fetchXML(content, "", "<eventType>");
            ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(content));
            switch (eventType) {
                case "videoloss":
                    if (content.contains("<eventState>inactive</eventState>")) {
                        if (vmdCount > 1) {
                            vmdCount = 1;
                        }
                        countDown();
                        countDown();
                    }
                    break;
                case "PIR":
                    ipCameraHandler.motionDetected(CHANNEL_PIR_ALARM);
                    pirCount = debounce;
                    break;
                case "attendedBaggage":
                    ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.ON);
                    takenCount = debounce;
                    break;
                case "unattendedBaggage":
                    ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.ON);
                    leftCount = debounce;
                    break;
                case "facedetection":
                    ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.ON);
                    faceCount = debounce;
                    break;
                case "VMD":
                    ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                    vmdCount = debounce;
                    break;
                case "fielddetection":
                    ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                    fieldCount = debounce;
                    break;
                case "linedetection":
                    ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
                    lineCount = debounce;
                    break;
                default:
                    logger.debug("Unrecognised Hikvision eventType={}", eventType);
            }
        }
        countDown();
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
            logger.trace("HTTP Result from {} contains \t:{}:", requestUrl, content);
            switch (requestUrl) {
                case "/ISAPI/Event/notification/alertStream":
                    int startIndex = content.indexOf("<");// skip to start of XML content
                    if (startIndex != -1) {
                        String eventData = content.substring(startIndex, content.length());
                        processEvent(eventData);
                    }
                    return;
                case "/ISAPI/System/IO/capabilities": // Used to check if the camera supports IO
                    List<org.openhab.core.thing.Channel> removeChannels = new ArrayList<>();
                    org.openhab.core.thing.Channel channel;
                    if (content.contains("<IOOutputPortNums>0<") || !content.contains("<IOOutputPortNums>")) {
                        logger.debug("Camera does not support IO outputs.");
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ACTIVATE_ALARM_OUTPUT);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ACTIVATE_ALARM_OUTPUT2);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    } else if (content.contains("<IOOutputPortNums>1<")) {
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ACTIVATE_ALARM_OUTPUT2);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    }
                    ipCameraHandler.lowPriorityRequests.clear(); // no longer need to check if the IO is supported.
                    if (content.contains("<IOInputPortNums>0<") || !content.contains("<IOInputPortNums>")) {
                        logger.debug("Camera does not support IO inputs.");
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_EXTERNAL_ALARM_INPUT);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_EXTERNAL_ALARM_INPUT2);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                    } else if (content.contains("<IOInputPortNums>1<")) {
                        channel = ipCameraHandler.getThing().getChannel(CHANNEL_EXTERNAL_ALARM_INPUT2);
                        if (channel != null) {
                            removeChannels.add(channel);
                        }
                        // start checking the input IO status
                        ipCameraHandler.lowPriorityRequests.set(0,
                                "/ISAPI/System/IO/inputs/" + ipCameraHandler.cameraConfig.getNvrChannel() + "/status");
                    } else {
                        // start checking the input IO status
                        ipCameraHandler.lowPriorityRequests.set(0,
                                "/ISAPI/System/IO/inputs/" + ipCameraHandler.cameraConfig.getNvrChannel() + "/status");
                    }
                    ipCameraHandler.removeChannels(removeChannels);
                    return;
            }
            String replyElement = Helper.fetchXML(content, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<");
            switch (replyElement) {
                case "MotionDetection version=":
                    ipCameraHandler.storeHttpReply(
                            "/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection", content);
                    if (content.contains("<enabled>true</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    } else if (content.contains("<enabled>false</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
                    }
                    break;
                case "IOInputPort version=":
                    ipCameraHandler.storeHttpReply("/ISAPI/System/IO/inputs/" + nvrChannel, content);
                    if (content.contains("<enabled>true</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT, OnOffType.ON);
                    } else if (content.contains("<enabled>false</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT, OnOffType.OFF);
                    }
                    if (content.contains("<triggering>low</triggering>")) {
                        ipCameraHandler.setChannelState(CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT, OnOffType.OFF);
                    } else if (content.contains("<triggering>high</triggering>")) {
                        ipCameraHandler.setChannelState(CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT, OnOffType.ON);
                    }
                    break;
                case "LineDetection":
                    ipCameraHandler.storeHttpReply("/ISAPI/Smart/LineDetection/" + nvrChannel + "01", content);
                    if (content.contains("<enabled>true</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.ON);
                    } else if (content.contains("<enabled>false</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.OFF);
                    }
                    break;
                case "TextOverlay version=":
                    ipCameraHandler.storeHttpReply(
                            "/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1", content);
                    String text = Helper.fetchXML(content, "<enabled>true</enabled>", "<displayText>");
                    ipCameraHandler.setChannelState(CHANNEL_TEXT_OVERLAY, StringType.valueOf(text));
                    break;
                case "AudioDetection version=":
                    ipCameraHandler.storeHttpReply("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01",
                            content);
                    if (content.contains("<enabled>true</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                    } else if (content.contains("<enabled>false</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
                    }
                    break;
                case "IOPortStatus version=":
                    if (content.contains("<ioState>active</ioState>")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.ON);
                    } else if (content.contains("<ioState>inactive</ioState>")) {
                        ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.OFF);
                    }
                    break;
                case "FieldDetection version=":
                    ipCameraHandler.storeHttpReply("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01", content);
                    if (content.contains("<enabled>true</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM, OnOffType.ON);
                    } else if (content.contains("<enabled>false</enabled>")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM, OnOffType.OFF);
                    }
                    break;
                case "ResponseStatus version=":
                    ////////////////// External Alarm Input ///////////////
                    if (content
                            .contains("<requestURL>/ISAPI/System/IO/inputs/" + nvrChannel + "/status</requestURL>")) {
                        // Stops checking the external alarm if camera does not have feature.
                        if (content.contains("<statusString>Invalid Operation</statusString>")) {
                            ipCameraHandler.lowPriorityRequests.remove(0);
                            ipCameraHandler.logger.debug(
                                    "Stopping checks for alarm inputs as camera appears to be missing this feature.");
                        }
                    }
                    break;
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // This does debouncing of the alarms
    void countDown() {
        if (lineCount > 1) {
            lineCount--;
        } else if (lineCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_LINE_CROSSING_ALARM, OnOffType.OFF);
            lineCount--;
        }
        if (vmdCount > 1) {
            vmdCount--;
        } else if (vmdCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.OFF);
            vmdCount--;
        }
        if (leftCount > 1) {
            leftCount--;
        } else if (leftCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.OFF);
            leftCount--;
        }
        if (takenCount > 1) {
            takenCount--;
        } else if (takenCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.OFF);
            takenCount--;
        }
        if (faceCount > 1) {
            faceCount--;
        } else if (faceCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.OFF);
            faceCount--;
        }
        if (pirCount > 1) {
            pirCount--;
        } else if (pirCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_PIR_ALARM, OnOffType.OFF);
            pirCount--;
        }
        if (fieldCount > 1) {
            fieldCount--;
        } else if (fieldCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_FIELD_DETECTION_ALARM, OnOffType.OFF);
            fieldCount--;
        }
        if (fieldCount == 0 && pirCount == 0 && faceCount == 0 && takenCount == 0 && leftCount == 0 && vmdCount == 0
                && lineCount == 0) {
            ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
        }
    }

    public void hikSendXml(String httpPutURL, String xml) {
        logger.trace("Body for PUT:{} is going to be:{}", httpPutURL, xml);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"), httpPutURL);
        request.headers().set(HttpHeaderNames.HOST, ipCameraHandler.cameraConfig.getIp());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
        ByteBuf bbuf = Unpooled.copiedBuffer(xml, StandardCharsets.UTF_8);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);
        ipCameraHandler.sendHttpPUT(httpPutURL, request);
    }

    public void hikChangeSetting(String httpGetPutURL, String removeElement, String replaceRemovedElementWith) {
        ChannelTracking localTracker = ipCameraHandler.channelTrackingMap.get(httpGetPutURL);
        if (localTracker == null) {
            ipCameraHandler.sendHttpGET(httpGetPutURL);
            logger.debug(
                    "Did not have a reply stored before hikChangeSetting was run, try again shortly as a reply has just been requested.");
            return;
        }
        String body = localTracker.getReply();
        if (body.isEmpty()) {
            logger.debug(
                    "Did not have a reply stored before hikChangeSetting was run, try again shortly as a reply has just been requested.");
            ipCameraHandler.sendHttpGET(httpGetPutURL);
        } else {
            logger.trace("An OLD reply from the camera was:{}", body);
            if (body.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
                body = body.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
            }
            int elementIndexStart = body.indexOf("<" + removeElement + ">");
            int elementIndexEnd = body.indexOf("</" + removeElement + ">");
            body = body.substring(0, elementIndexStart) + replaceRemovedElementWith
                    + body.substring(elementIndexEnd + removeElement.length() + 3, body.length());
            logger.trace("Body for this PUT is going to be:{}", body);
            localTracker.setReply(body);
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"),
                    httpGetPutURL);
            request.headers().set(HttpHeaderNames.HOST, ipCameraHandler.cameraConfig.getIp());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
            ByteBuf bbuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
            request.content().clear().writeBytes(bbuf);
            ipCameraHandler.sendHttpPUT(httpGetPutURL, request);
        }
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01");
                    return;
                case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                    ipCameraHandler.sendHttpGET("/ISAPI/Smart/LineDetection/" + nvrChannel + "01");
                    return;
                case CHANNEL_ENABLE_FIELD_DETECTION_ALARM:
                    ipCameraHandler.logger.debug("FieldDetection command");
                    ipCameraHandler.sendHttpGET("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01");
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler
                            .sendHttpGET("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection");
                    return;
                case CHANNEL_TEXT_OVERLAY:
                    ipCameraHandler
                            .sendHttpGET("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1");
                    return;
                case CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT:
                    ipCameraHandler.sendHttpGET("/ISAPI/System/IO/inputs/" + nvrChannel);
                    return;
                case CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT:
                    ipCameraHandler.sendHttpGET("/ISAPI/System/IO/inputs/" + nvrChannel);
                    return;
            }
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_TEXT_OVERLAY:
                logger.debug("Changing text overlay to {}", command.toString());
                if (command.toString().isEmpty()) {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1",
                            "enabled", "<enabled>false</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1",
                            "displayText", "<displayText>" + command.toString() + "</displayText>");
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1",
                            "enabled", "<enabled>true</enabled>");
                }
                return;
            case CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT:
                logger.debug("Changing enabled state of the external input 1 to {}", command.toString());
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT:
                logger.debug("Changing triggering state of the external input 1 to {}", command.toString());
                if (OnOffType.OFF.equals(command)) {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "triggering",
                            "<triggering>low</triggering>");
                } else {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "triggering",
                            "<triggering>high</triggering>");
                }
                return;
            case CHANNEL_ENABLE_PIR_ALARM:
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/WLAlarm/PIR", "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/WLAlarm/PIR", "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
                            "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
                            "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_FIELD_DETECTION_ALARM:
                if (OnOffType.ON.equals(command)) {
                    hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if (OnOffType.ON.equals(command)) {
                    hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
                            "<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>high</outputState>\r\n</IOPortData>\r\n");
                } else {
                    hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
                            "<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>low</outputState>\r\n</IOPortData>\r\n");
                }
                return;
        }
    }
}
