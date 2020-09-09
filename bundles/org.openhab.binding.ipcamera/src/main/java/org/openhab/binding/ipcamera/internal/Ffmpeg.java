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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
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
    private String ffmpegCommand = "", format = "";
    private String[] commandArray;
    private StreamRunning streamRunning = new StreamRunning();
    private int keepAlive = 8;
    private boolean running = false;

    public void setKeepAlive(int seconds) {
        if (seconds == -1) {
            keepAlive = -1;
        } else {// We now poll every 8 seconds due to mjpeg stream requirement.
            keepAlive = 8; // 64 seconds approx.
        }
    }

    public void checkKeepAlive() {
        if (keepAlive <= -1) {
            return;
        } else if (keepAlive == 0) {
            stopConverting();
        } else {
            keepAlive--;
        }
        return;
    }

    public Ffmpeg(IpCameraHandler handle, IpCameraHandler.ffmpegFormat format, String ffmpegLocation,
            String inputArguments, String input, String outArguments, String output, String username, String password) {
        this.format = format.toString();
        ipCameraHandler = handle;
        String altInput = input;
        // Input can be snapshots not just rtsp or http
        if (!password.isEmpty() && !input.contains("@") && input.contains("rtsp")) {
            String credentials = username + ":" + password + "@";
            // will not work for https: but currently binding does not use https
            altInput = input.substring(0, 7) + credentials + input.substring(7);
        }
        ffmpegCommand = ffmpegLocation + " " + inputArguments + " -i " + altInput + " " + outArguments + " " + output;
        commandArray = ffmpegCommand.trim().split("\\s+");
    }

    @NonNullByDefault
    private class StreamRunning extends Thread {
        public int countOfMotions = 0;

        @Override
        public void run() {
            try {
                process = Runtime.getRuntime().exec(commandArray);
                if (process != null) {
                    InputStream errorStream = process.getErrorStream();
                    InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                    BufferedReader bufferedReader = new BufferedReader(errorStreamReader);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (format.equals("RTSPHELPER")) {
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
                    case "GIF":
                        try {
                            // Without a small delay, Pushover sends no file 10% of time.
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                        }
                        logger.debug("Animated GIF has been created and is ready for use.");
                        ipCameraHandler.setChannelState(CHANNEL_UPDATE_GIF, OnOffType.OFF);
                        ipCameraHandler.setChannelState(CHANNEL_GIF_HISTORY_LENGTH,
                                new DecimalType(++ipCameraHandler.gifHistoryLength));
                        break;
                    case "RECORD":
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                        }
                        logger.debug("MP4 has been created and is ready for use.");
                        ipCameraHandler.setChannelState(CHANNEL_RECORD_MP4, DecimalType.ZERO);
                        ipCameraHandler.setChannelState(CHANNEL_MP4_HISTORY_LENGTH,
                                new DecimalType(++ipCameraHandler.mp4HistoryLength));
                        break;
                }
            }
        }
    }

    public void startConverting() {
        if (!streamRunning.isAlive()) {
            streamRunning = new StreamRunning();
            logger.debug("Starting ffmpeg with this command now:{}", ffmpegCommand);
            streamRunning.start();
            running = true;
            if (format.equals("HLS")) {
                ipCameraHandler.setChannelState(CHANNEL_START_STREAM, OnOffType.ON);
                if (keepAlive > -1) {
                    try {
                        Thread.sleep(4500); // Used for on demand HLS to give ffmpeg time to produce the files needed.
                    } catch (InterruptedException e) {
                    }
                }
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
        if (streamRunning.isAlive()) {
            logger.debug("Stopping ffmpeg {} now", format);
            running = false;
            if (process != null) {
                process.destroyForcibly();
            }
            if (format.equals("HLS")) {
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
