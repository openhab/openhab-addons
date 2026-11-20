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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.WebhookApi;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.core.io.rest.WebhookService;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netatmo webhook implementation using an openHAB Cloud webhook URL.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CloudWebhookServlet extends WebhookServlet {
    private static final long serialVersionUID = 1L;
    private static final int CLOUD_WEBHOOK_RETRY_DELAY_SECONDS = 30;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final Duration CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW = Duration.ofHours(1);

    private final Logger logger = LoggerFactory.getLogger(CloudWebhookServlet.class);
    private final WebhookService cloudWebhookService;
    private final ScheduledExecutorService scheduler;
    private volatile boolean disposed;
    private @Nullable ScheduledFuture<?> cloudWebhookRefreshJob;
    private @Nullable String cloudWebhookPath;

    public CloudWebhookServlet(ApiBridgeHandler handler, HttpService httpService, NADeserializer deserializer,
            WebhookApi api, WebhookService cloudWebhookService, ScheduledExecutorService scheduler) {
        super(handler, httpService, deserializer, api);
        this.cloudWebhookService = cloudWebhookService;
        this.scheduler = scheduler;
    }

    @Override
    protected synchronized void registerEndpoint() {
        if (disposed) {
            return;
        }

        if (cloudWebhookPath instanceof String oldPath) {
            removeCloudWebhook(oldPath);
        }

        String path = publishedPath();
        try {
            org.openhab.core.io.rest.Webhook webhook = cloudWebhookService.requestWebhook(path)
                    .get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            URI uri = webhook.url().toURI();
            if (!registerWebhook(uri)) {
                removeAndReschedule(path);
                return;
            }

            scheduleCloudWebhookRefresh(webhook.expiresAt());
            cloudWebhookPath = path;

            logger.info("Registered Netatmo webhook through openHAB Cloud at {}", uri);
        } catch (URISyntaxException e) {
            logger.warn("Error in the path used to register Netatmo webhook: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while requesting openHAB Cloud webhook URL");
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Unable to register Netatmo webhook through openHAB Cloud: {}", e.getMessage());
            removeAndReschedule(path);
        }
    }

    @Override
    protected synchronized void disposeEndpoint() {
        disposed = true;
        cancelCloudWebhookRefresh();
        String cloudPath = cloudWebhookPath;
        if (cloudPath != null) {
            removeCloudWebhook(cloudPath);
            cloudWebhookPath = null;
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

    private void removeAndReschedule(String path) {
        removeCloudWebhook(path);
        scheduleCloudWebhookRetry();
    }

    private void scheduleCloudWebhookTask(long delaySeconds) {
        cloudWebhookRefreshJob = scheduler.schedule(this::registerEndpoint, delaySeconds, TimeUnit.SECONDS);
    }

    private synchronized void cancelCloudWebhookRefresh() {
        ScheduledFuture<?> refreshJob = cloudWebhookRefreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(false);
            cloudWebhookRefreshJob = null;
        }
    }

    private void removeCloudWebhook(String path) {
        cloudWebhookService.removeWebhook(path).whenComplete((ignored, error) -> {
            if (error != null) {
                logger.debug("Error removing openHAB Cloud webhook", error);
            }
        });
    }

    @Override
    protected String publishedPath() {
        return getPath() + "/" + UUID.randomUUID();
    }
}
