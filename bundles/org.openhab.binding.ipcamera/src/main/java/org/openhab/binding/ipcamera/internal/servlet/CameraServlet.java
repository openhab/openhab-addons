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
package org.openhab.binding.ipcamera.internal.servlet;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.HLS_STARTUP_DELAY_MS;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.ChannelTracking;
import org.openhab.binding.ipcamera.internal.Ffmpeg;
import org.openhab.binding.ipcamera.internal.InstarHandler;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.osgi.service.http.HttpService;

/**
 * The {@link CameraServlet} is responsible for serving files for a single camera back to the Jetty server normally
 * found on port 8080
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class CameraServlet extends IpCameraServlet {
    private static final long serialVersionUID = -134658667574L;
    private static final Dictionary<Object, Object> INIT_PARAMETERS = new Hashtable<>(
            Map.of("async-supported", "true"));

    private final IpCameraHandler handler;
    public OpenStreams openStreams = new OpenStreams();
    private OpenStreams openSnapshotStreams = new OpenStreams();
    private OpenStreams openAutoFpsStreams = new OpenStreams();

    public CameraServlet(IpCameraHandler handler, HttpService httpService) {
        super(handler, httpService, INIT_PARAMETERS);
        this.handler = handler;
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            return;
        }
        switch (pathInfo) {
            case "/ipcamera.jpg":
                // ffmpeg sends data here for ipcamera.mjpeg streams when camera has no native stream.
                ServletInputStream snapshotData = req.getInputStream();
                openStreams.queueFrame(snapshotData.readAllBytes());
                snapshotData.close();
                break;
            case "/snapshot.jpg":
                snapshotData = req.getInputStream();
                handler.processSnapshot(snapshotData.readAllBytes());
                snapshotData.close();
                break;
            case "/OnvifEvent":
                handler.onvifCamera.eventRecieved(req.getReader().toString());
                break;
            default:
                logger.debug("Recieved unknown request \tPOST:{}", pathInfo);
                break;
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            return;
        }
        logger.debug("GET:{}, received from {}", pathInfo, req.getRemoteHost());
        if (!"DISABLE".equals(handler.getWhiteList())) {
            String requestIP = "(" + req.getRemoteHost() + ")";
            if (!handler.getWhiteList().contains(requestIP)) {
                logger.warn("The request made from {} was not in the whiteList and will be ignored.", requestIP);
                return;
            }
        }
        switch (pathInfo) {
            case "/ipcamera.m3u8":
                Ffmpeg localFfmpeg = handler.ffmpegHLS;
                if (localFfmpeg == null) {
                    handler.setupFfmpegFormat(FFmpegFormat.HLS);
                } else if (!localFfmpeg.getIsAlive()) {
                    localFfmpeg.startConverting();
                } else {
                    localFfmpeg.setKeepAlive(8);
                    sendFile(resp, pathInfo, "application/x-mpegURL");
                    return;
                }
                // Allow files to be created, or you get old m3u8 from the last time this ran.
                try {
                    Thread.sleep(HLS_STARTUP_DELAY_MS);
                } catch (InterruptedException e) {
                    return;
                }
                sendFile(resp, pathInfo, "application/x-mpegURL");
                return;
            case "/ipcamera.mpd":
                sendFile(resp, pathInfo, "application/dash+xml");
                return;
            case "/ipcamera.gif":
                sendFile(resp, pathInfo, "image/gif");
                return;
            case "/ipcamera.jpg":
                // Use cached image if recent. Cameras can take > 1sec to send back a reply.
                // Example an Image item/widget may have a 1 second refresh.
                if (handler.ffmpegSnapshotGeneration
                        || Duration.between(handler.currentSnapshotTime, Instant.now()).toMillis() < 1200) {
                    sendSnapshotImage(resp, "image/jpg", handler.getSnapshot());
                } else {
                    handler.getSnapshot();
                    final AsyncContext acontext = req.startAsync(req, resp);
                    acontext.start(new Runnable() {
                        @Override
                        public void run() {
                            Instant startTime = Instant.now();
                            do {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            } // 5 sec timeout OR a new snapshot comes back from camera
                            while (Duration.between(startTime, Instant.now()).toMillis() < 5000
                                    && Duration.between(handler.currentSnapshotTime, Instant.now()).toMillis() > 1200);
                            sendSnapshotImage(resp, "image/jpg", handler.getSnapshot());
                            acontext.complete();
                        }
                    });
                }
                return;
            case "/snapshots.mjpeg":
                handler.streamingSnapshotMjpeg = true;
                handler.startSnapshotPolling();
                StreamOutput output = new StreamOutput(resp);
                openSnapshotStreams.addStream(output);
                do {
                    try {
                        output.sendSnapshotBasedFrame(handler.getSnapshot());
                        Thread.sleep(handler.cameraConfig.getPollTime());
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        openSnapshotStreams.removeStream(output);
                        logger.debug("Now there are {} snapshots.mjpeg streams open.",
                                openSnapshotStreams.getNumberOfStreams());
                        if (openSnapshotStreams.isEmpty()) {
                            handler.streamingSnapshotMjpeg = false;
                            handler.stopSnapshotPolling();
                            logger.debug("All snapshots.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (true);
            case "/ipcamera.mjpeg":
                if (openStreams.isEmpty()) {
                    logger.debug("First stream requested, opening up stream from camera");
                    handler.openCamerasStream();
                    if (handler.mjpegUri.isEmpty() || "ffmpeg".equals(handler.mjpegUri)) {
                        output = new StreamOutput(resp);
                    } else {
                        output = new StreamOutput(resp, handler.mjpegContentType);
                    }
                } else if (handler.mjpegUri.isEmpty() || "ffmpeg".equals(handler.mjpegUri)) {
                    output = new StreamOutput(resp);
                } else {
                    ChannelTracking tracker = handler.channelTrackingMap.get(handler.getTinyUrl(handler.mjpegUri));
                    if (tracker == null || !tracker.getChannel().isOpen()) {
                        logger.debug("Not the first stream requested but the stream from camera was closed");
                        handler.openCamerasStream();
                    }
                    output = new StreamOutput(resp, handler.mjpegContentType);
                }
                openStreams.addStream(output);
                do {
                    try {
                        output.sendFrame();
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        openStreams.removeStream(output);
                        logger.debug("Now there are {} ipcamera.mjpeg streams open.", openStreams.getNumberOfStreams());
                        if (openStreams.isEmpty()) {
                            if (output.isSnapshotBased) {
                                Ffmpeg localMjpeg = handler.ffmpegMjpeg;
                                if (localMjpeg != null) {
                                    localMjpeg.stopConverting();
                                    // Set reference to ffmpegMjpeg to null to prevent automatic reconnection
                                    // in handler's pollCameraRunnable() check for frozen camera
                                    handler.ffmpegMjpeg = null;
                                }
                            } else {
                                handler.closeChannel(handler.getTinyUrl(handler.mjpegUri));
                            }
                            logger.debug("All ipcamera.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (!openStreams.isEmpty());
            case "/autofps.mjpeg":
                handler.streamingAutoFps = true;
                output = new StreamOutput(resp);
                openAutoFpsStreams.addStream(output);
                int counter = 0;
                do {
                    try {
                        if (handler.motionDetected) {
                            output.sendSnapshotBasedFrame(handler.getSnapshot());
                        } // every 8 seconds if no motion or the first three snapshots to fill any FIFO
                        else if (counter % 8 == 0 || counter < 3) {
                            output.sendSnapshotBasedFrame(handler.getSnapshot());
                        }
                        counter++;
                        Thread.sleep(1000);
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        openAutoFpsStreams.removeStream(output);
                        logger.debug("Now there are {} autofps.mjpeg streams open.",
                                openAutoFpsStreams.getNumberOfStreams());
                        if (openAutoFpsStreams.isEmpty()) {
                            handler.streamingAutoFps = false;
                            logger.debug("All autofps.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (true);
            case "/instar":
                InstarHandler instar = new InstarHandler(handler);
                instar.alarmTriggered(pathInfo + "?" + req.getQueryString());
                return;
            default:
                if (pathInfo.endsWith(".ts")) {
                    sendFile(resp, pathInfo, "video/MP2T");
                } else if (pathInfo.endsWith(".gif")) {
                    sendFile(resp, pathInfo, "image/gif");
                } else if (pathInfo.endsWith(".jpg")) {
                    // Allow access to the preroll and postroll jpg files
                    sendFile(resp, pathInfo, "image/jpg");
                } else if (pathInfo.endsWith(".mp4")) {
                    sendFile(resp, pathInfo, "video/mp4");
                }
                return;
        }
    }

    @Override
    protected void sendFile(HttpServletResponse response, String filename, String contentType) throws IOException {
        // Ensure no files can be sourced from parent or child folders
        String truncated = filename.substring(filename.lastIndexOf("/"));
        super.sendFile(response, handler.cameraConfig.getFfmpegOutput() + truncated, contentType);
    }

    @Override
    public void dispose() {
        openStreams.closeAllStreams();
        openSnapshotStreams.closeAllStreams();
        openAutoFpsStreams.closeAllStreams();
        super.dispose();
    }
}
