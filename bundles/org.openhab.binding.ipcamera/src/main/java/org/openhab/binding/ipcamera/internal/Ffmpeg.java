/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
 * @author Matthew Skinner - Initial contribution
 * @author Kai Kreuzer - FFmpeg process lifecycle management
 */

@NonNullByDefault
public class Ffmpeg {
    private static final long RESTART_BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(8);

    @FunctionalInterface
    interface ProcessStarter {
        Process start(String[] command) throws IOException;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Object lifecycleLock = new Object();
    private final IpCameraHandler ipCameraHandler;
    private final ProcessStarter processStarter;
    private volatile @Nullable Process process;
    private String ffmpegCommand = "";
    private final FFmpegFormat format;
    private final List<String> commandArrayList = new ArrayList<>();
    private IpCameraFfmpegThread ipCameraFfmpegThread = new IpCameraFfmpegThread();
    private volatile int keepAlive = 8;
    private String password;
    private volatile boolean notFrozen = true;
    private volatile boolean stopRequested;
    private long retryAfterNanos;
    private volatile boolean disposed;

    public Ffmpeg(IpCameraHandler handle, FFmpegFormat format, String ffmpegLocation, String inputArguments,
            String input, String outArguments, String output, String username, String password) {
        this(handle, format, ffmpegLocation, inputArguments, input, outArguments, output, username, password,
                command -> new ProcessBuilder(command).start());
    }

    Ffmpeg(IpCameraHandler handle, FFmpegFormat format, String ffmpegLocation, String inputArguments, String input,
            String outArguments, String output, String username, String password, ProcessStarter processStarter) {
        this.format = format;
        this.password = URLEncoder.encode(password, StandardCharsets.UTF_8);
        this.processStarter = processStarter;

        ipCameraHandler = handle;
        String altInput = input;
        // Input can be snapshots not just rtsp or http
        if (!password.isEmpty() && !input.contains("@") && input.toLowerCase().startsWith("rtsp")) {
            String credentials = username + ":" + this.password + "@";
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
            return; // When set to -1 this will not auto turn off stream.
        }
        keepAlive = numberOfEightSeconds;
    }

    public void checkKeepAlive() {
        if (keepAlive == 1) {
            stopConverting();
        } else if (keepAlive <= -1 && !isAlive()) {
            logger.warn("HLS stream was not running, restarting it now.");
            startConverting();
        }
        if (keepAlive > 0) {
            keepAlive--;
        }
    }

    private class IpCameraFfmpegThread extends Thread {
        private final ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        public int countOfMotions;

        IpCameraFfmpegThread() {
            super(String.format("OH-binding-%s-%s", IpCameraBindingConstants.BINDING_ID, "Ffmpeg"));
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
            Process localProcess = null;
            boolean failed = false;
            try {
                synchronized (lifecycleLock) {
                    if (stopRequested || disposed) {
                        return;
                    }
                    localProcess = processStarter.start(commandArrayList.toArray(String[]::new));
                    process = localProcess;
                }
                try (InputStreamReader reader = new InputStreamReader(localProcess.getErrorStream());
                        BufferedReader bufferedReader = new BufferedReader(reader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (stopRequested || disposed) {
                            break;
                        }
                        handleOutputLine(line);
                    }
                }
                failed = !stopRequested && !disposed && (localProcess.isAlive() || localProcess.exitValue() != 0);
            } catch (IOException | SecurityException e) {
                failed = !stopRequested && !disposed;
                if (failed) {
                    logger.warn("Unable to run FFmpeg conversion for {}.", format);
                }
            } finally {
                if (localProcess != null) {
                    terminateProcess(localProcess);
                }
                synchronized (lifecycleLock) {
                    if (Objects.equals(process, localProcess)) {
                        process = null;
                    }
                    if (failed && !disposed) {
                        retryAfterNanos = System.nanoTime() + RESTART_BACKOFF_NANOS;
                    }
                }
                Runnable callback = switch (format) {
                    case GIF -> this::gifCreated;
                    case RECORD -> this::mp4Created;
                    default -> null;
                };
                if (callback == null || stopRequested || disposed) {
                    threadPool.shutdownNow();
                } else {
                    threadPool.schedule(() -> {
                        try {
                            callback.run();
                        } finally {
                            threadPool.shutdown();
                        }
                    }, 800, TimeUnit.MILLISECONDS);
                }
            }
        }

