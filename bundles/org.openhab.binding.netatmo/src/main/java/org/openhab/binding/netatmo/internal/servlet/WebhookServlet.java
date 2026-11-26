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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WebhookApi;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.ServiceError;
import org.openhab.binding.netatmo.internal.api.dto.Webhook;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.ops4j.pax.web.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MediaType;

/**
 * HTTP servlet for Netatmo Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class WebhookServlet extends NetatmoServlet {
    private static final long serialVersionUID = -354583910860541214L;

    private final Map<String, CommonInterface> dataListeners = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);
    private final WebhookApi api;
    private final NADeserializer deserializer;
    private @Nullable URI currentUri;

    private boolean hooked;

    protected WebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            WebhookApi api) {
        super(handler, httpService, "webhook");
        this.deserializer = deserializer;
        this.api = api;
    }

    @Override
    public void startListening() {
        super.startListening();
        registerEndpoint();
    }

    protected abstract void registerEndpoint();

    protected abstract String publishedPath();

    protected void disposeEndpoint() {
    }

    @Override
    public synchronized void dispose() {
        disposeEndpoint();
        if (hooked) {
            logger.info("Releasing webhook");
            try {
                List<Webhook> currentHooks = api.getWebhooks();
                URI uri = currentUri;
                if (uri != null) {
                    currentHooks.removeIf(url -> url.getUri().equals(uri));
                }
                if (currentHooks.isEmpty()) {
                    api.dropWebhook();
                } else {
                    List<URI> otherUris = new ArrayList<>();
                    currentHooks.forEach(hook -> otherUris.add(hook.getUri()));
                    api.setWebhooks(otherUris);
                }
            } catch (NetatmoException e) {
                logger.warn("Error releasing webhook: {}", e.getMessage());
            }
            setHooked(false);
        }
        dataListeners.clear();
        super.dispose();
    }

    protected final boolean registerWebhook(URI uri) {
        try {
            logger.debug("Getting list of active webhooks");
            List<Webhook> currentHooks = api.getWebhooks();
            if (currentHooks.stream().anyMatch(url -> url.getUri().equals(uri))) {
                logger.info("The webhook {} is already present", uri);
                currentUri = uri;
                setHooked(true);
                return true;
            } else {
                logger.info("Setting up the webhook {}", uri);
                boolean registered = api.appendWebhook(uri);
                if (registered) {
                    currentUri = uri;
                }
                setHooked(registered);
                return registered;
            }
        } catch (NetatmoException e) {
            if (ServiceError.WEBHOOK_LIMIT_REACHED.equals(e.getStatusCode())) {
                logger.debug("Another uri is already present, force the new one");
                try {
                    boolean registered = api.setWebhooks(List.of(uri));
                    if (registered) {
                        currentUri = uri;
                    }
                    setHooked(registered);
                    return registered;
                } catch (NetatmoException e1) {
                    logger.warn("Error setting webhook: {}", e1.getMessage());
                }
            } else {
                logger.warn("Error setting webhook: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    @NonNullByDefault({})
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        replyQuick(resp);
        processEvent(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private void processEvent(String data) {
        if (!data.isEmpty()) {
            logger.debug("Event received on webhook: {}", data);
            try {
                WebhookEvent event = deserializer.deserialize(WebhookEvent.class, data);
                switch (event.getEventType()) {
                    case EventType.WEBHOOK_ACTIVATION:
                        setHooked(true);
                        break;
                    case EventType.WEBHOOK_DELETION:
                        setHooked(false);
                        break;
                    default:
                        notifyListeners(event);
                }
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
            if (dataListeners.get(id) instanceof CommonInterface handler
                    && event.getEventType().validFor(handler.getModuleType())) {
                logger.trace("Dispatching webhook event to {}", id);
                handler.setNewData(event.ignoringForThingUpdate());
            }
        });
    }

    public void registerDataListener(CommonInterface handler) {
        String id = handler.getId();
        logger.debug("Registering webhook listener for '{}' ({})", handler.getThing().getLabel(), id);
        dataListeners.put(id, handler);
    }

    public void unregisterDataListener(String id) {
        dataListeners.remove(id);
    }

    private void setHooked(boolean hookSet) {
        if (hooked != hookSet) {
            hooked = hookSet;
            handler.hookState(hooked);
        }
    }

    public boolean hookInstalled() {
        return hooked;
    }
}
