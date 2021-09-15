/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.Ffmpeg;
import org.openhab.binding.ipcamera.internal.InstarHandler;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CameraServlet} is responsible for serving files for a single camera back to the Jetty server normally
 * found on port 8080
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class CameraServlet extends HttpServlet {
    private static final long serialVersionUID = -23465822674L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IpCameraHandler handler;
    private final HttpService httpService;
    private int autofpsStreamsOpen = 0;
    private int snapshotStreamsOpen = 0;
    public OpenStreams openStreams = new OpenStreams();

    public CameraServlet(IpCameraHandler ipCameraHandler, HttpService httpService) {
        handler = ipCameraHandler;
        this.httpService = httpService;
        startListening();
    }

    public void startListening() {
        try {
            httpService.registerServlet("/ipcamera/" + handler.getThing().getUID().getId(), this, null,
                    httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
        }
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
                sendSnapshotImage(resp, "image/jpg");
                return;
            case "/snapshots.mjpeg":
                req.getSession().setMaxInactiveInterval(0);
                snapshotStreamsOpen++;
                handler.streamingSnapshotMjpeg = true;
                handler.startSnapshotPolling();
                StreamOutput output = new StreamOutput(resp);
                do {
                    try {
                        output.sendSnapshotBasedFrame(handler.getSnapshot());
                        Thread.sleep(1005);
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        snapshotStreamsOpen--;
                        if (snapshotStreamsOpen == 0) {
                            handler.streamingSnapshotMjpeg = false;
                            handler.stopSnapshotPolling();
                            logger.debug("All snapshots.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (true);
            case "/ipcamera.mjpeg":
                req.getSession().setMaxInactiveInterval(0);
                if (handler.mjpegUri.isEmpty() || "ffmpeg".equals(handler.mjpegUri)) {
                    if (openStreams.isEmpty()) {
                        handler.setupFfmpegFormat(FFmpegFormat.MJPEG);
                    }
                    output = new StreamOutput(resp);
                    openStreams.addStream(output);
                } else if (openStreams.isEmpty()) {
                    logger.debug("First stream requested, opening up stream from camera");
                    handler.openCamerasStream();
                    output = new StreamOutput(resp, handler.mjpegContentType);
                    openStreams.addStream(output);
                } else {
                    logger.debug("Not the first stream requested. Stream from camera already open");
                    output = new StreamOutput(resp, handler.mjpegContentType);
                    openStreams.addStream(output);
                }
                do {
                    try {
                        output.sendFrame();
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        openStreams.removeStream(output);
                        if (openStreams.isEmpty()) {
                            if (output.isSnapshotBased) {
                                Ffmpeg localMjpeg = handler.ffmpegMjpeg;
                                if (localMjpeg != null) {
                                    localMjpeg.stopConverting();
                                }
                            } else {
                                handler.closeChannel(handler.getTinyUrl(handler.mjpegUri));
                            }
                            logger.debug("All ipcamera.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (true);
            case "/autofps.mjpeg":
                req.getSession().setMaxInactiveInterval(0);
                autofpsStreamsOpen++;
                handler.streamingAutoFps = true;
                output = new StreamOutput(resp);
                int counter = 0;
                do {
                    try {
                        if (handler.motionDetected) {
                            output.sendSnapshotBasedFrame(handler.getSnapshot());
                        } else if (counter % 8 == 0) {// every 8 seconds if no motion
                            output.sendSnapshotBasedFrame(handler.getSnapshot());
                        }
                        counter++;
                        Thread.sleep(1000);
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        autofpsStreamsOpen--;
                        if (autofpsStreamsOpen == 0) {
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

    private void sendFile(HttpServletResponse response, @Nullable String fileUri, String contentType)
            throws IOException {
        File file = new File(handler.cameraConfig.getFfmpegOutput() + fileUri);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setBufferSize((int) file.length());
        response.setContentType(contentType);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "max-age=0, no-cache, no-store");
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file), (int) file.length());
            output = new BufferedOutputStream(response.getOutputStream(), (int) file.length());
            byte[] buffer = new byte[(int) file.length()];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    private void sendSnapshotImage(HttpServletResponse response, String contentType) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setContentType(contentType);
        byte[] snapshot = handler.getSnapshot();
        if (snapshot.length == 1) {
            logger.warn("ipcamera.jpg was requested but there was no jpg in ram to send.");
            return;
        }
        try {
            response.setContentLength(snapshot.length);
            ServletOutputStream servletOut = response.getOutputStream();
            servletOut.write(snapshot);
        } catch (IOException e) {
        }
    }

    public void dispose() {
        try {
            openStreams.closeAllStreams();
            httpService.unregister("/ipcamera/" + handler.getThing().getUID().getId());
            this.destroy();
        } catch (IllegalArgumentException e) {
            logger.debug("Unregistration of servlet failed:{}", e.getMessage());
        }
    }
}
