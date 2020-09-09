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
package org.openhab.binding.ipcamera.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CameraConfig} handles the configuration of cameras.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class CameraConfig {
    String ipAddress = "";
    int port;
    BigDecimal onvifPort = new BigDecimal(0);
    BigDecimal serverPort = new BigDecimal(0);
    String username = "";
    String password = "";
    BigDecimal onvifMediaProfile = new BigDecimal(0);
    BigDecimal pollTime = new BigDecimal(0);
    String ffmpegInput = "";
    String snapshotUrl = "";
    String mjpegUrl = "";
    String alarmInputUrl = "";
    String customMotionAlarmUrl = "";
    String customAudioAlarmUrl = "";
    String updateImageWhen = "";
    boolean updateImage;
    BigDecimal nvrChannel = new BigDecimal(0);
    String ipWhitelist = "";
    String ffmpegLocation = "";
    String ffmpegOutput = "";
    String hlsOutOptions = "";
    String gifOutOptions = "";
    String mp4OutOptions = "";
    String mjpegOptions = "";
    String motionOptions = "";
    boolean ptzContinuous;
    BigDecimal gifPreroll = new BigDecimal(0);
    BigDecimal gifPostroll = new BigDecimal(0);

    public int getOnvifMediaProfile() {
        return onvifMediaProfile.intValue();
    }

    public String getMjpegOptions() {
        return mjpegOptions;
    }

    public String getMotionOptions() {
        return motionOptions;
    }

    public String getMp4OutOptions() {
        return mp4OutOptions;
    }

    public String getGifOutOptions() {
        return gifOutOptions;
    }

    public String getHlsOutOptions() {
        return hlsOutOptions;
    }

    public String getIpWhitelist() {
        return ipWhitelist;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public String getFfmpegOutput() {
        return ffmpegOutput;
    }

    public boolean getUpdateImage() {
        return updateImage;
    }

    public boolean getPtzContinuous() {
        return ptzContinuous;
    }

    public String getAlarmInputUrl() {
        return alarmInputUrl;
    }

    public String getCustomAudioAlarmUrl() {
        return customAudioAlarmUrl;
    }

    public String getCustomMotionAlarmUrl() {
        return customMotionAlarmUrl;
    }

    public int getNvrChannel() {
        return nvrChannel.intValue();
    }

    public String getMjpegUrl() {
        return mjpegUrl;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public String getFfmpegInput() {
        return ffmpegInput;
    }

    public String getUpdateImageWhen() {
        return updateImageWhen;
    }

    public int getPollTime() {
        return pollTime.intValue();
    }

    public int getOnvifPort() {
        return onvifPort.intValue();
    }

    public int getServerPort() {
        return serverPort.intValue();
    }

    public String getIp() {
        return ipAddress;
    }

    public String getUser() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getGifPreroll() {
        return gifPreroll.intValue();
    }

    public int getGifPostroll() {
        return gifPostroll.intValue();
    }

    public int getPort() {
        return port.intValue();
    }
}
