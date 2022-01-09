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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyEventServlet} implements a servlet. which is called by the Shelly device to signnal events (button,
 * relay output, sensor data). The binding automatically sets those vent urls on startup (when not disabled in the thing
 * config).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ShellyEventServlet extends HttpServlet {
    private static final long serialVersionUID = 549582869577534569L;
    private final Logger logger = LoggerFactory.getLogger(ShellyEventServlet.class);

    private final HttpService httpService;
    private final ShellyHandlerFactory handlerFactory;

    @Activate
    public ShellyEventServlet(@Reference HttpService httpService, @Reference ShellyHandlerFactory handlerFactory,
            Map<String, Object> config) {
        this.httpService = httpService;
        this.handlerFactory = handlerFactory;
        try {
            httpService.registerServlet(SHELLY_CALLBACK_URI, this, null, httpService.createDefaultHttpContext());
            logger.debug("ShellyEventServlet started at '{}'", SHELLY_CALLBACK_URI);
        } catch (NamespaceException | ServletException | IllegalArgumentException e) {
            logger.warn("Could not start CallbackServlet", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(SHELLY_CALLBACK_URI);
        logger.debug("ShellyEventServlet stopped");
    }

    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException, IllegalArgumentException {
        String path = "";
        String deviceName = "";
        String index = "";
        String type = "";

        if ((request == null) || (resp == null)) {
            logger.debug("request or resp must not be null!");
            return;
        }

        try {
            path = getString(request.getRequestURI()).toLowerCase();
            String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            Map<String, String[]> parameters = request.getParameterMap();
            logger.debug("CallbackServlet: {} Request from {}:{}{}?{}", request.getProtocol(), ipAddress,
                    request.getRemotePort(), path, parameters.toString());
            if (!path.toLowerCase().startsWith(SHELLY_CALLBACK_URI) || !path.contains("/event/shelly")) {
                logger.warn("CallbackServlet received unknown request: path = {}", path);
                return;
            }

            // URL looks like
            // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/relay/n?xxxxx or
            // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/roller/n?xxxxx or
            // <ip address>:<remote port>/shelly/event/shellyht-XXXXXX/sensordata?hum=53,temp=26.50
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
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.getWriter().write("");
        }
    }
}
