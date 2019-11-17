/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyEventServlet} implements a servlet. which is called by the Shelly device to single events (button,
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

    private @Nullable HttpService httpService;
    private @Nullable ShellyHandlerFactory handlerFactory;

    @SuppressWarnings("null")
    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            httpService.registerServlet(SHELLY_CALLBACK_URI, this, null, httpService.createDefaultHttpContext());
            logger.debug("Shelly: CallbackServlet started at '{}'", SHELLY_CALLBACK_URI);
            // } catch (ServletException | NamespaceException e) {
        } catch (RuntimeException | NamespaceException | ServletException e) {
            logger.warn("Could not start CallbackServlet: {} ({})", e.getMessage(), e.getClass());
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(SHELLY_CALLBACK_URI);
        logger.debug("Shelly: CallbackServlet stopped");
    }

    @SuppressWarnings("null")
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        String data = inputStreamToString(request);
        String path = request.getRequestURI().toLowerCase();
        String deviceName = "";
        String index = "";
        String type = "";

        try {
            String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            Map<String, String[]> parameters = request.getParameterMap();
            logger.debug("CallbackServlet: {} Request from {}:{}{}?{}", request.getProtocol(), ipAddress,
                    request.getRemotePort(), path, parameters.toString());
            if (!path.toLowerCase().startsWith(SHELLY_CALLBACK_URI) || !path.contains("/event/shelly")) {
                logger.debug("ERROR: CallbackServletreceived unknown request- path = {}, data={}", path, data);
                return;
            }

            // URL looks like
            // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/relay/n?xxxxx or
            // <ip address>:<remote port>/shelly/event/shellyrelay-XXXXXX/roller/n?xxxxx or
            // <ip address>:<remote port>/shelly/event/shellyht-XXXXXX/sensordata?hum=53,temp=26.50
            deviceName = StringUtils.substringBetween(path, "/event/", "/").toLowerCase();
            if (path.contains("/" + EVENT_TYPE_RELAY + "/") || path.contains("/" + EVENT_TYPE_ROLLER + "/")
                    || path.contains("/" + EVENT_TYPE_LIGHT + "/")) {
                index = StringUtils.substringAfterLast(path, "/").toLowerCase();
                type = StringUtils.substringBetween(path, deviceName + "/", "/" + index);
            } else {
                index = "";
                type = StringUtils.substringAfterLast(path, "/").toLowerCase();
            }
            logger.trace("Process event of type type={} for device {}, index={}", type, deviceName, index);
            Map<String, String> parms = new HashMap<String, String>();
            for (Map.Entry<String, String[]> p : parameters.entrySet()) {
                parms.put(p.getKey(), p.getValue()[0]);

            }
            handlerFactory.onEvent(deviceName, index, type, parms);

        } catch (RuntimeException e) {
            logger.warn(
                    "ERROR: Exception processing callback: {} ({}), path={}, data='{}'; deviceName={}, index={}, type={}, parameters={}",
                    e.getMessage(), e.getClass(), path, data, deviceName, index, type,
                    request.getParameterMap().toString());
        } finally {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.getWriter().write("");
        }
    }

    @SuppressWarnings("resource")
    private String inputStreamToString(@Nullable HttpServletRequest request) throws IOException {
        @SuppressWarnings("null")
        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setShellyHandlerFactory(ShellyHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        logger.debug("Shelly Binding: HandlerFactory bound");
    }

    public void unsetShellyHandlerFactory(ShellyHandlerFactory handlerFactory) {
        this.handlerFactory = null;
        logger.debug("Shelly Binding: HandlerFactory unbound");
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
        logger.debug("Shelly Binding: httpService bound");
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
        logger.debug("Shelly Binding: httpService unbound");
    }

}
