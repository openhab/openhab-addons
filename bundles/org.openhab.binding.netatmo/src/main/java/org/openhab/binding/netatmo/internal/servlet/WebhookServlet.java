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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

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
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.core.io.rest.WebhookService;
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
    private static final int CLOUD_WEBHOOK_RETRY_DELAY_SECONDS = 30;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final Duration CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW = Duration.ofHours(1);

    private final Map<String, Capability> dataListeners = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);
    private final WebhookApi api;
    private final NADeserializer deserializer;
    private final @Nullable String urlPattern;
    private final @Nullable WebhookService cloudWebhookService;
    private final ScheduledExecutorService scheduler;
    private @Nullable URI currentUri;
    private @Nullable String cloudWebhookPath;
    private @Nullable ScheduledFuture<?> cloudWebhookRefreshJob;
    private volatile boolean disposed;

    private boolean hooked;

    public WebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            WebhookApi api, @Nullable String urlPattern, @Nullable WebhookService cloudWebhookService,
            ScheduledExecutorService scheduler) {
        super(handler, httpService, "webhook");
        this.deserializer = deserializer;
        this.api = api;
        this.urlPattern = urlPattern;
        this.cloudWebhookService = cloudWebhookService;
        this.scheduler = scheduler;
        if (urlPattern == null && cloudWebhookService == null) {
            throw new IllegalArgumentException(
                    "WebhookServlet can not be instanciated without either cloud service, either local url.");
        }
    }

    @Override
    public void startListening() {
        super.startListening();
        if (cloudWebhookService != null) {
            registerCloudWebhook();
        } else {
            registerLocalWebhook();
        }
    }

    private void registerLocalWebhook() {
        String configuredPattern = Objects.requireNonNull(urlPattern);
        try {
            URI uri = UriBuilder.fromUri(configuredPattern.formatted(getPath())).build();
            registerWebhook(uri);
        } catch (UriBuilderException e) {
            logger.warn("Invalid webhook URL pattern: {}", e.getMessage());
            return;
        }
    }

    private synchronized void registerCloudWebhook() {
        WebhookService cloudService = Objects.requireNonNull(cloudWebhookService);

        if (cloudWebhookPath instanceof String oldPath) {
            removeCloudWebhook(cloudService, oldPath);
        }

        String path = getPath() + "/" + UUID.randomUUID();
        try {
            org.openhab.core.io.rest.Webhook webhook = cloudService.requestWebhook(path).get(REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            URI uri = webhook.url().toURI();
            if (!registerWebhook(uri)) {
                removeAndReschedule(cloudService, path);
                return;
            }

            scheduleCloudWebhookRefresh(webhook.expiresAt());
            cloudWebhookPath = path;

            logger.info("Registered Netatmo webhook through openHAB Cloud at {}", uri);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while requesting openHAB Cloud webhook URL");
            removeAndReschedule(cloudService, path);
        } catch (ExecutionException | TimeoutException | URISyntaxException e) {
            logger.warn("Unable to register Netatmo webhook through openHAB Cloud: {}", e.getMessage());
            removeAndReschedule(cloudService, path);
        }
    }

    private synchronized void scheduleCloudWebhookRefresh(Instant expiresAt) {
        cancelCloudWebhookRefresh();
        long delaySeconds = Math.max(60,
                Duration.between(Instant.now(), expiresAt.minus(CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW)).getSeconds());
        scheduleCloudWebhookTask(delaySeconds);
    }

    private synchronized void scheduleCloudWebhookRetry() {
        if (!disposed) {
            cancelCloudWebhookRefresh();
            scheduleCloudWebhookTask(CLOUD_WEBHOOK_RETRY_DELAY_SECONDS);
        }
    }

    private void removeAndReschedule(WebhookService cloudService, String path) {
        removeCloudWebhook(cloudService, path);
        scheduleCloudWebhookRetry();
    }

    @Override
    public synchronized void dispose() {
        disposed = true;
        cancelCloudWebhookRefresh();
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
        String cloudPath = cloudWebhookPath;
        WebhookService cloudService = cloudWebhookService;
        if (cloudPath != null && cloudService != null) {
            removeCloudWebhook(cloudService, cloudPath);
            cloudWebhookPath = null;
        }
        dataListeners.clear();
        super.dispose();
    }

    private boolean registerWebhook(URI uri) {
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

    private void scheduleCloudWebhookTask(long delaySeconds) {
        cloudWebhookRefreshJob = scheduler.schedule(this::registerCloudWebhook, delaySeconds, TimeUnit.SECONDS);
    }

    private synchronized void cancelCloudWebhookRefresh() {
        ScheduledFuture<?> refreshJob = cloudWebhookRefreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(false);
            cloudWebhookRefreshJob = null;
        }
    }

    private void removeCloudWebhook(WebhookService cloudService, String path) {
        cloudService.removeWebhook(path).whenComplete((ignored, error) -> {
            if (error != null) {
                logger.debug("Error removing openHAB Cloud webhook", error);
            }
        });
    }

    @Override
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
            if (dataListeners.get(id) instanceof Capability module) {
                logger.trace("Dispatching webhook event to {}", id);
                module.setNewData(event);
            }
        });
    }

    public void registerDataListener(String id, Capability listener) {
        logger.debug("Registering webhook listener '{}' with capability {}'", id, listener.getClass().getSimpleName());
        dataListeners.put(id, listener);
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
