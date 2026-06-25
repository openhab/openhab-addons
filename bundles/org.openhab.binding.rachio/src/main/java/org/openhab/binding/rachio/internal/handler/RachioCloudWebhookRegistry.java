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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared owner for the single Rachio servlet path exposed through the openHAB Cloud WebhookService.
 */
@NonNullByDefault
public final class RachioCloudWebhookRegistry {
    private static final long CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(RachioCloudWebhookRegistry.class);
    private final Supplier<@Nullable WebhookService> webhookServiceSupplier;
    private final Set<String> activeConsumers = ConcurrentHashMap.newKeySet();
    private @Nullable Webhook cachedWebhook;
    private @Nullable WebhookService cachedWebhookService;
    private long cachedWebhookGeneration;

    public RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this.webhookServiceSupplier = webhookServiceSupplier;
    }

    synchronized CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        activeConsumers.add(consumerId);

        WebhookService webhookService = webhookServiceSupplier.get();
        if (webhookService == null) {
            throw new CloudWebhookException("WebhookServiceUnavailable");
        }

        Webhook webhook = cachedWebhook;
        if (webhook != null && webhookService.equals(cachedWebhookService)) {
            return new CloudWebhookLease(webhook.url().toExternalForm(), webhook.expiresAt(), cachedWebhookGeneration);
        }

        cachedWebhook = null;
        cachedWebhookService = webhookService;

        CompletableFuture<Webhook> webhookFuture = webhookService.requestWebhook(SERVLET_WEBHOOK_PATH);
        try {
            webhook = webhookFuture.get(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            cachedWebhook = webhook;
            cachedWebhookGeneration++;
            logger.debug("RachioCloud: openHAB Cloud webhook URL is available for {} active bridge(s)",
                    activeConsumers.size());
            return new CloudWebhookLease(webhook.url().toExternalForm(), webhook.expiresAt(), cachedWebhookGeneration);
        } catch (InterruptedException e) {
            webhookFuture.cancel(true);
            throw e;
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            webhookFuture.cancel(true);
            throw new CloudWebhookException(webhookFailureCause(e), e);
        }
    }

    synchronized void release(String consumerId) {
        if (!activeConsumers.remove(consumerId) || !activeConsumers.isEmpty()) {
            return;
        }

        cachedWebhook = null;
        WebhookService webhookService = webhookServiceSupplier.get();
        cachedWebhookService = webhookService;
        if (webhookService == null) {
            return;
        }

        try {
            webhookService.removeWebhook(SERVLET_WEBHOOK_PATH)
                    .whenComplete((ignored, error) -> logger.debug("RachioCloud: openHAB Cloud webhook URL removal {}",
                            error == null ? "completed" : "failed: " + webhookFailureCause(error)));
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: openHAB Cloud webhook URL removal failed: {}", e.getClass().getSimpleName());
        }
    }

    public synchronized void clearCachedWebhook() {
        cachedWebhook = null;
        cachedWebhookService = webhookServiceSupplier.get();
    }

    synchronized void invalidateCachedWebhook(long expectedGeneration) {
        // A stale bridge must not invalidate the replacement already acquired by another bridge.
        if (cachedWebhook != null && cachedWebhookGeneration == expectedGeneration) {
            cachedWebhook = null;
        }
    }

    synchronized int activeConsumerCount() {
        return activeConsumers.size();
    }

    private String webhookFailureCause(Throwable error) {
        Throwable cause = error instanceof ExecutionException && error.getCause() != null ? error.getCause() : error;
        return cause != null ? cause.getClass().getSimpleName() : error.getClass().getSimpleName();
    }

    record CloudWebhookLease(String url, Instant expiresAt, long generation) {
    }

    static final class CloudWebhookException extends Exception {
        private static final long serialVersionUID = 1L;

        private final String diagnosticCause;

        CloudWebhookException(String diagnosticCause) {
            this(diagnosticCause, null);
        }

        CloudWebhookException(String diagnosticCause, @Nullable Throwable cause) {
            super(diagnosticCause, cause);
            this.diagnosticCause = diagnosticCause;
        }

        String diagnosticCause() {
            return diagnosticCause;
        }
    }
}
