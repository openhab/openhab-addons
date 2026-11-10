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
package org.openhab.binding.netatmo.internal.servlet;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WebhookApi;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.dto.Webhook;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.api.dto.WebhookExtraParams;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.capability.WebhookListenerCapability;
import org.openhab.core.common.ThreadPoolManager;
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
    private class ListenerMap extends ConcurrentHashMap<String, WebhookListenerCapability> {
        private static final long serialVersionUID = 1L;

    }

    private static final long serialVersionUID = -354583910860541214L;

    private final ListenerMap dataListeners = new ListenerMap();
    private final Map<EventType, ListenerMap> eventListeners = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);
    private final WebhookApi api;
    private final NADeserializer deserializer;
    private final String webHookUrl;
    private final String webHookPostfix;

    private boolean hookSet = false;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("blbalalal");

    public WebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            WebhookApi api, String webHookUrl, String webHookPostfix) {
        super(handler, httpService, "webhook");
        this.deserializer = deserializer;
        this.api = api;
        this.webHookUrl = webHookUrl;
        this.webHookPostfix = webHookPostfix;
    }

    @Override
    public void startListening() {
        super.startListening();
        URI uri = UriBuilder.fromUri(webHookUrl).path(getPath() + webHookPostfix).build();
        try {
            logger.debug("Getting list of active webhooks");
            String stringUri = uri.toString();
            List<Webhook> currentHooks = api.getWebhooks();
            if (currentHooks.stream().anyMatch(url -> url.getUrl().equals(stringUri))) {
                logger.info("The webhook {} is already present", stringUri);
                hookSet = true;
            } else {
                logger.info("Setting up the webhook {}", stringUri);
                hookSet = api.appendWebhook(uri);
            }
            scheduler.schedule(this::testPush, 15, TimeUnit.SECONDS);
        } catch (NetatmoException e) {
            logger.info("Error setting webhook: {}", e.getMessage());
        }
    }

    private void testPush() {
        processEvent(
                "{\"extra_params\":{\"device_type\":\"NAPlug\",\"event_type\":\"temperature_variation_event\",\"mode\":\"home\",\"room_id\":\"1222145740\",\"setpoint\":21,\"temperature\":20.5,\"ts\":1794124100},\"push_type\":\"device_event\",\"user_id\":\"51c55d041877596321000035\",\"device_id\":\"70:ee:50:09:cc:98\",\"home_id\":\"59959297b4809d1aff8b4726\",\"home_name\":\"Orgeval Chauffage\"}");
        processEvent(
                "{\"extra_params\":{\"boiler_status\":\"boiler_off\",\"device_type\":\"NAPlug\",\"event_type\":\"boiler_event\",\"ts\":1794125778},\"push_type\":\"device_event\",\"user_id\":\"51c55d041877596321000035\",\"device_id\":\"70:ee:50:09:cc:98\",\"home_id\":\"59959297b4809d1aff8b4726\",\"home_name\":\"Orgeval Chauffage\"}");
    }

    @Override
    public void dispose() {
        if (hookSet) {
            logger.info("Releasing webhook");
            try {
                api.dropWebhook();
                hookSet = false;
            } catch (NetatmoException e) {
                logger.warn("Error releasing webhook: {}", e.getMessage());
            }
        }
        dataListeners.clear();
        eventListeners.clear();
        super.dispose();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        replyQuick(resp);
        processEvent(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private void processEvent(String data) {
        if (!data.isEmpty()) {
            logger.debug("Event transmitted on webhook: {}", data);
            try {
                WebhookEvent event = deserializer.deserialize(WebhookEvent.class, data);
                notifyListeners(event);
            } catch (NetatmoException e) {
                logger.debug("Error deserializing webhook data received: {}. {}", data, e.getMessage());
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
            if (dataListeners.get(id) instanceof WebhookListenerCapability module) {
                logger.trace("Dispatching webhook event to {}", id);
                module.setNewData(event);
            }
        });
        EventType eventType = event.getEventType();
        if (EventType.DEVICE_EVENT.equals(eventType) && event.getExtraParams() instanceof WebhookExtraParams whep) {
            ListenerMap listeners = eventListeners.computeIfAbsent(whep.eventType, k -> new ListenerMap());
            listeners.values().forEach(listener -> listener.pushData(whep));
        }
    }

    public void registerDataListener(String id, WebhookListenerCapability listener) {
        logger.debug("*** Registering webhook listener '{}' with capability {}'", id,
                listener.getClass().getSimpleName());
        dataListeners.put(id, listener);
    }

    public void monitorEvents(String id, WebhookListenerCapability listener, Set<EventType> monitoredEvents) {
        monitoredEvents.forEach(eventType -> {
            logger.debug("*** Registering webhook listener '{}' on event '{}' with capability '{}'", id, eventType,
                    listener.getClass().getSimpleName());
            ListenerMap listeners = eventListeners.computeIfAbsent(eventType, k -> new ListenerMap());
            listeners.put(id, listener);
        });
    }

    public void unregisterDataListener(String id) {
        dataListeners.remove(id);
        eventListeners.values().forEach(eventListeners -> eventListeners.remove(id));
    }
}
