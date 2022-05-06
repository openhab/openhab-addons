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
package org.openhab.binding.netatmo.internal.webhook;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.capability.EventCapability;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP servlet for Netatmo Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoServlet extends HttpServlet {
    private static final long serialVersionUID = -354583910860541214L;
    private static final String CALLBACK_URI = "/" + BINDING_ID;

    private final Logger logger = LoggerFactory.getLogger(NetatmoServlet.class);
    private final Map<String, EventCapability> dataListeners = new ConcurrentHashMap<>();
    private final HttpService httpService;
    private final NADeserializer deserializer;
    private final Optional<SecurityApi> securityApi;
    private boolean hookSet = false;

    public NetatmoServlet(HttpService httpService, ApiBridgeHandler apiBridge, String webHookUrl) {
        this.httpService = httpService;
        this.deserializer = apiBridge.getDeserializer();
        this.securityApi = Optional.ofNullable(apiBridge.getRestManager(SecurityApi.class));
        securityApi.ifPresent(api -> {
            try {
                httpService.registerServlet(CALLBACK_URI, this, null, httpService.createDefaultHttpContext());
                logger.debug("Started Netatmo Webhook Servlet at '{}'", CALLBACK_URI);
                URI uri = UriBuilder.fromUri(webHookUrl).path(BINDING_ID).build();
                try {
                    logger.info("Setting Netatmo Welcome WebHook to {}", uri.toString());
                    api.addwebhook(uri);
                    hookSet = true;
                } catch (UriBuilderException e) {
                    logger.info("webhookUrl is not a valid URI '{}' : {}", uri, e.getMessage());
                } catch (NetatmoException e) {
                    logger.info("Error setting webhook : {}", e.getMessage());
                }
            } catch (ServletException | NamespaceException e) {
                logger.warn("Could not start Netatmo Webhook Servlet : {}", e.getMessage());
            }
        });
    }

    public void dispose() {
        securityApi.ifPresent(api -> {
            if (hookSet) {
                logger.info("Releasing Netatmo Welcome WebHook");
                try {
                    api.dropWebhook();
                } catch (NetatmoException e) {
                    logger.warn("Error releasing webhook : {}", e.getMessage());
                }
            }
            httpService.unregister(CALLBACK_URI);
        });
        logger.debug("Netatmo Webhook Servlet stopped");
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req != null && resp != null) {
            String data = inputStreamToString(req.getInputStream());
            if (!data.isEmpty()) {
                logger.debug("Event transmitted from restService : {}", data);
                try {
                    WebhookEvent event = deserializer.deserialize(WebhookEvent.class, data);
                    List<String> tobeNotified = collectNotified(event);
                    dataListeners.keySet().stream().filter(tobeNotified::contains).forEach(id -> {
                        EventCapability module = dataListeners.get(id);
                        if (module != null) {
                            module.setNewData(event);
                        }
                    });
                } catch (NetatmoException e) {
                    logger.info("Error deserializing webhook data received : {}. {}", data, e.getMessage());
                }
            }
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", HttpMethod.POST);
            resp.setIntHeader("Access-Control-Max-Age", 3600);
            resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            resp.getWriter().write("");
        }
    }

    private List<String> collectNotified(WebhookEvent event) {
        List<String> result = new ArrayList<>();
        result.add(event.getCameraId());
        String person = event.getPersonId();
        if (person != null) {
            result.add(person);
        }
        result.addAll(event.getPersons().keySet());
        return result.stream().distinct().collect(Collectors.toList());
    }

    public void registerDataListener(String id, EventCapability dataListener) {
        dataListeners.put(id, dataListener);
    }

    public void unregisterDataListener(EventCapability dataListener) {
        dataListeners.entrySet().removeIf(entry -> entry.getValue().equals(dataListener));
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
