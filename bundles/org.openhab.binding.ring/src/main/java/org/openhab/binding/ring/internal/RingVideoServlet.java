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

package org.openhab.binding.ring.internal;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main OSGi service and HTTP servlet for Ring Video
 *
 * @author Peter Mietlowski (zolakk) - Initial contribution
 */
@Component(service = HttpServlet.class)
public class RingVideoServlet extends HttpServlet {

    private static final long serialVersionUID = -5592161948589682812L;

    private final Logger logger = LoggerFactory.getLogger(RingVideoServlet.class);

    private String videoStoragePath;
    /**
     * The RestClient is used to connect to the Ring Account.
     */
    private RestClient restClient;

    public RingVideoServlet() {
    }

    public RingVideoServlet(HttpService httpService, String videoStoragePath) {
        Path path = Paths.get(videoStoragePath);
        FileSystem fs = path.getFileSystem();
        String sep = fs.getSeparator();
        this.videoStoragePath = videoStoragePath + (videoStoragePath.endsWith(sep) ? "" : sep);
        restClient = new RestClient();
        try {
            httpService.registerServlet(SERVLET_VIDEO_PATH, this, null, httpService.createDefaultHttpContext());
        } catch (Exception e) {
            logger.warn("Register servlet fails", e);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            String path = request.getRequestURI().substring(0, SERVLET_VIDEO_PATH.length());
            logger.trace("RingVideo: Reqeust from {}:{}{} ({}:{}, {})", ipAddress, request.getRemotePort(), path,
                    request.getRemoteHost(), request.getServerPort(), request.getProtocol());
            if (!request.getMethod().equalsIgnoreCase(HTTP_METHOD_GET)) {
                logger.error("RingVideo: Unexpected method='{}'", request.getMethod());
            }
            if (!path.equalsIgnoreCase(SERVLET_VIDEO_PATH)) {
                logger.error("RingVideo: Invalid request received - path = {}", path);
                return;
            }

            String uri = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);

            logger.debug("RingVideo: {} video '{}' requested", request.getMethod(), uri);

            String FILE_NAME = videoStoragePath + uri;
            File toBeCopied = new File(FILE_NAME);
            String mimeType = URLConnection.guessContentTypeFromName(toBeCopied.getName());
            String contentDisposition = String.format("attachment; filename=%s", toBeCopied.getName());
            int fileSize = Long.valueOf(toBeCopied.length()).intValue();
            response.setHeader("Content-Disposition", contentDisposition);
            response.setContentLength(fileSize);
            response.setContentType(mimeType);

            response.setHeader("Access-Control-Allow-Origin", "*");

            try (OutputStream out = response.getOutputStream()) {
                Path videoPath = toBeCopied.toPath();
                Files.copy(videoPath, out);
                out.flush();
            } catch (IOException e) {
                // handle exception
                logger.error("RingVideo: Unable to process request: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("RingVideo: Unable to process request: {}", e.getMessage());
        }
    }
}