        private void handleOutputLine(String line) {
            switch (format) {
                case RTSP_ALARMS:
                    if (line.contains("lavfi.")) {
                        if (countOfMotions < 10) {
                            countOfMotions++;
                        }
                        if (countOfMotions > 9) {
                            ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                        } else if (countOfMotions > 4 && ipCameraHandler.motionThreshold.intValue() > 10) {
                            ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                        } else if (countOfMotions > 3 && ipCameraHandler.motionThreshold.intValue() > 15) {
                            ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                        } else if (countOfMotions > 2 && ipCameraHandler.motionThreshold.intValue() > 30) {
                            ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                        } else if (countOfMotions > 0 && ipCameraHandler.motionThreshold.intValue() > 89) {
                            ipCameraHandler.motionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                            countOfMotions = 4;
                        }
                    } else if (line.contains("speed=") && countOfMotions > 0) {
                        if (ipCameraHandler.motionThreshold.intValue() > 89) {
                            countOfMotions--;
                        }
                        if (ipCameraHandler.motionThreshold.intValue() > 10) {
                            countOfMotions -= 2;
                        } else {
                            countOfMotions -= 4;
                        }
                        if (countOfMotions <= 0) {
                            ipCameraHandler.noMotionDetected(CHANNEL_FFMPEG_MOTION_ALARM);
                            countOfMotions = 0;
                        }
                    } else if (line.contains("silence_start")) {
                        ipCameraHandler.noAudioDetected();
                    } else if (line.contains("silence_end")) {
                        ipCameraHandler.audioDetected();
                    }
                    // Fall through: stderr output also signals liveness for these formats.
                case MJPEG:
                case SNAPSHOT:
                    notFrozen = true;
                    break;
                default:
                    break;
            }
        }
    }

    public void startConverting() {
        synchronized (lifecycleLock) {
            if (disposed || ipCameraFfmpegThread.isAlive() || System.nanoTime() < retryAfterNanos) {
                return;
            }
            stopRequested = false;
            ipCameraFfmpegThread = new IpCameraFfmpegThread();
            logger.debug("Starting FFmpeg conversion for {}.", format);
            ipCameraFfmpegThread.start();
            if (format == FFmpegFormat.HLS) {
                ipCameraHandler.setChannelState(CHANNEL_START_STREAM, OnOffType.ON);
            }
        }
        if (keepAlive != -1) {
            keepAlive = 8;
        }
    }

    private static void terminateProcess(Process process) {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        try {
            process.waitFor(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        closeProcessStream(process.getOutputStream());
        closeProcessStream(process.getInputStream());
        closeProcessStream(process.getErrorStream());
    }

    private static void closeProcessStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            // The process may already have closed its streams.
        }
    }

    public boolean isAlive() {
        Process localProcess = process;
        if (localProcess != null) {
            if (localProcess.isAlive() && notFrozen) {
                notFrozen = false; // Any process output will set this back to true before next check.
                return true;
            }
        }
        return false;
    }

    public void stopConverting() {
        Process localProcess;
        IpCameraFfmpegThread localThread;
        synchronized (lifecycleLock) {
            stopRequested = true;
            localProcess = process;
            localThread = ipCameraFfmpegThread;
        }
        if (localProcess != null) {
            logger.debug("Stopping FFmpeg conversion for {}.", format);
            terminateProcess(localProcess);
        }
        if (!Objects.equals(localThread, Thread.currentThread())) {
            try {
                localThread.join(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (lifecycleLock) {
            if (Objects.equals(process, localProcess)) {
                process = null;
            }
        }
        if (format == FFmpegFormat.HLS) {
            ipCameraHandler.setChannelState(CHANNEL_START_STREAM, OnOffType.OFF);
        }
    }

    public void dispose() {
        IpCameraFfmpegThread localThread;
        synchronized (lifecycleLock) {
            disposed = true;
            localThread = ipCameraFfmpegThread;
        }
        stopConverting();
        localThread.threadPool.shutdownNow();
        password = "";
        commandArrayList.clear();
        ffmpegCommand = "";
    }
}
