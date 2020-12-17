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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ffmpeg} class is responsible for handling multiple ffmpeg conversions which are used for many tasks
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class Ffmpeg {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler ipCameraHandler;
    private @Nullable Process process = null;
    private String ffmpegCommand = "";
    private FFmpegFormat format;
    private List<String> commandArrayList = new ArrayList<String>();
    private IpCameraFfmpegThread ipCameraFfmpegThread = new IpCameraFfmpegThread();
    private int keepAlive = 8;
    private boolean running = false;
    private String password;

    public Ffmpeg(IpCameraHandler handle, FFmpegFormat format, String ffmpegLocation, String inputArguments,
            String input, String outArguments, String output, String username, String password) {
        this.format = format;
        this.password = password;
        ipCameraHandler = handle;
        String altInput = input;
        // Input can be snapshots not just rtsp or http
        if (!password.isEmpty() && !input.contains("@") && input.contains("rtsp")) {
            String credentials = username + ":" + password + "@";
            // will not work for https: but currently binding does not use https
            altInput = input.substring(0, 7) + credentials + input.substring(7);
        }
        if (inputArguments.isEmpty()) {
            ffmpegCommand = "-i " + altInput + " " + outArguments + " " + output;
        } else {
            ffmpegCommand = inputArguments + " -i " + altInput + " " + outArguments + " " + output;
        }
        Collections.addAll(commandArrayList, ffmpegCommand.trim().split("\\s+"));
        // ffmpegLocation may have a space in its folder
        commandArrayList.add(0, ffmpegLocation);
    }

    public void setKeepAlive(int numberOfEightSeconds) {
        // We poll every 8 seconds due to mjpeg stream requirement.
        if (keepAlive == -1 && numberOfEightSeconds > 1) {
            return;// When set to -1 this will not auto turn off stream.
        }
        keepAlive = numberOfEightSeconds;
    }

    public void checkKeepAlive() {
        if (keepAlive <= -1) {
            return;
        } else if (--keepAlive == 0) {
            stopConverting();
        }
    }

    private class IpCameraFfmpegThread extends Thread {
        private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
        public int countOfMotions;

        IpCameraFfmpegThread() {
            setDaemon(true);
        }

        private void gifCreated() {
            // Without a small delay, Pushover sends no file 10% of time.
            ipCameraHandler.setChannelState(CHANNEL_RECORDING_GIF, DecimalType.ZERO);
            ipCameraHandler.setChannelState(CHANNEL_GIF_HISTORY_LENGTH,
                    new DecimalType(++ipCameraHandler.gifHistoryLength));
        }

        private void mp4Created() {
            ipCameraHandler.setChannelState(CHANNEL_RECORDING_MP4, DecimalType.ZERO);
            ipCameraHandler.setChannelState(CHANNEL_MP4_HISTORY_LENGTH,
                    new DecimalType(++ipCameraHandler.mp4HistoryLength));
        }

        @Override
        public void run() {
            try {
                process = Runtime.getRuntime().exec(commandArrayList.toArray(new String[commandArrayList.size()]));
                Process localProcess = process;
                if (localProcess != null) {
                    InputStream errorStream = localProcess.getErrorStream();
                    InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                    BufferedReader bufferedReader = new BufferedReader(errorStreamReader);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (format.equals(FFmpegFormat.RTSP_ALARMS)) {
                            logger.debug("{}", line);
                            if (line.contains("lavfi.")) {
                                if (countOfMotions == 4) {
                                    ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                                } else {
                                    countOfMotions++;
                                }
                            } else if (line.contains("speed=")) {
                                if (countOfMotions > 0) {
                                    countOfMotions--;
                                    countOfMotions--;
                                    if (countOfMotions <= 0) {
                                        ipCameraHandler.noMotionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                                    }
                                }
                            } else if (line.contains("silence_start")) {
                                ipCameraHandler.noAudioDetected();
                            } else if (line.contains("silence_end")) {
                                ipCameraHandler.audioDetected();
                            }
                        } else {
                            logger.debug("{}", line);
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("An error occured trying to process the messages from FFmpeg.");
            } finally {
                switch (format) {
                    case GIF:
                        threadPool.schedule(this::gifCreated, 800, TimeUnit.MILLISECONDS);
                        break;
                    case RECORD:
                        threadPool.schedule(this::mp4Created, 800, TimeUnit.MILLISECONDS);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void startConverting() {
        if (!ipCameraFfmpegThread.isAlive()) {
            ipCameraFfmpegThread = new IpCameraFfmpegThread();
            logger.debug("Starting ffmpeg with this command now:{}", ffmpegCommand.replaceAll(password, "********"));
            ipCameraFfmpegThread.start();
            running = true;
            if (format.equals(FFmpegFormat.HLS)) {
                ipCameraHandler.setChannelState(CHANNEL_START_STREAM, OnOffType.ON);
            }
        }
        if (keepAlive != -1) {
            keepAlive = 8;
        }
    }

    public boolean getIsAlive() {
        return running;
    }

    public void stopConverting() {
        if (ipCameraFfmpegThread.isAlive()) {
            logger.debug("Stopping ffmpeg {} now when keepalive is:{}", format, keepAlive);
            Process localProcess = process;
            if (localProcess != null) {
                localProcess.destroyForcibly();
                running = false;
            }
            if (format.equals(FFmpegFormat.HLS)) {
                if (keepAlive == -1) {
                    logger.warn("HLS stopped when Stream should be running non stop, restarting HLS now.");
                    startConverting();
                    return;
                } else {
                    ipCameraHandler.setChannelState(CHANNEL_START_STREAM, OnOffType.OFF);
                }
            }
            keepAlive = 8;
        }
    }
}
