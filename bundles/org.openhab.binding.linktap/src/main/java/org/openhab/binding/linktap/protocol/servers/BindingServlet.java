/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.servers;

import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.EMPTY_STRING;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.linktap.internal.LinkTapBindingConstants;
import org.openhab.binding.linktap.internal.TransactionProcessor;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.core.thing.Thing;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BindingServlet} defines the request to enable or disable alerts from a given device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class BindingServlet extends HttpServlet {
    private static final long serialVersionUID = -23L;

    private final Logger logger = LoggerFactory.getLogger(BindingServlet.class);

    public static final String SERVLET_URL_WITHOUT_ROOT = "linkTap";
    private static final String SERVLET_URL = "/" + SERVLET_URL_WITHOUT_ROOT;
    @Nullable
    HttpService httpService;

    volatile boolean registered;
    List<Thing> accountHandlers = new ArrayList<>();

    public static final BindingServlet INSTANCE = new BindingServlet();

    public static final BindingServlet getInstance() {
        return INSTANCE;
    }

    public void setHttpService(final HttpService httpService) {
        this.httpService = httpService;
    }

    public static String getServletAddress(final String hostname) {
        final String httpPortStr = System.getProperty("org.osgi.service.http.port");
        final String httpsPortStr = System.getProperty("org.osgi.service.https.port");
        final Logger logger = LoggerFactory.getLogger(BindingServlet.class);
        if (httpPortStr == null || httpPortStr.isEmpty()) {
            logger.warn("HTTP Server port is not running, cannot use for API callbacks");
            if (httpsPortStr != null && !httpsPortStr.isEmpty()) {
                logger.warn("Looks like HTTPS is enabled - the device needs HTTP for efficient comm's");
            }
            return EMPTY_STRING;
        }
        return "http://" + hostname + ":" + httpPortStr + SERVLET_URL;
    }

    public void registerServlet() {
        final HttpService srv = httpService;
        if (!registered && srv != null) {
            try {
                srv.registerServlet(SERVLET_URL, this, null, srv.createDefaultHttpContext());
                registered = true;
                logger.trace("Registered servlet " + SERVLET_URL);
            } catch (NamespaceException | ServletException e) {
                logger.warn("Register servlet failed for {}", SERVLET_URL, e);
            }
        }
    }

    public void unregisterServlet() {
        final HttpService srv = httpService;
        if (registered && srv != null) {
            srv.unregister(SERVLET_URL);
            registered = false;
            logger.trace("Unregistered servlet");
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }
        /*
         * if (BridgeManager.getInstance().isEmpty()) {
         * resp.setStatus(HttpStatus.NOT_FOUND_404);
         * resp.setContentLength(0);
         * resp.flushBuffer();
         * }
         */

        logger.warn("Got GET request from {}", req.getRemoteAddr());
        logger.warn("Got GET request from {}", req.getRemoteHost());

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>" + "My first page" + "</title><head><body>");
        html.append("<h1>" + "Some Heading" + "</h1>");

        html.append("<body><b>Remote Host</b>").append(req.getRemoteHost()).append("</body>");
        html.append("<body><b>Remote Addr</b>").append(req.getRemoteAddr()).append("</body>");
        html.append("<body><b>Remote User</b>").append(req.getRemoteUser()).append("</body>");
        html.append("<body><b>Remote Port</b>").append(req.getRemotePort()).append("</body>");

        if (resp == null) {
            return;
        }

        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            return;
        }
        String uri = requestUri.substring(SERVLET_URL.length());
        String queryString = req.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            uri += "?" + queryString;
        }
        logger.debug("doGet {}", uri);

        // if (!"/".equals(uri)) {
        // String newUri = req.getServletPath() + "/";
        // resp.sendRedirect(newUri);
        // return;
        // }

        html.append("</body></html>");

        resp.addHeader("content-type", "text/html;charset=UTF-8");
        try {
            resp.getWriter().write(html.toString());
        } catch (IOException e) {
            logger.warn("return html failed with uri syntax error", e);
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            return;
        }

        /*
         * if (BridgeManager.getInstance().isEmpty()) {
         * resp.setStatus(HttpStatus.NOT_FOUND_404);
         * resp.setContentLength(0);
         * resp.flushBuffer();
         * }
         */

        // logger.warn("Got request from {}", req.getRemoteAddr());
        // logger.warn("Got request from {}", req.getRemoteHost());
        // Enumeration<String> headers = req.getHeaderNames();
        /*
         * if (headers != null) {
         * for (Iterator<String> it = headers.asIterator(); it.hasNext();) {
         * String header = it.next();
         * // logger.warn("Got header {} with value {}", header, req.getHeader(header));
         * }
         * }
         */

        int bufferSize = 1000; // The payload string is technically limited to 768 characters - this should be enough
                               // for one buffer for the whole lot
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0;) {
            out.append(buffer, 0, numRead);
        }

        String payload = out.toString();
        // logger.warn("Output {}", payload);
        final TLGatewayFrame tlFrame = LinkTapBindingConstants.GSON.fromJson(payload, TLGatewayFrame.class);
        // if (tlFrame.command == DEFAULT_INT) {
        // logger.warn("Unsolicited frame - Mapping to CMD 3");
        // } else {
        String result = "";
        if (tlFrame != null) {
            // logger.warn("Got Command {}", tlFrame.command);
            TransactionProcessor tp = TransactionProcessor.getInstance();
            result = tp.processGwRequest(req.getRemoteAddr(), tlFrame.command, payload);
        }
        // resp.setStatus(HttpServletResponse.SC_OK);
        // resp.setContentType("application/json");
        // resp.setContentLength(result.length());
        // resp.setCharacterEncoding("UTF-8");
        // resp.getWriter().write(result);
        // resp.getWriter().flush();
        // resp.getWriter().close();
        // resp.flushBuffer();
        // }
        // logger.warn("SENDING RESPONSE BODY {}", result);
        // resp.addHeader("content-type", "text/html;charset=UTF-8");
        // try {
        // resp.getWriter().write(result);
        // } catch (IOException e) {
        // logger.warn("return html failed with uri syntax error", e);
        // }
        if (resp == null) {
            return;
        }
        // logger.warn("SENDING RESPONSE BODY 2 {}", result);

        resp.setContentType("application/json");
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().append(result);
        resp.getWriter().close();
    }
}
