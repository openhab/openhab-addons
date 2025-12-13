/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.media;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.handler.UnifiProtectCameraHandler;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImageStreamServlet provides a simple image proxy for the camera snapshot image.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(ImageServlet.class);
    private final UnifiMediaService mediaService;

    public ImageServlet(UnifiMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] parts = pathInfo.split("\\/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String thingUID = parts[1].trim();
        UnifiProtectCameraHandler handler = mediaService.getHandler(new ThingUID(thingUID));
        if (handler == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        boolean highQuality = false;
        String queryString = req.getQueryString();
        if (queryString != null && queryString.contains("quality=high")) {
            highQuality = true;
        }
        try {
            byte[] snapshot = handler.getSnapshot(highQuality);
            resp.setContentType("image/jpeg");
            resp.setContentLength(snapshot.length);
            resp.getOutputStream().write(snapshot);
            resp.getOutputStream().flush();
        } catch (IOException e) {
            logger.debug("Error getting snapshot", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
