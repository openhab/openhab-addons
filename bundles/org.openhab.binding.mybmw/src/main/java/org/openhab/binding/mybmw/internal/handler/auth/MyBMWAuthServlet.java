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
package org.openhab.binding.mybmw.internal.handler.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MyBMWAuthServlet} provides captcha html pages
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class MyBMWAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 3817341543768551687L;
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(MyBMWAuthServlet.class);
    private final @NonNullByDefault({}) ClassLoader classLoader = MyBMWAuthServlet.class.getClassLoader();
    private final HttpService httpService;
    private final MyBMWBridgeHandler bridgeHandler;
    private String path = "";
    private String captchaHtml = "";

    public MyBMWAuthServlet(MyBMWBridgeHandler bridgeHandler, String region, HttpService httpService) {
        this.httpService = httpService;
        this.bridgeHandler = bridgeHandler;
        this.path = MyBMWConstants.LOCAL_OPENHAB_BASE_PATH + bridgeHandler.getThing().getUID().getAsString();
        String captchaTemplate = MyBMWConstants.CAPTCHA_HTML.get(region);
        if (captchaTemplate != null) {
            try (InputStream stream = classLoader.getResourceAsStream(captchaTemplate)) {
                captchaHtml = stream != null ? new String(stream.readAllBytes(), StandardCharsets.UTF_8) : "";
            } catch (IOException e) {
                throw new IllegalArgumentException("No captcha html found for region " + region);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("myBMW auth servlet received GET request {}.", request.getRequestURI());
        StringBuffer requestUrl = request.getRequestURL();
        if (requestUrl != null) {
            response.setContentType(CONTENT_TYPE);
            response.getWriter().append(captchaHtml);
            response.getWriter().close();
        } else {
            logger.warn("Unexpected: GET requestUrl is null");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder buffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            logger.trace("myBMW auth servlet received POST content: {}", buffer.toString());
            this.bridgeHandler.setHCaptchaToken(buffer.toString().trim());
        }
    }

    public void startListening() {
        try {
            httpService.registerServlet(path, this, null, httpService.createDefaultHttpContext());
            logger.info("Registered myBMW servlet at '{}'", path);
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
        }
    }

    public void dispose() {
        logger.debug("Stopping myBMW Servlet {}", path);
        httpService.unregister(path);
        this.destroy();
    }

    public String getPath() {
        return path;
    }
}
