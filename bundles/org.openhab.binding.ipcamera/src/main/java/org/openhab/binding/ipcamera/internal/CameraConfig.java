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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CameraConfig} handles the configuration of cameras.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class CameraConfig {
    private String ipAddress = "";
    private String ffmpegInputOptions = "";
    private int port;
    private int onvifPort;
    private String username = "";
    private String password = "";
    private int onvifMediaProfile;
    private int pollTime;
    private String ffmpegInput = "";
    private String snapshotUrl = "";
    private String mjpegUrl = "";
    private String alarmInputUrl = "";
    private String customMotionAlarmUrl = "";
    private String customAudioAlarmUrl = "";
    private String updateImageWhen = "";
    private int nvrChannel;
    private String ipWhitelist = "";
    private String ffmpegLocation = "";
    private String ffmpegOutput = "";
    private String hlsOutOptions = "";
    private String gifOutOptions = "";
    private String mp4OutOptions = "";
    private String mjpegOptions = "";
    private String snapshotOptions = "";
    private String motionOptions = "";
    private boolean ptzContinuous;
    private int gifPreroll;

    public int getOnvifMediaProfile() {
        return onvifMediaProfile;
    }

    public String getFfmpegInputOptions() {
        return ffmpegInputOptions;
    }

    public String getMjpegOptions() {
        return mjpegOptions;
    }

    public String getSnapshotOptions() {
        return snapshotOptions;
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

    public void setFfmpegOutput(String path) {
        ffmpegOutput = path;
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
        return nvrChannel;
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
        return pollTime;
    }

    public int getOnvifPort() {
        return onvifPort;
    }

    public String getIp() {
        return ipAddress;
    }

    public String getUser() {
        return username;
    }

    public void setUser(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getGifPreroll() {
        return gifPreroll;
    }

    public int getPort() {
        return port;
    }
}
