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

package org.openhab.binding.ring.internal;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main OSGi service and HTTP servlet for Ring Video
 *
 * @author Peter Mietlowski (zolakk) - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@Component(service = { Servlet.class, RingVideoServlet.class }, immediate = true)
@HttpWhiteboardServletName(SERVLET_VIDEO_PATH)
@HttpWhiteboardServletPattern({ SERVLET_VIDEO_PATH, SERVLET_VIDEO_PATH + "/*" })
@NonNullByDefault
public class RingVideoServlet extends HttpServlet {
    private static final long serialVersionUID = -5592161948589682812L;

    private final Logger logger = LoggerFactory.getLogger(RingVideoServlet.class);

    private final Map<ThingUID, Path> pathRegistrations = new ConcurrentHashMap<>();
    private List<Path> videoPaths = List.of();

    public void addVideoStoragePath(ThingUID thingUID, String pathString) {
        Path path = Paths.get(pathString);
        pathRegistrations.put(thingUID, path);
        determineAllVideoPaths();
    }

    public void removeVideoStoragePath(ThingUID thingUID) {
        pathRegistrations.remove(thingUID);
        determineAllVideoPaths();
    }

    private synchronized void determineAllVideoPaths() {
        videoPaths = pathRegistrations.values().stream().distinct().toList();
    }

    @Override
    protected void doGet(@NonNullByDefault({}) HttpServletRequest request,
            @NonNullByDefault({}) HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (SERVLET_VIDEO_PATH.equals(requestURI)) {
            String responseString = """
                    <html lang="en-US">
                    <head>
                    <title>Ring - Video files</title>
                    </head>
                    <body>
                    <h1>Available video files:</h1>
                    """;

            responseString += videoPaths.stream().flatMap(path -> {
                try (Stream<Path> stream = Files.list(path)) {
                    return stream.toList().stream();
                } catch (IOException ignored) {
                    return Stream.empty();
                }
            }).map(file -> "<a href=\"" + SERVLET_VIDEO_PATH + "/"
                    + URLEncoder.encode(file.getFileName().toString(), StandardCharsets.UTF_8) + "\">"
                    + file.getFileName() + "</a><br>").collect(Collectors.joining());

            responseString += """
                    </body>
                    """;

            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(responseString);
            return;
        }

        String path = request.getRequestURI().substring(0, SERVLET_VIDEO_PATH.length());

        if (!path.equalsIgnoreCase(SERVLET_VIDEO_PATH)) {
            logger.warn("RingVideo: Invalid request received - path = {}", path);
            return;
        }

        String filename = URLDecoder.decode(
                request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1),
                StandardCharsets.UTF_8);

        logger.debug("RingVideo: {} video '{}' requested", request.getMethod(), filename);

        Path fullPath = videoPaths.stream().map(p -> p.resolve(filename)).filter(Files::exists).findFirst()
                .orElse(null);

        if (fullPath != null) {
            String mimeType = URLConnection.guessContentTypeFromName(filename);
            String contentDisposition = String.format("attachment; filename=%s",
                    URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.setHeader("Content-Disposition", contentDisposition);
            response.setContentLength((int) Files.size(fullPath));
            response.setContentType(mimeType);

            response.setHeader("Access-Control-Allow-Origin", "*");

            try (OutputStream out = response.getOutputStream()) {
                Files.copy(fullPath, out);
                out.flush();
            } catch (IOException e) {
                // handle exception
                logger.warn("RingVideo: Unable to process request: {}", e.getMessage());
            }
        } else {
            logger.warn("RingVideo: File not found: {}", filename);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
