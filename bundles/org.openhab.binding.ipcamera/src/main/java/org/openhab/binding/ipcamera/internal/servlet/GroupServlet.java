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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraGroupHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupServlet} is responsible for serving files for a rotating feed of multiple cameras back to the Jetty
 * server normally found on port 8080
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class GroupServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final long serialVersionUID = -234658667574L;
    private final IpCameraGroupHandler groupHandler;
    public int snapshotStreamsOpen = 0;
    private final String ipWhitelist;
    private final HttpService httpService;

    public GroupServlet(IpCameraGroupHandler ipCameraGroupHandler, HttpService httpService) {
        groupHandler = ipCameraGroupHandler;
        this.httpService = httpService;
        ipWhitelist = groupHandler.groupConfig.getIpWhitelist();
        try {
            httpService.registerServlet("/ipcamera/" + groupHandler.getThing().getUID().getId(), this, null,
                    httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
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
        if (!"DISABLE".equals(ipWhitelist)) {
            String requestIP = "(" + req.getRemoteHost() + ")";
            if (!ipWhitelist.contains(requestIP)) {
                logger.warn("The request made from {} was not in the whiteList and will be ignored.", requestIP);
                return;
            }
        }
        switch (pathInfo) {
            case "/ipcamera.m3u8":
                if (!groupHandler.hlsTurnedOn) {
                    logger.debug(
                            "HLS requires the groups startStream channel to be turned on first. Just starting it now.");
                    String channelPrefix = "ipcamera:" + groupHandler.getThing().getThingTypeUID() + ":"
                            + groupHandler.getThing().getUID().getId() + ":";
                    groupHandler.handleCommand(new ChannelUID(channelPrefix + CHANNEL_START_STREAM), OnOffType.ON);
                    try {
                        TimeUnit.MILLISECONDS.sleep(HLS_STARTUP_DELAY_MS);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                String playList = groupHandler.getPlayList();
                sendString(resp, playList, "application/x-mpegURL");
                return;
            case "/ipcamera.jpg":
                sendSnapshotImage(resp, "image/jpg");
                return;
            case "/ipcamera.mjpeg":
            case "/snapshots.mjpeg":
                req.getSession().setMaxInactiveInterval(0);
                snapshotStreamsOpen++;
                StreamOutput output = new StreamOutput(resp);
                do {
                    try {
                        output.sendSnapshotBasedFrame(groupHandler.getSnapshot());
                        Thread.sleep(1005);
                    } catch (InterruptedException | IOException e) {
                        // Never stop streaming until IOException. Occurs when browser stops the stream.
                        snapshotStreamsOpen--;
                        if (snapshotStreamsOpen == 0) {
                            logger.debug("All snapshots.mjpeg streams have stopped.");
                        }
                        return;
                    }
                } while (true);
            default:
                // example is "/1ipcameraxx.ts"
                if (pathInfo.endsWith(".ts")) {
                    sendFile(resp, resolveIndexToPath(pathInfo) + pathInfo.substring(2), "video/MP2T");
                }
        }
    }

    private String resolveIndexToPath(String uri) {
        if (!"i".equals(uri.substring(1, 2))) {
            return groupHandler.getOutputFolder(Integer.parseInt(uri.substring(1, 2)));
        }
        return "notFound";
    }

    private void sendFile(HttpServletResponse response, String fileUri, String contentType) throws IOException {
        File file = new File(fileUri);
        if (!file.exists()) {
            logger.warn(
                    "HLS File {} was not found. Try adding a larger -hls_delete_threshold to each cameras HLS out options.",
                    fileUri);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setBufferSize((int) file.length());
        response.setContentType(contentType);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Content-Length", String.valueOf(file.length()));
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
        if (groupHandler.cameraIndex >= groupHandler.cameraOrder.size()) {
            logger.debug("All cameras in this group are OFFLINE and a snapshot was requested.");
            return;
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setContentType(contentType);
        byte[] snapshot = groupHandler.getSnapshot();
        try {
            response.setContentLength(snapshot.length);
            ServletOutputStream servletOut = response.getOutputStream();
            servletOut.write(snapshot);
        } catch (IOException e) {
        }
    }

    private void sendString(HttpServletResponse response, String contents, String contentType) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setContentType(contentType);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "max-age=0, no-cache, no-store");
        byte[] bytes = contents.getBytes();
        try {
            response.setContentLength(bytes.length);
            ServletOutputStream servletOut = response.getOutputStream();
            servletOut.write(bytes);
            servletOut.write("\r\n".getBytes());
        } catch (IOException e) {
        }
    }

    public void dispose() {
        try {
            httpService.unregister("/ipcamera/" + groupHandler.getThing().getUID().getId());
        } catch (IllegalArgumentException e) {
            logger.warn("Unregistration of servlet failed:{}", e.getMessage());
        }
    }
}
