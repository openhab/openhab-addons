/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
import org.openhab.binding.ipcamera.onvif.OnvifConnection;
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
    IpCameraHandler ipCameraHandler;
    String nvrChannel;
    int lineCount, vmdCount, leftCount, takenCount, faceCount, pirCount, fieldCount = 0;

    public HikvisionHandler(ThingHandler handler, String nvrChannel) {
        ipCameraHandler = (IpCameraHandler) handler;
        this.nvrChannel = nvrChannel;
    }

    // This handles the incoming http replies back from the camera.
    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        String content = "";
        int debounce = 3;
        try {
            content = msg.toString();
            if (content.isEmpty()) {
                return;
            }
            logger.trace("HTTP Result back from camera is \t:{}:", content);

            if (content.contains("--boundary")) {// Alarm checking goes in here//
                if (content.contains("<EventNotificationAlert version=\"")) {
                    if (content.contains("hannelID>" + nvrChannel + "</")) {// some camera use c or <dynChannelID>
                        if (content.contains("<eventType>linedetection</eventType>")) {
                            ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
                            lineCount = debounce;
                        }
                        if (content.contains("<eventType>fielddetection</eventType>")) {
                            ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
                            fieldCount = debounce;
                        }
                        if (content.contains("<eventType>VMD</eventType>")) {
                            ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                            vmdCount = debounce;
                        }
                        if (content.contains("<eventType>facedetection</eventType>")) {
                            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.valueOf("ON"));
                            faceCount = debounce;
                        }
                        if (content.contains("<eventType>unattendedBaggage</eventType>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.valueOf("ON"));
                            leftCount = debounce;
                        }
                        if (content.contains("<eventType>attendedBaggage</eventType>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.valueOf("ON"));
                            takenCount = debounce;
                        }
                        if (content.contains("<eventType>PIR</eventType>")) {
                            ipCameraHandler.motionDetected(CHANNEL_PIR_ALARM);
                            pirCount = debounce;
                        }
                        if (content.contains("<eventType>videoloss</eventType>\r\n<eventState>inactive</eventState>")) {
                            if (vmdCount > 1) {
                                vmdCount = 1;
                            }
                            countDown();
                            countDown();
                        }
                    } else if (content.contains("<channelID>0</channelID>")) {// NVR uses channel 0 to say all channels
                        if (content.contains("<eventType>videoloss</eventType>\r\n<eventState>inactive</eventState>")) {
                            if (vmdCount > 1) {
                                vmdCount = 1;
                            }
                            countDown();
                            countDown();
                        }
                    }
                    countDown();
                }
            } else {
                String replyElement = OnvifConnection.fetchXML(content, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                        "<");
                switch (replyElement) {
                    case "MotionDetection version=":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests.indexOf(
                                    "/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection");
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        if (content.contains("<enabled>true</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("ON"));
                        } else if (content.contains("<enabled>false</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("OFF"));
                        }
                        break;
                    case "IOInputPort version=":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests
                                    .indexOf("/ISAPI/System/IO/inputs/" + nvrChannel);
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        if (content.contains("<enabled>true</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT,
                                    OnOffType.valueOf("ON"));
                        } else if (content.contains("<enabled>false</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT,
                                    OnOffType.valueOf("OFF"));
                        }
                        if (content.contains("<triggering>low</triggering>")) {
                            ipCameraHandler.setChannelState(CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT,
                                    OnOffType.valueOf("OFF"));
                        } else if (content.contains("<triggering>high</triggering>")) {
                            ipCameraHandler.setChannelState(CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT,
                                    OnOffType.valueOf("ON"));
                        }
                        break;
                    case "LineDetection":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests
                                    .indexOf("/ISAPI/Smart/LineDetection/" + nvrChannel + "01");
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        if (content.contains("<enabled>true</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM,
                                    OnOffType.valueOf("ON"));
                        } else if (content.contains("<enabled>false</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM,
                                    OnOffType.valueOf("OFF"));
                        }
                        break;
                    case "TextOverlay version=":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests
                                    .indexOf("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "/overlays/text/1");
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        String text = OnvifConnection.fetchXML(content, "<enabled>true</enabled>", "<displayText>");
                        ipCameraHandler.setChannelState(CHANNEL_TEXT_OVERLAY, StringType.valueOf(text));
                        break;
                    case "AudioDetection version=":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests
                                    .indexOf("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01");
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        if (content.contains("<enabled>true</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("ON"));
                        } else if (content.contains("<enabled>false</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("OFF"));
                        }
                        break;
                    case "IOPortStatus version=":
                        if (content.contains("<ioState>active</ioState>")) {
                            ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("ON"));
                        } else if (content.contains("<ioState>inactive</ioState>")) {
                            ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("OFF"));
                        }
                        break;
                    case "FieldDetection version=":
                        ipCameraHandler.lock.lock();
                        try {
                            byte indexInLists = (byte) ipCameraHandler.listOfRequests
                                    .indexOf("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01");
                            if (indexInLists >= 0) {
                                ipCameraHandler.listOfReplies.set(indexInLists, content);
                            }
                        } finally {
                            ipCameraHandler.lock.unlock();
                        }
                        if (content.contains("<enabled>true</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM,
                                    OnOffType.valueOf("ON"));
                        } else if (content.contains("<enabled>false</enabled>")) {
                            ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM,
                                    OnOffType.valueOf("OFF"));
                        }
                        break;
                    case "ResponseStatus version=":
                        ////////////////// External Alarm Input ///////////////
                        if (content.contains(
                                "<requestURL>/ISAPI/System/IO/inputs/" + nvrChannel + "/status</requestURL>")) {
                            // Stops checking the external alarm if camera does not have feature.
                            if (content.contains("<statusString>Invalid Operation</statusString>")) {
                                ipCameraHandler.lowPriorityRequests.remove(0);
                                ipCameraHandler.logger.debug(
                                        "Stopping checks for alarm inputs as camera appears to be missing this feature.");
                            }
                        }
                        break;
                    default:
                        if (content.contains("<EventNotificationAlert")) {
                            if (content.contains("hannelID>" + nvrChannel + "</")
                                    || content.contains("<channelID>0</channelID>")) {// some camera use c or
                                                                                      // <dynChannelID>
                                if (content.contains(
                                        "<eventType>videoloss</eventType>\r\n<eventState>inactive</eventState>")) {
                                    if (vmdCount > 1) {
                                        vmdCount = 1;
                                    }
                                    countDown();
                                    countDown();
                                }
                                countDown();
                            }
                        } else {
                            logger.debug("Unhandled reply-{}.", content);
                        }
                        break;
                }
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
            ipCameraHandler.setChannelState(CHANNEL_LINE_CROSSING_ALARM, OnOffType.valueOf("OFF"));
            lineCount--;
        }
        if (vmdCount > 1) {
            vmdCount--;
        } else if (vmdCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.valueOf("OFF"));
            vmdCount--;
        }
        if (leftCount > 1) {
            leftCount--;
        } else if (leftCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.valueOf("OFF"));
            leftCount--;
        }
        if (takenCount > 1) {
            takenCount--;
        } else if (takenCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.valueOf("OFF"));
            takenCount--;
        }
        if (faceCount > 1) {
            faceCount--;
        } else if (faceCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.valueOf("OFF"));
            faceCount--;
        }
        if (pirCount > 1) {
            pirCount--;
        } else if (pirCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_PIR_ALARM, OnOffType.valueOf("OFF"));
            pirCount--;
        }
        if (fieldCount > 1) {
            fieldCount--;
        } else if (fieldCount == 1) {
            ipCameraHandler.setChannelState(CHANNEL_FIELD_DETECTION_ALARM, OnOffType.valueOf("OFF"));
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
        request.headers().set(HttpHeaderNames.HOST, ipCameraHandler.ipAddress);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
        ByteBuf bbuf = Unpooled.copiedBuffer(xml, StandardCharsets.UTF_8);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);
        ipCameraHandler.sendHttpPUT(httpPutURL, request);
    }

    public void hikChangeSetting(String httpGetPutURL, String removeElement, String replaceRemovedElementWith) {
        String body;
        byte indexInLists;
        ipCameraHandler.lock.lock();
        try {
            indexInLists = (byte) ipCameraHandler.listOfRequests.indexOf(httpGetPutURL);
            if (indexInLists >= 0) {
                if (!"".contains(ipCameraHandler.listOfReplies.get(indexInLists))) {
                    body = ipCameraHandler.listOfReplies.get(indexInLists);
                    logger.trace("An OLD reply from the camera was:{}", body);
                    if (body.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
                        body = body.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
                    }
                    int elementIndexStart = body.indexOf("<" + removeElement + ">");
                    int elementIndexEnd = body.indexOf("</" + removeElement + ">");
                    body = body.substring(0, elementIndexStart) + replaceRemovedElementWith
                            + body.substring(elementIndexEnd + removeElement.length() + 3, body.length());
                    logger.trace("Body for this PUT is going to be:{}", body);
                    ipCameraHandler.listOfReplies.set(indexInLists, body);
                    FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"),
                            httpGetPutURL);
                    request.headers().set(HttpHeaderNames.HOST, ipCameraHandler.ipAddress);
                    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
                    ByteBuf bbuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
                    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
                    request.content().clear().writeBytes(bbuf);
                    ipCameraHandler.sendHttpPUT(httpGetPutURL, request);
                } else {
                    logger.warn(
                            "Did not have a reply stored before hikChangeSetting was run, try again shortly as a reply has just been requested.");
                    ipCameraHandler.sendHttpGET(httpGetPutURL);
                }
            }
        } finally {
            ipCameraHandler.lock.unlock();
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
                if (command.toString().equals("")) {
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
                if (command.toString().equals("ON")) {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT:
                logger.debug("Changing triggering state of the external input 1 to {}", command.toString());
                if (command.toString().equals("OFF")) {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "triggering",
                            "<triggering>low</triggering>");
                } else {
                    hikChangeSetting("/ISAPI/System/IO/inputs/" + nvrChannel, "triggering",
                            "<triggering>high</triggering>");
                }
                return;
            case CHANNEL_ENABLE_PIR_ALARM:
                if ("ON".equals(command.toString())) {
                    hikChangeSetting("/ISAPI/WLAlarm/PIR", "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/WLAlarm/PIR", "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if ("ON".equals(command.toString())) {
                    hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                if ("ON".equals(command.toString())) {
                    hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if ("ON".equals(command.toString())) {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
                            "enabled", "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
                            "enabled", "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ENABLE_FIELD_DETECTION_ALARM:
                if ("ON".equals(command.toString())) {
                    hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>true</enabled>");
                } else {
                    hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01", "enabled",
                            "<enabled>false</enabled>");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if ("ON".equals(command.toString())) {
                    hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
                            "<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>high</outputState>\r\n</IOPortData>\r\n");
                } else {
                    hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
                            "<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>low</outputState>\r\n</IOPortData>\r\n");
                }
                return;
            case CHANNEL_FFMPEG_MOTION_CONTROL:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.motionAlarmEnabled = true;
                } else if ("OFF".equals(command.toString()) || "0".equals(command.toString())) {
                    ipCameraHandler.motionAlarmEnabled = false;
                    ipCameraHandler.noMotionDetected(CHANNEL_MOTION_ALARM);
                } else {
                    ipCameraHandler.motionAlarmEnabled = true;
                    ipCameraHandler.motionThreshold = Double.valueOf(command.toString());
                    ipCameraHandler.motionThreshold = ipCameraHandler.motionThreshold / 10000;
                }
                ipCameraHandler.setupFfmpegFormat(IpCameraHandler.ffmpegFormat.RTSPHELPER);
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public ArrayList<String> getLowPriorityRequests() {
        ArrayList<String> lowPriorityRequests = new ArrayList<String>(1);
        lowPriorityRequests.add("/ISAPI/System/IO/inputs/" + nvrChannel + "/status"); // must stay in element 0.
        return lowPriorityRequests;
    }
}
