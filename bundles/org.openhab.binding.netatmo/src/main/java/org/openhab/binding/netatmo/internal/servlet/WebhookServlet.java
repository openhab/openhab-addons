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
package org.openhab.binding.netatmo.internal.servlet;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP servlet for Netatmo Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WebhookServlet extends NetatmoServlet {
    private static final long serialVersionUID = -354583910860541214L;

    private final Map<String, Capability> dataListeners = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);
    private final SecurityApi securityApi;
    private final NADeserializer deserializer;
    private final String webHookUrl;
    private final String webHookPostfix;

    private boolean hookSet = false;

    public WebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            SecurityApi securityApi, String webHookUrl, String webHookPostfix) {
        super(handler, httpService, "webhook");
        this.deserializer = deserializer;
        this.securityApi = securityApi;
        this.webHookUrl = webHookUrl;
        this.webHookPostfix = webHookPostfix;
    }

    @Override
    public void startListening() {
        super.startListening();
        URI uri = UriBuilder.fromUri(webHookUrl).path(getPath() + webHookPostfix).build();
        try {
            logger.info("Setting up WebHook at Netatmo to {}", uri.toString());
            hookSet = securityApi.addwebhook(uri);
        } catch (UriBuilderException e) {
            logger.info("webhookUrl is not a valid URI '{}' : {}", uri, e.getMessage());
        } catch (NetatmoException e) {
            logger.info("Error setting webhook : {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (hookSet) {
            logger.info("Releasing WebHook at Netatmo ");
            try {
                securityApi.dropWebhook();
                hookSet = false;
            } catch (NetatmoException e) {
                logger.warn("Error releasing webhook : {}", e.getMessage());
            }
        }
        super.dispose();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        replyQuick(resp);
        processEvent(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private void processEvent(String data) throws IOException {
        if (!data.isEmpty()) {
            logger.debug("Event transmitted from restService : {}", data);
            try {
                WebhookEvent event = deserializer.deserialize(WebhookEvent.class, data);
                notifyListeners(event);
            } catch (NetatmoException e) {
                logger.debug("Error deserializing webhook data received : {}. {}", data, e.getMessage());
            }
        }
    }

    private void replyQuick(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", HttpMethod.POST);
        resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        resp.setIntHeader("Access-Control-Max-Age", 3600);
        resp.getWriter().write("");
    }

    private void notifyListeners(WebhookEvent event) {
        event.getNAObjectList().forEach(id -> {
            Capability module = dataListeners.get(id);
            if (module != null) {
                logger.trace("Dispatching webhook event to {}", id);
                module.setNewData(event);
            }
        });
    }

    public void registerDataListener(String id, Capability capability) {
        dataListeners.put(id, capability);
    }

    public void unregisterDataListener(String id) {
        dataListeners.remove(id);
    }
}
