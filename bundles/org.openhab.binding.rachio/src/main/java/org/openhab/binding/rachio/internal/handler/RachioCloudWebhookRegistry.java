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
import static org.openhab.binding.rachio.internal.RachioUtils.isSameInstance;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    private final Map<String, ActiveConsumerLease> activeConsumers = new HashMap<>();
    private @Nullable Webhook cachedWebhook;
    private @Nullable WebhookService cachedWebhookProvider;
    private @Nullable CompletableFuture<Webhook> pendingRequest;
    private @Nullable WebhookService pendingRequestProvider;
    private CompletableFuture<Boolean> pendingRemoval = CompletableFuture.completedFuture(Boolean.TRUE);
    private long cachedWebhookGeneration;
    private long consumerLeaseSequence;

    public RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this.webhookServiceSupplier = webhookServiceSupplier;
    }

    CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        WebhookService webhookService = webhookServiceSupplier.get();
        if (webhookService == null) {
            throw new CloudWebhookException(WEBHOOK_SERVICE_UNAVAILABLE);
        }

        CompletableFuture<Webhook> webhookFuture;
        CompletableFuture<Boolean> removalBeforeRequest = CompletableFuture.completedFuture(Boolean.TRUE);
        boolean startRequest = false;
        synchronized (this) {
            Webhook webhook = cachedWebhook;
            if (webhook != null && webhookService.equals(cachedWebhookProvider)) {
                return activateLease(consumerId, webhook, cachedWebhookGeneration);
            }

            cachedWebhook = null;
            cachedWebhookProvider = webhookService;
            CompletableFuture<Webhook> request = pendingRequest;
            if (request != null && webhookService.equals(pendingRequestProvider)) {
                webhookFuture = request;
            } else {
                removalBeforeRequest = pendingRemoval;
                webhookFuture = new CompletableFuture<>();
                pendingRequest = webhookFuture;
                pendingRequestProvider = webhookService;
                startRequest = true;
            }
        }
        if (startRequest) {
            startWebhookRequest(webhookService, removalBeforeRequest, webhookFuture);
        }

        try {
            Webhook webhook = webhookFuture.get(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            synchronized (this) {
                Webhook currentWebhook = cachedWebhook;
                if (currentWebhook != null && webhookService.equals(cachedWebhookProvider)) {
                    return activateLease(consumerId, currentWebhook, cachedWebhookGeneration);
                }
                if (!isSameInstance(pendingRequest, webhookFuture) || !webhookService.equals(pendingRequestProvider)) {
                    throw new CloudWebhookException(
                            "openHAB Cloud webhook provider changed while requesting the webhook URL");
                }
                webhookLease(webhook, cachedWebhookGeneration + 1, 0);
                cachedWebhook = webhook;
                cachedWebhookGeneration++;
                pendingRequest = null;
                pendingRequestProvider = null;
                currentWebhook = webhook;
                return activateLease(consumerId, currentWebhook, cachedWebhookGeneration);
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (TimeoutException e) {
            throw new CloudWebhookException("Timed out while requesting openHAB Cloud webhook URL", e);
        } catch (CloudWebhookException e) {
            synchronized (this) {
                if (isSameInstance(pendingRequest, webhookFuture)) {
                    pendingRequest = null;
                    pendingRequestProvider = null;
                }
            }
            throw e;
        } catch (ExecutionException | RuntimeException e) {
            synchronized (this) {
                if (isSameInstance(pendingRequest, webhookFuture)) {
                    pendingRequest = null;
                    pendingRequestProvider = null;
                }
            }
            throw new CloudWebhookException("Failed to request openHAB Cloud webhook URL",
                    unwrapCompletionException(e));
        }
    }

    void release(String consumerId, long consumerLease) {
        CompletableFuture<Boolean> previousRemoval;
        CompletableFuture<Boolean> removalFuture;
        WebhookService removalProvider;
        synchronized (this) {
            ActiveConsumerLease activeLease = activeConsumers.get(consumerId);
            if (activeLease == null || activeLease.consumerLease() != consumerLease) {
                return;
            }
            activeConsumers.remove(consumerId);
            if (!activeConsumers.isEmpty()) {
                return;
            }

            cachedWebhook = null;
            WebhookService webhookService = cachedWebhookProvider;
            if (webhookService == null) {
                webhookService = webhookServiceSupplier.get();
                cachedWebhookProvider = webhookService;
            }
            if (webhookService == null) {
                return;
            }

            removalProvider = webhookService;
            previousRemoval = pendingRemoval;
            removalFuture = new CompletableFuture<>();
            pendingRemoval = removalFuture;
        }
        startWebhookRemoval(removalProvider, previousRemoval, removalFuture);
        removalFuture.whenComplete((ignored, error) -> {
            if (error == null) {
                logger.debug("RachioCloud: openHAB Cloud webhook URL removal completed");
            } else {
                logger.debug("RachioCloud: Failed to remove openHAB Cloud webhook URL",
                        unwrapCompletionException(error));
            }
        });
    }

    private void startWebhookRequest(WebhookService webhookService, CompletableFuture<Boolean> removalBeforeRequest,
            CompletableFuture<Webhook> result) {
        removalBeforeRequest.handle((ignored, error) -> Boolean.TRUE)
                .thenCompose(ignored -> webhookService.requestWebhook(SERVLET_WEBHOOK_PATH))
                .whenComplete((webhook, error) -> {
                    if (error == null) {
                        result.complete(webhook);
                    } else {
                        result.completeExceptionally(unwrapCompletionException(error));
                    }
                });
    }

    private void startWebhookRemoval(WebhookService webhookService, CompletableFuture<Boolean> previousRemoval,
            CompletableFuture<Boolean> result) {
        previousRemoval.handle((ignored, error) -> Boolean.TRUE)
                .thenCompose(ignored -> webhookService.removeWebhook(SERVLET_WEBHOOK_PATH))
                .whenComplete((ignored, error) -> {
                    if (error == null) {
                        result.complete(Boolean.TRUE);
                    } else {
                        result.completeExceptionally(unwrapCompletionException(error));
                    }
                });
    }

    public void clearCachedWebhook() {
        @Nullable
        WebhookService webhookService = webhookServiceSupplier.get();
        synchronized (this) {
            cachedWebhook = null;
            cachedWebhookProvider = webhookService;
            pendingRequest = null;
            pendingRequestProvider = null;
            activeConsumers.clear();
        }
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

    private CloudWebhookLease activateLease(String consumerId, Webhook webhook, long generation)
            throws CloudWebhookException {
        ActiveConsumerLease activeLease = activeConsumers.get(consumerId);
        if (activeLease != null && activeLease.webhookGeneration() == generation) {
            return webhookLease(webhook, generation, activeLease.consumerLease());
        }
        long consumerLease = ++consumerLeaseSequence;
        CloudWebhookLease lease = webhookLease(webhook, generation, consumerLease);
        activeConsumers.put(consumerId, new ActiveConsumerLease(generation, consumerLease));
        logger.debug("RachioCloud: openHAB Cloud webhook URL is available for {} active bridge(s)",
                activeConsumers.size());
        return lease;
    }

    private CloudWebhookLease webhookLease(Webhook webhook, long generation, long consumerLease)
            throws CloudWebhookException {
        String urlString = webhook.url().toString();
        if (urlString.isBlank()) {
            throw new CloudWebhookException("openHAB Cloud webhook URL is blank");
        }
        return new CloudWebhookLease(urlString, webhook.expiresAt(), generation, consumerLease);
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

    record CloudWebhookLease(String url, Instant expiresAt, long generation, long consumerLease) {
    }

    private record ActiveConsumerLease(long webhookGeneration, long consumerLease) {
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
