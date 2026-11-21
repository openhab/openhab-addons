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
package org.openhab.binding.atv.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.core.FileHostService;
import org.openhab.binding.atv.internal.client.core.HostedFile;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

/**
 * Hosts local files over openHAB's HTTP server, backing the client library's {@link FileHostService} SPI so
 * AirPlay {@code play_url} can serve a local file to a receiver. Each file is reachable only under a random
 * token, and only while the client holds it open.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(service = { FileHostService.class, Servlet.class })
@HttpWhiteboardServletName(AtvFileHostService.SERVLET_NAME)
@HttpWhiteboardServletPattern(AtvFileHostService.ALIAS + "/*")
public class AtvFileHostService extends HttpServlet implements FileHostService {

    static final String SERVLET_NAME = "atv-media";
    static final String ALIAS = "/atv-media";
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PORT = 8080;

    private final transient NetworkAddressService networkAddressService;
    private final transient BundleContext bundleContext;
    private final Map<String, Path> hostedFiles = new ConcurrentHashMap<>();

    @Activate
    public AtvFileHostService(@Reference NetworkAddressService networkAddressService, BundleContext bundleContext) {
        this.networkAddressService = networkAddressService;
        this.bundleContext = bundleContext;
    }

    @Override
    public HostedFile host(Path file) throws IOException {
        String token = UUID.randomUUID().toString();
        hostedFiles.put(token, file);
        String url = "http://" + hostAddress() + ":" + port() + ALIAS + "/" + token;
        return new HostedFile() {
            @Override
            public String url() {
                return url;
            }

            @Override
            public void close() {
                hostedFiles.remove(token);
            }
        };
    }

    private String hostAddress() {
        String address = networkAddressService.getPrimaryIpv4HostAddress();
        if (address != null) {
            return address;
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private int port() {
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        return port == -1 ? DEFAULT_PORT : port;
    }

    /**
     * Serves files previously published through {@link #host(Path)}.
     */
    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        String pathInfo = req.getPathInfo();
        String token = pathInfo == null ? "" : pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        Path path = hostedFiles.get(token);
        if (path == null || !Files.exists(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String contentType = Files.probeContentType(path);
        resp.setContentType(contentType != null ? contentType : "application/octet-stream");
        resp.setContentLengthLong(Files.size(path));
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(path, out);
        }
    }
}
