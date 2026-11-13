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
                "{\"extra_params\":{\"boiler_status\":\"boiler_on\",\"device_type\":\"NAPlug\",\"event_type\":\"boiler_event\",\"ts\":1794125778},\"push_type\":\"device_event\",\"user_id\":\"51c55d041877596321000035\",\"device_id\":\"70:ee:50:09:cc:98\",\"home_id\":\"59959297b4809d1aff8b4726\",\"home_name\":\"Orgeval Chauffage\"}");
        processEvent(
                "{\"extra_params\":{\"modules\":[{\"co2\":714,\"co2_calibrating\":false,\"firmware\":300,\"homekit_migrated\":false,\"homekit_paired\":false,\"humidity\":58,\"hw_version\":251,\"id\":\"70:ee:50:00:c1:72\",\"noise_avg\":34,\"noise_current\":34,\"pressure_abs\":1012.8,\"pressure_sea\":1026.3,\"rf_amb_status\":107,\"temperature\":21.3,\"timestamp\":1794124302,\"trend_pressure\":\"stable\",\"trend_temperature\":\"stable\",\"usb_status\":false,\"wifi_status\":57,\"wind_gauge_factory_calibrated\":false},{\"battery_vp\":5837,\"firmware\":53,\"humidity\":93,\"hw_version\":1,\"id\":\"02:00:00:00:bc:dc\",\"last_message\":1794124301,\"last_seen\":1794124281,\"rf_status\":72,\"temperature\":4.4,\"trend_temperature\":\"stable\"},{\"battery_vp\":5342,\"firmware\":14,\"hw_version\":3,\"id\":\"05:00:00:03:3e:ee\",\"last_message\":1794124301,\"last_seen\":1794124294,\"rain\":122,\"rf_status\":70},{\"battery_vp\":5624,\"firmware\":27,\"gustangle\":303,\"gustspeed\":9,\"hw_version\":6,\"id\":\"06:00:00:02:10:de\",\"last_message\":1794124301,\"last_seen\":1794124301,\"rf_status\":72,\"windangle\":251,\"windspeed\":3},{\"battery_vp\":5447,\"co2\":2041,\"firmware\":53,\"humidity\":54,\"hw_version\":1,\"id\":\"03:00:00:05:d1:62\",\"last_message\":1794124301,\"last_seen\":1794124281,\"rf_status\":66,\"temperature\":21.6,\"trend_temperature\":\"up\"}]},\"push_type\":\"device_event\",\"user_id\":\"51c55d041877596321000035\",\"device_id\":\"70:ee:50:00:c1:72\",\"home_id\":\"5842ac41ec135ebca48c078f\",\"home_name\":\"Orgeval Météo\"}");
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
                module.getHandler().setNewData(event);
            }
        });
        EventType eventType = event.getEventType();
        if (EventType.DEVICE_EVENT.equals(eventType) && event.getExtraParams() instanceof WebhookExtraParams whep) {
            EventType extraParamsEventType = whep.eventType;
            ListenerMap listeners = eventListeners.get(extraParamsEventType);
            if (listeners != null) {
                if (!EventType.UNKNOWN.equals(extraParamsEventType)) {
                    listeners.values().forEach(listener -> listener.getHandler().setNewData(whep));
                } else {
                    // no event type is provided, we expect to find a list of modules
                    whep.modules.forEach(module -> {
                        WebhookListenerCapability listener = listeners.get(module.id);
                        if (listener != null) {
                            listener.getHandler().setNewData(module);
                        }
                    });
                }
            }
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
            ListenerMap listeners = eventListeners.get(eventType);
            if (listeners == null) {
                listeners = new ListenerMap();
                eventListeners.put(eventType, listeners);
            }
            listeners.put(id, listener);
        });
    }

    public void unregisterDataListener(String id) {
        dataListeners.remove(id);
        eventListeners.values().forEach(eventListeners -> eventListeners.remove(id));
    }
}
