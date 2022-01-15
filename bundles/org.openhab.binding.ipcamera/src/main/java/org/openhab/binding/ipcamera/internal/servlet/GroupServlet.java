/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraGroupHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.osgi.service.http.HttpService;

/**
 * The {@link GroupServlet} is responsible for serving files for a rotating feed of multiple cameras back to the Jetty
 * server normally found on port 8080
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class GroupServlet extends IpCameraServlet {
    private static final long serialVersionUID = -234658667574L;
    private final IpCameraGroupHandler handler;
    public int snapshotStreamsOpen = 0;

    public GroupServlet(IpCameraGroupHandler handler, HttpService httpService) {
        super(handler, httpService);
        this.handler = handler;
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
        if (!"DISABLE".equals(handler.groupConfig.getIpWhitelist())) {
            String requestIP = "(" + req.getRemoteHost() + ")";
            if (!handler.groupConfig.getIpWhitelist().contains(requestIP)) {
                logger.warn("The request made from {} was not in the whiteList and will be ignored.", requestIP);
                return;
            }
        }
        switch (pathInfo) {
            case "/ipcamera.m3u8":
                if (!handler.hlsTurnedOn) {
                    logger.debug(
                            "HLS requires the groups startStream channel to be turned on first. Just starting it now.");
                    String channelPrefix = "ipcamera:" + handler.getThing().getThingTypeUID() + ":"
                            + handler.getThing().getUID().getId() + ":";
                    handler.handleCommand(new ChannelUID(channelPrefix + CHANNEL_START_STREAM), OnOffType.ON);
                    try {
                        TimeUnit.MILLISECONDS.sleep(HLS_STARTUP_DELAY_MS);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                String playList = handler.getPlayList();
                sendString(resp, playList, "application/x-mpegURL");
                return;
            case "/ipcamera.jpg":
                sendSnapshotImage(resp, "image/jpg", handler.getSnapshot());
                return;
            case "/ipcamera.mjpeg":
            case "/snapshots.mjpeg":
                req.getSession().setMaxInactiveInterval(0);
                snapshotStreamsOpen++;
                StreamOutput output = new StreamOutput(resp);
                do {
                    try {
                        output.sendSnapshotBasedFrame(handler.getSnapshot());
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
                    sendFile(resp, pathInfo, "video/MP2T");
                }
        }
    }

    private String resolveIndexToPath(String uri) {
        if (!"i".equals(uri.substring(1, 2))) {
            return handler.getOutputFolder(Integer.parseInt(uri.substring(1, 2)));
        }
        return "notFound";
    }

    @Override
    protected void sendFile(HttpServletResponse response, String filename, String contentType) throws IOException {
        // Ensure no files can be sourced from parent or child folders
        String truncated = filename.substring(filename.lastIndexOf("/"));
        truncated = resolveIndexToPath(truncated) + truncated.substring(2);
        File file = new File(truncated);
        if (!file.exists()) {
            logger.warn(
                    "HLS File {} was not found. Try adding a larger -hls_delete_threshold to each cameras HLS out options.",
                    file.getName());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        super.sendFile(response, truncated, contentType);
    }

    @Override
    protected void sendSnapshotImage(HttpServletResponse response, String contentType, byte[] snapshot) {
        if (handler.cameraIndex >= handler.cameraOrder.size()) {
            logger.debug("All cameras in this group are OFFLINE and a snapshot was requested.");
            return;
        }
        super.sendSnapshotImage(response, contentType, snapshot);
    }
}
