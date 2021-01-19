/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.webhook;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.security.SecurityApi;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.utils.BindingUtils;
import org.openhab.core.config.core.Configuration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi service and HTTP servlet for Netatmo Welcome Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = NetatmoServlet.class, configurationPid = "binding.netatmo")
@NonNullByDefault
public class NetatmoServlet extends HttpServlet {
    private static final long serialVersionUID = -354583910860541214L;
    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = "utf-8";

    private final Logger logger = LoggerFactory.getLogger(NetatmoServlet.class);

    private final HttpService httpService;
    private final ApiBridge apiBridge;
    private final Map<String, NetatmoDeviceHandler> dataListeners = new ConcurrentHashMap<>();
    private @Nullable URI webhookURI;

    @Activate
    public NetatmoServlet(@Reference HttpService httpService, @Reference ApiBridge apiBridge,
            ComponentContext componentContext) {
        this.httpService = httpService;
        this.apiBridge = apiBridge;
        try {
            httpService.registerServlet(NETATMO_CALLBACK_URI, this, null, httpService.createDefaultHttpContext());
            logger.debug("Started Netatmo Webhook Servlet at '{}'", NETATMO_CALLBACK_URI);
        } catch (ServletException | NamespaceException e) {
            logger.error("Could not start Netatmo Webhook Servlet : {}", e.getMessage());
        }
        modified(BindingUtils.ComponentContextToMap(componentContext));
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(NETATMO_CALLBACK_URI);
        releaseWebHook();
        logger.debug("Netatmo Webhook Servlet stopped");
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        NetatmoBindingConfiguration configuration = new Configuration(config).as(NetatmoBindingConfiguration.class);
        if (configuration.webHookUrl != null && !configuration.webHookUrl.isEmpty()) {
            String tentative = configuration.webHookUrl + NETATMO_CALLBACK_URI;
            try {
                webhookURI = new URI(tentative);
                String uri = webhookURI.toString();

                logger.info("Setting Netatmo Welcome WebHook to {}", uri);
                SecurityApi api = apiBridge.getRestManager(SecurityApi.class);
                if (api != null) {
                    try {
                        api.addwebhook(uri);
                    } catch (NetatmoException e) {
                        logger.warn("Error setting webhook : {}", e.getMessage());
                    }
                }
            } catch (URISyntaxException e) {
                logger.warn("webhookUrl is not a valid URI '{}' : {}", tentative, e.getMessage());
            }
        }
    }

    private void releaseWebHook() {
        logger.info("Releasing Netatmo Welcome WebHook");
        SecurityApi api = apiBridge.getRestManager(SecurityApi.class);
        if (api != null) {
            try {
                api.dropWebhook();
            } catch (NetatmoException e) {
                logger.warn("Error releasing webhook : {}", e.getMessage());
            }
        }
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req != null && resp != null) {
            String data = inputStreamToString(req.getInputStream());
            if (!data.isEmpty()) {
                logger.debug("Event transmitted from restService : {}", data);
                NAWebhookEvent event = NETATMO_GSON.fromJson(data, NAWebhookEvent.class);
                if (event != null) {
                    NetatmoDeviceHandler targetListener = dataListeners.get(event.getHomeId());
                    if (targetListener != null) {
                        targetListener.setEvent(event);
                    }
                } else {
                    logger.info("Unable to deserialize empty string");
                }
            }
            setHeaders(resp);
            resp.getWriter().write("");
        }
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    public void registerDataListener(String id, NetatmoDeviceHandler dataListener) {
        dataListeners.put(id, dataListener);
    }

    public void unregisterDataListener(NetatmoDeviceHandler dataListener) {
        dataListeners.entrySet().forEach(entry -> {
            if (entry.getValue().equals(dataListener)) {
                dataListeners.remove(entry.getKey());
            }
        });
    }

    private String inputStreamToString(InputStream is) throws IOException {
        String value = "";
        try (Scanner scanner = new Scanner(is)) {
            scanner.useDelimiter("\\A");
            value = scanner.hasNext() ? scanner.next() : "";
        }
        return value;
    }
}
