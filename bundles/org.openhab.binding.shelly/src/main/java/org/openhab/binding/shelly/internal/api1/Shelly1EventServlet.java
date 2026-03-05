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
package org.openhab.binding.shelly.internal.api1;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly1EventServlet} implements the HttpSocket callback for Gen1 devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebServlet(name = "Shelly1EventServlet", urlPatterns = { SHELLY1_CALLBACK_URI })
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class Shelly1EventServlet extends HttpServlet {
    private static final long serialVersionUID = -1210354558091063207L;
    private final Logger logger = LoggerFactory.getLogger(Shelly1EventServlet.class);

    private final ShellyHandlerFactory handlerFactory;

    @Activate
    public Shelly1EventServlet(@Reference ShellyHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        logger.debug("Shelly1EventServlet started at {}", SHELLY1_CALLBACK_URI);
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Shelly1EventServlet: Stopping");
    }

    /**
     * Servlet handler. HTTP request.
     */
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException, IllegalArgumentException {
        if (request == null) {
            logger.trace("Shelly1EventServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        String path = getString(request.getRequestURI()).toLowerCase(Locale.ROOT);

        // Shelly1: http events, URL looks like
        // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/relay/n?xxxxx or
        // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/roller/n?xxxxx or
        // <ip address>:<remote port>/shelly/event/shellyht-XXXXXX/sensordata?hum=53,temp=26.50
        String deviceName = "";
        String index = "";
        String type = "";
        try {
            String ipAddress = request.getRemoteAddr();
            Map<String, String[]> parameters = request.getParameterMap();
            logger.debug("Shelly1EventServlet: {} Request from {}:{}{}?{}", request.getProtocol(), ipAddress,
                    request.getRemotePort(), path, parameters.toString());
            if (!path.toLowerCase().startsWith(SHELLY1_CALLBACK_URI) || !path.contains("/event/shelly")) {
                logger.warn("Shelly1EventServlet received unknown request: path = {}", path);
                return;
            }

            deviceName = substringBetween(path, "/event/", "/").toLowerCase();
            if (path.contains("/" + EVENT_TYPE_RELAY + "/") || path.contains("/" + EVENT_TYPE_ROLLER + "/")
                    || path.contains("/" + EVENT_TYPE_LIGHT + "/")) {
                index = substringAfterLast(path, "/").toLowerCase();
                type = substringBetween(path, deviceName + "/", "/" + index);
            } else {
                index = "";
                type = substringAfterLast(path, "/").toLowerCase();
            }
            logger.trace("{}: Process event of type type={}, index={}", deviceName, type, index);

            Map<String, String> parms = new TreeMap<>();
            for (Map.Entry<String, String[]> p : parameters.entrySet()) {
                parms.put(p.getKey(), p.getValue()[0]);

            }
            handlerFactory.onEvent(ipAddress, deviceName, index, type, parms);
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Exception processing callback: path={}; index={}, type={}, parameters={}", deviceName,
                    path, index, type, request.getParameterMap().toString());
        } finally {
            if (resp != null) {
                resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                resp.getWriter().write("");
            }
        }
    }
}
