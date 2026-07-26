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
import java.util.concurrent.CompletionException;
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
 * Shared owner for the single Rachio servlet path exposed through the openHAB Cloud webhook provider.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public final class RachioCloudWebhookRegistry {
    private static final long CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS = 10;
    static final String WEBHOOK_SERVICE_UNAVAILABLE = "openHAB core WebhookService is not available";

    private final Logger logger = LoggerFactory.getLogger(RachioCloudWebhookRegistry.class);
    private final Supplier<@Nullable WebhookService> webhookServiceSupplier;
    private final Set<String> activeConsumers = ConcurrentHashMap.newKeySet();
    private @Nullable Webhook cachedWebhook;
    private @Nullable WebhookService cachedWebhookProvider;
    private long cachedWebhookGeneration;

    public RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this.webhookServiceSupplier = webhookServiceSupplier;
    }

    synchronized CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        WebhookService webhookService = webhookServiceSupplier.get();
        if (webhookService == null) {
            throw new CloudWebhookException(WEBHOOK_SERVICE_UNAVAILABLE);
        }

        Webhook webhook = cachedWebhook;
        if (webhook != null && webhookService.equals(cachedWebhookProvider)) {
            CloudWebhookLease lease = webhookLease(webhook, cachedWebhookGeneration);
            activeConsumers.add(consumerId);
            return lease;
        }

        cachedWebhook = null;
        cachedWebhookProvider = webhookService;

        CompletableFuture<Webhook> webhookFuture;
        try {
            webhookFuture = webhookService.requestWebhook(SERVLET_WEBHOOK_PATH);
        } catch (RuntimeException e) {
            throw new CloudWebhookException("Failed to request openHAB Cloud webhook URL", e);
        }
        boolean consumerAdded = false;
        try {
            webhook = webhookFuture.get(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            CloudWebhookLease lease = webhookLease(webhook, cachedWebhookGeneration + 1);
            cachedWebhook = webhook;
            cachedWebhookGeneration++;
            consumerAdded = activeConsumers.add(consumerId);
            logger.debug("RachioCloud: openHAB Cloud webhook URL is available for {} active bridge(s)",
                    activeConsumers.size());
            return lease;
        } catch (InterruptedException e) {
            if (consumerAdded) {
                activeConsumers.remove(consumerId);
            }
            webhookFuture.cancel(true);
            throw e;
        } catch (CloudWebhookException | ExecutionException | TimeoutException | RuntimeException e) {
            if (consumerAdded) {
                activeConsumers.remove(consumerId);
            }
            webhookFuture.cancel(true);
            if (e instanceof CloudWebhookException cloudWebhookException) {
                throw cloudWebhookException;
            }
            throw new CloudWebhookException("Failed to request openHAB Cloud webhook URL",
                    unwrapCompletionException(e));
        }
    }

    synchronized void release(String consumerId) {
        if (!activeConsumers.remove(consumerId) || !activeConsumers.isEmpty()) {
            return;
        }

        cachedWebhook = null;
        WebhookService webhookService = webhookServiceSupplier.get();
        cachedWebhookProvider = webhookService;
        if (webhookService == null) {
            return;
        }

        try {
            webhookService.removeWebhook(SERVLET_WEBHOOK_PATH).whenComplete((ignored, error) -> {
                if (error == null) {
                    logger.debug("RachioCloud: openHAB Cloud webhook URL removal completed");
                } else {
                    logger.debug("RachioCloud: Failed to remove openHAB Cloud webhook URL",
                            unwrapCompletionException(error));
                }
            });
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Failed to remove openHAB Cloud webhook URL", e);
        }
    }

    public synchronized void clearCachedWebhook() {
        cachedWebhook = null;
        cachedWebhookProvider = webhookServiceSupplier.get();
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

    private CloudWebhookLease webhookLease(Webhook webhook, long generation) throws CloudWebhookException {
        String urlString = webhook.url().toString();
        if (urlString.isBlank()) {
            throw new CloudWebhookException("openHAB Cloud webhook URL is blank");
        }
        return new CloudWebhookLease(urlString, webhook.expiresAt(), generation);
    }

    private Throwable unwrapCompletionException(Throwable error) {
        Throwable cause = error;
        while (cause instanceof ExecutionException || cause instanceof CompletionException) {
            @Nullable
            Throwable nestedCause = cause.getCause();
            if (nestedCause == null) {
                break;
            }
            cause = nestedCause;
        }
        return cause;
    }

    record CloudWebhookLease(String url, Instant expiresAt, long generation) {
    }

    static final class CloudWebhookException extends Exception {
        private static final long serialVersionUID = 1L;

        CloudWebhookException(String message) {
            super(message);
        }

        CloudWebhookException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
