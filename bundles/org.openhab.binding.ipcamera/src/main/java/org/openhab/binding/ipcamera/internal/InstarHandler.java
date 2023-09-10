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

import java.math.BigDecimal;
import java.util.ArrayList;

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
public class InstarHandler extends ChannelDuplexHandler {
    private IpCameraHandler ipCameraHandler;
    private String requestUrl = "Empty";

    public InstarHandler(IpCameraHandler thingHandler) {
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
            String value1 = "";
            String content = msg.toString();
            ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
            switch (requestUrl) {
                case "/param.cgi?cmd=getinfrared":
                    if (content.contains("var infraredstat=\"auto") || content.contains("infraredstat=\"2\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.OFF);
                    }
                    break;
                case "/param.cgi?cmd=getoverlayattr&-region=1":// Text Overlays
                    if (content.contains("var show_1=\"0\"") || content.contains("show=\"0\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_TEXT_OVERLAY, StringType.EMPTY);
                    } else {
                        value1 = Helper.searchString(content, "var name_1=\"");
                        if (!value1.isEmpty()) {
                            ipCameraHandler.setChannelState(CHANNEL_TEXT_OVERLAY, StringType.valueOf(value1));
                        } else {
                            value1 = Helper.searchString(content, "name=\"");
                            if (!value1.isEmpty()) {
                                ipCameraHandler.setChannelState(CHANNEL_TEXT_OVERLAY, StringType.valueOf(value1));
                            }
                        }
                    }
                    break;
                case "/cgi-bin/hi3510/param.cgi?cmd=getmdattr":// Motion Alarm old
                    if (content.contains("var m1_enable=\"1\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/param.cgi?cmd=getalarmattr":// Motion Alarm new
                    if (content.contains("armed=\"1\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/param.cgi?cmd=getaudioalarmattr":// Audio Alarm
                    if (content.contains("enable=\"1\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.ON);
                        value1 = Helper.searchString(content, "aa_value=\"");
                        if (!value1.isEmpty()) {// old cameras have threshold in percentage
                            ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.valueOf(value1));
                        } else {
                            value1 = Helper.searchString(content, "threshold=\"");
                            if (!value1.isEmpty()) {// newer cameras have values up to 10
                                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM,
                                        PercentType.valueOf(value1 + "0"));
                            }
                        }
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.OFF);
                        ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, OnOffType.OFF);
                    }
                    break;
                case "/param.cgi?cmd=getpirattr":// PIR Alarm
                    if (content.contains("enable=\"1\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_PIR_ALARM, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_PIR_ALARM, OnOffType.OFF);
                    }
                    // Reset the Alarm, need to find better place to put this.
                    ipCameraHandler.noMotionDetected(CHANNEL_PIR_ALARM);
                    break;
                case "/param.cgi?cmd=getioattr":// External Alarm Input
                    if (content.contains("enable=\"1\"")) {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT, OnOffType.ON);
                    } else {
                        ipCameraHandler.setChannelState(CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT, OnOffType.OFF);
                    }
                    break;
                default:
                    if (requestUrl.startsWith("/param.cgi?cmd=setasaction&-server=1&enable=1")
                            && content.contains("response=\"200\";")) {// new
                        ipCameraHandler.newInstarApi = true;
                        ipCameraHandler.logger.debug("Alarm server successfully setup for a 2k+ Instar camera");
                        if (ipCameraHandler.cameraConfig.getFfmpegInput().isEmpty()) {
                            ipCameraHandler.rtspUri = "rtsp://" + ipCameraHandler.cameraConfig.getIp()
                                    + "/livestream/12";
                        }
                        if (ipCameraHandler.cameraConfig.getMjpegUrl().isEmpty()) {
                            ipCameraHandler.mjpegUri = "/livestream/12?action=play&media=mjpeg";
                        }
                        if (ipCameraHandler.cameraConfig.getSnapshotUrl().isEmpty()) {
                            ipCameraHandler.snapshotUri = "/snap.cgi?chn=12";
                        }
                    } else if (requestUrl.startsWith("/param.cgi?cmd=setmdalarm&-aname=server2&-switch=on&-interval=1")
                            && content.startsWith("[Succeed]set ok")) {
                        ipCameraHandler.newInstarApi = false;
                        ipCameraHandler.logger.debug("Alarm server successfully setup for a 1080p Instar camera");
                    } else {
                        ipCameraHandler.logger.debug("Unknown reply from URI:{}", requestUrl);
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
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=getaudioalarmattr");
                    break;
                case CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT:
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=getioattr");
                    break;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    if (ipCameraHandler.newInstarApi) {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=getalarmattr");
                    } else {
                        ipCameraHandler.sendHttpGET("/cgi-bin/hi3510/param.cgi?cmd=getmdattr");
                    }
                    break;
                case CHANNEL_ENABLE_PIR_ALARM:
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=getpirattr");
                    break;
                case CHANNEL_AUTO_LED:
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=getinfrared");
                    break;
                case CHANNEL_TEXT_OVERLAY:
                    ipCameraHandler.sendHttpGET("/param.cgi?cmd=getoverlayattr&-region=1");
                    break;
            }
            return;
        } // end of "REFRESH"
        if (ipCameraHandler.newInstarApi) {
            switch (channelUID.getId()) {
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                    if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=0");
                    } else if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setaudioalarmattr&enable=1");
                    } else if (command instanceof PercentType percentCommand) {
                        int value = percentCommand.toBigDecimal().divide(BigDecimal.TEN).intValue();
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
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                    if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/hi3510/param.cgi?cmd=setaudioalarmattr&-aa_enable=0");
                    } else if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/hi3510/param.cgi?cmd=setaudioalarmattr&-aa_enable=1");
                    } else if (command instanceof PercentType percentCommand) {
                        int value = percentCommand.toBigDecimal().divide(BigDecimal.TEN).intValue();
                        ipCameraHandler.sendHttpGET(
                                "/cgi-bin/hi3510/param.cgi?cmd=setaudioalarmattr&-aa_enable=1&-aa_value=" + value * 10);
                    }
                    return;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET("/cgi-bin/hi3510/param.cgi?cmd=setaudioalarmattr&-aa_enable=1");
                    } else {
                        ipCameraHandler.sendHttpGET("/cgi-bin/hi3510/param.cgi?cmd=setaudioalarmattr&-aa_enable=0");
                    }
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET(
                                "/cgi-bin/hi3510/param.cgi?cmd=setmdattr&-enable=1&-name=1&cmd=setmdattr&-enable=1&-name=2&cmd=setmdattr&-enable=1&-name=3&cmd=setmdattr&-enable=1&-name=4");
                    } else {
                        ipCameraHandler.sendHttpGET(
                                "/cgi-bin/hi3510/param.cgi?cmd=setmdattr&-enable=0&-name=1&cmd=setmdattr&-enable=0&-name=2&cmd=setmdattr&-enable=0&-name=3&cmd=setmdattr&-enable=0&-name=4");
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
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setinfrared&-infraredstat=auto");
                    } else {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setinfrared&-infraredstat=close");
                    }
                    return;
                case CHANNEL_ENABLE_PIR_ALARM:
                    if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setpirattr&-pir_enable=1");
                    } else {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setpirattr&-pir_enable=0");
                    }
                    return;
                case CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT:
                    if (OnOffType.ON.equals(command)) {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setioattr&-io_enable=1");
                    } else {
                        ipCameraHandler.sendHttpGET("/param.cgi?cmd=setioattr&-io_enable=0");
                    }
                    return;
            }
        }
    }

    public void alarmTriggered(String alarm) {
        // older cameras placed the & for the first query, whilst newer cameras do not.
        // examples are /instar?&active=6 vs /instar?active=6&object=0
        ipCameraHandler.setChannelState(CHANNEL_LAST_EVENT_DATA, new StringType(alarm));
        String alarmCode = alarm.replaceAll(".+active=", "");
        alarmCode = alarmCode.replaceAll("&.+", "");
        String objectCode = alarm.replaceAll(".+object=", "");
        switch (alarmCode) {
            case "1":// The motion area boxes 1-4
            case "2":
            case "3":
            case "4":
                ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                break;
            case "5":// PIR
                ipCameraHandler.motionDetected(CHANNEL_PIR_ALARM);
                break;
            case "6":// Audio Alarm
                ipCameraHandler.audioDetected();
                break;
            case "7":// Motion area 1 + PIR
            case "8":// Motion area 2 + PIR
            case "9":// Motion area 3 + PIR
            case "10":// Motion area 4 + PIR
                ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
                ipCameraHandler.motionDetected(CHANNEL_PIR_ALARM);
                break;
            default:
                ipCameraHandler.logger.debug("Unknown alarm code:{}", alarmCode);
        }
        if (!objectCode.isEmpty()) {
            switch (objectCode) {
                // person=1, car=2, animal=4 so 1+2+4=7 means one of each.
                case "0":// no object
                    break;
                case "1":
                    ipCameraHandler.motionDetected(CHANNEL_HUMAN_ALARM);
                    break;
                case "2":
                    ipCameraHandler.motionDetected(CHANNEL_CAR_ALARM);
                    break;
                case "3":
                    ipCameraHandler.motionDetected(CHANNEL_HUMAN_ALARM);
                    ipCameraHandler.motionDetected(CHANNEL_CAR_ALARM);
                    break;
                case "4":
                    ipCameraHandler.motionDetected(CHANNEL_ANIMAL_ALARM);
                    break;
                case "5":
                    ipCameraHandler.motionDetected(CHANNEL_HUMAN_ALARM);
                    ipCameraHandler.motionDetected(CHANNEL_ANIMAL_ALARM);
                    break;
                case "6":
                    ipCameraHandler.motionDetected(CHANNEL_CAR_ALARM);
                    ipCameraHandler.motionDetected(CHANNEL_ANIMAL_ALARM);
                    break;
                case "7":
                    ipCameraHandler.motionDetected(CHANNEL_HUMAN_ALARM);
                    ipCameraHandler.motionDetected(CHANNEL_CAR_ALARM);
                    ipCameraHandler.motionDetected(CHANNEL_ANIMAL_ALARM);
                    break;
                default:
                    if (objectCode.startsWith("/instar?")) {
                        return;// has no object due to older Instar camera model
                    }
                    ipCameraHandler.logger.debug("Unknown object detection code:{}", objectCode);
            }
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public ArrayList<String> getLowPriorityRequests() {
        ArrayList<String> lowPriorityRequests = new ArrayList<String>(7);
        lowPriorityRequests.add("/param.cgi?cmd=getaudioalarmattr");
        lowPriorityRequests.add("/cgi-bin/hi3510/param.cgi?cmd=getmdattr");
        if (ipCameraHandler.newInstarApi) {// old API cameras get a error 404 response to this
            lowPriorityRequests.add("/param.cgi?cmd=getalarmattr");
        }
        lowPriorityRequests.add("/param.cgi?cmd=getinfrared");
        lowPriorityRequests.add("/param.cgi?cmd=getoverlayattr&-region=1");
        lowPriorityRequests.add("/param.cgi?cmd=getpirattr");
        lowPriorityRequests.add("/param.cgi?cmd=getioattr"); // ext alarm input on/off
        return lowPriorityRequests;
    }
}
