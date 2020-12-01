/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * This servlet will handle the interactions between the sony system and the webpages
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
@Component()
public class SonyServlet extends HttpServlet {

    /** Stupid */
    private static final long serialVersionUID = -8873654812522111922L;

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SonyServlet.class);

    /** The path to the main sony page */
    private static final String SONY_PATH = "/sony";

    /** The path to the main sony application */
    private static final String SONYAPP_PATH = "/sony/app";

    /** The http service */
    private final HttpService httpService;

    /** The websocket client to use */
    private final WebSocketClient webSocketClient;

    /** The GSON to use for serialization */
    private final Gson gson = GsonUtilities.getApiGson();

    /** The scheduler to use to schedule tasks */
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("sony");

    /**
     * Constructs the sony servlet
     * 
     * @param webSocketFactory a non-null websocket factory
     * @param httpService a non-null http service
     */
    @Activate
    public SonyServlet(final @Reference WebSocketFactory webSocketFactory, final @Reference HttpService httpService) {
        Objects.requireNonNull(webSocketFactory, "webSocketFactory cannot be null");
        Objects.requireNonNull(httpService, "httpService cannot be null");

        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
        this.httpService = httpService;
    }

    @Activate
    public void activate() {
        try {
            httpService.registerServlet(SONYAPP_PATH, this, new Hashtable<>(), httpService.createDefaultHttpContext());
            httpService.registerResources(SONY_PATH, "web/sonyapp", httpService.createDefaultHttpContext());
            logger.debug("Started Sony Web service at {}", SONY_PATH);
        } catch (ServletException | NamespaceException e) {
            logger.debug("Exception starting status servlet: {}", e.getMessage(), e);
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    protected void doPost(final @Nullable HttpServletRequest req, final @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        final CommandRequest cmdRqst = gson.fromJson(req.getReader(), CommandRequest.class);

        final String baseUrl = cmdRqst.getBaseUrl();
        if (baseUrl == null || StringUtils.isEmpty(baseUrl)) {
            write(resp, gson.toJson(new CommandResponse(false, "baseUrl is required")));
            return;
        }

        final String serviceName = cmdRqst.getServiceName();
        if (serviceName == null || StringUtils.isEmpty(serviceName)) {
            write(resp, gson.toJson(new CommandResponse(false, "serviceName is required")));
            return;
        }

        final String transportName = cmdRqst.getTransport();
        if (transportName == null || StringUtils.isEmpty(transportName)) {
            write(resp, gson.toJson(new CommandResponse(false, "transport is required")));
            return;
        }

        final String command = cmdRqst.getCommand();
        if (command == null || StringUtils.isEmpty(command)) {
            write(resp, gson.toJson(new CommandResponse(false, "command is required")));
            return;
        }

        final String version = cmdRqst.getVersion();
        if (version == null || StringUtils.isEmpty(version)) {
            write(resp, gson.toJson(new CommandResponse(false, "version is required")));
            return;
        }

        final String parms = cmdRqst.getParms();
        // cannot be null but can be empty
        if (parms == null) {
            write(resp, gson.toJson(new CommandResponse(false, "parms is required")));
            return;
        }

        final SonyTransportFactory factory = new SonyTransportFactory(new URL(baseUrl), gson, webSocketClient,
                scheduler);

        try (final SonyTransport transport = factory.getSonyTransport(serviceName, transportName)) {
            if (transport == null) {
                write(resp, gson.toJson(new CommandResponse(false, "No transport of type: " + transportName)));
                return;
            } else {
                // Use 1 to not conflict with ScalarWebRequest IDs
                final String cmd = "{\"id\":1,\"method\":\"" + command + "\",\"version\":\"" + version
                        + "\",\"params\":[" + parms + "]}";

                final ScalarWebRequest rqst = gson.fromJson(cmd, ScalarWebRequest.class);

                final ScalarWebResult result = transport.execute(rqst, TransportOptionAutoAuth.TRUE);
                if (result.isError()) {
                    write(resp, gson.toJson(new CommandResponse(false,
                            StringUtils.defaultIfEmpty(result.getDeviceErrorDesc(), "failure"))));
                } else {
                    final JsonArray ja = result.getResults();
                    final String resString = ja == null ? null : gson.toJson(ja);
                    write(resp, gson.toJson(new CommandResponse(StringUtils.defaultIfEmpty(resString, "Success"))));
                }
            }
        }
    }

    /**
     * Write a response out to the {@link HttpServletResponse}
     *
     * @param resp the non-null {@link HttpServletResponse}
     * @param str the possibly null, possibly empty string content to write
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(final HttpServletResponse resp, final String str) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        final PrintWriter pw = resp.getWriter();
        if (StringUtils.isEmpty(str)) {
            pw.print("{}");
        } else {
            pw.print(str);
        }

        logger.trace("Sending: {}", str);
        pw.flush();
    }
}
