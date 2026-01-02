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
package org.openhab.binding.blink.internal.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.handler.CameraHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThumbnailServlet} class provides the servlet for outputting a camera's current thumbnail.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class ThumbnailServlet extends HttpServlet {

    public static final long serialVersionUID = 666L;

    private final Logger logger = LoggerFactory.getLogger(ThumbnailServlet.class);
    private final HttpService httpService;
    private final CameraHandler cameraHandler;
    private final String servletUrl;

    public ThumbnailServlet(HttpService httpService, CameraHandler cameraHandler) {
        this.httpService = httpService;
        this.cameraHandler = cameraHandler;

        try {
            servletUrl = "/blink/thumbnail/"
                    + URLEncoder.encode(cameraHandler.getThing().getUID().getId(), StandardCharsets.UTF_8);

            Hashtable<Object, Object> initParams = new Hashtable<>();
            initParams.put("servlet-name", servletUrl);

            httpService.registerServlet(servletUrl, this, initParams, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void dispose() {
        httpService.unregister(servletUrl);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (response == null) {
            logger.warn("Ignoring received request without response.");
            return;
        }
        if (request == null) {
            logger.warn("Ignoring illegal request.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        byte[] image = cameraHandler.getThumbnail();
        response.addHeader("content-type", "image/jpeg");
        try {
            OutputStream os = response.getOutputStream();
            os.write(image, 0, image.length);
        } catch (IOException e) {
            logger.warn("return html failed with uri syntax error", e);
        }
    }

    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (response == null) {
            logger.warn("Ignoring received request without response.");
            return;
        }
        if (request == null) {
            logger.warn("Ignoring illegal request.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        doGet(request, response);
    }
}
