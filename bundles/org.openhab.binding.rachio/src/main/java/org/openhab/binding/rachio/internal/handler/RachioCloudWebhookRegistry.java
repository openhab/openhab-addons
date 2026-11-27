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

import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_CLOUD_WEBHOOK_PATH_PREFIX;
import static org.openhab.binding.rachio.internal.RachioUtils.isSameInstance;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * Shares a Cloud registration between bridges while keeping each acquisition and remote operation independently owned.
 *
 * @author Kovacs Istvan - Initial contribution
 * @author Kovacs Istvan - Cloud webhook recovery
 * @author Kovacs Istvan - Isolated registration paths and acquisition ownership
 */
@NonNullByDefault
public final class RachioCloudWebhookRegistry {
    private static final long CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS = 10;
    private static final long CLOUD_WEBHOOK_CLEANUP_TIMEOUT_SECONDS = 60;
    static final String WEBHOOK_SERVICE_UNAVAILABLE = "openHAB core WebhookService is not available";

    private final Logger logger = LoggerFactory.getLogger(RachioCloudWebhookRegistry.class);
    private final Supplier<@Nullable WebhookService> webhookServiceSupplier;
    private final long requestTimeoutMillis;
    private final long cleanupTimeoutMillis;
    private final Map<String, Map<Long, RequestOperation>> activeConsumers = new HashMap<>();
    private boolean providerManaged;
    private @Nullable WebhookService managedProvider;
    private @Nullable RequestOperation cachedRequest;
    private @Nullable RequestOperation pendingRequest;
    private long providerEpoch;
    private long registrationSequence;
    private long consumerLeaseSequence;

    public RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this(webhookServiceSupplier, TimeUnit.SECONDS.toMillis(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS));
    }

    RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier, long requestTimeoutMillis) {
        this(webhookServiceSupplier, requestTimeoutMillis,
                TimeUnit.SECONDS.toMillis(CLOUD_WEBHOOK_CLEANUP_TIMEOUT_SECONDS));
    }

    RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier, long requestTimeoutMillis,
            long cleanupTimeoutMillis) {
        this.webhookServiceSupplier = webhookServiceSupplier;
        if (requestTimeoutMillis < 0 || cleanupTimeoutMillis < 0) {
            throw new IllegalArgumentException("Cloud webhook timeouts must not be negative");
        }
        this.requestTimeoutMillis = requestTimeoutMillis;
        this.cleanupTimeoutMillis = cleanupTimeoutMillis;
    }

    CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        RequestOperation operation;
        boolean startRequest;
        synchronized (this) {
            WebhookService provider = currentProvider();
            if (provider == null) {
                throw new CloudWebhookException(WEBHOOK_SERVICE_UNAVAILABLE);
            }
            RequestOperation cached = cachedRequest;
            Webhook cachedWebhook = cached == null ? null : cached.webhook;
            if (cached != null && !cached.retired && cached.providerEpoch == providerEpoch
                    && provider.equals(cached.webhookService) && cachedWebhook != null) {
                return activateLease(consumerId, cached, cachedWebhook);
            }
            RequestOperation pending = pendingRequest;
            if (pending != null && !pending.retired && pending.providerEpoch == providerEpoch
                    && provider.equals(pending.webhookService)) {
                operation = pending;
                startRequest = false;
            } else {
                operation = new RequestOperation(provider, providerEpoch, ++registrationSequence);
                pendingRequest = operation;
                startRequest = true;
            }
            operation.waiters++;
        }

        try {
            if (startRequest) {
                startWebhookRequest(operation);
            }
            Webhook webhook = operation.result.get(requestTimeoutMillis, TimeUnit.MILLISECONDS);
            synchronized (this) {
                if (operation.retired || operation.providerEpoch != providerEpoch
                        || !operation.webhookService.equals(currentProvider())
                        || !isSameInstance(pendingRequest, operation) && !isSameInstance(cachedRequest, operation)) {
                    throw new CloudWebhookException(
                            "openHAB Cloud webhook provider changed while requesting the webhook URL");
                }
                CloudWebhookLease lease = activateLease(consumerId, operation, webhook);
                operation.webhook = webhook;
                cachedRequest = operation;
                if (isSameInstance(pendingRequest, operation)) {
                    pendingRequest = null;
                }
                return lease;
            }
        } catch (TimeoutException e) {
            throw new CloudWebhookException("Timed out while requesting openHAB Cloud webhook URL", e);
        } catch (ExecutionException | RuntimeException e) {
            throw new CloudWebhookException("Failed to request openHAB Cloud webhook URL",
                    unwrapCompletionException(e));
        } finally {
            releaseRequestWaiter(operation);
        }
    }

    void release(String consumerId, long consumerLease) {
        RequestOperation operation;
        boolean cleanup;
        synchronized (this) {
            Map<Long, RequestOperation> consumerLeases = activeConsumers.get(consumerId);
            if (consumerLeases == null) {
                return;
            }
            RequestOperation leased = consumerLeases.remove(consumerLease);
            if (leased == null) {
                return;
            }
            operation = leased;
            if (consumerLeases.isEmpty()) {
                activeConsumers.remove(consumerId);
            }
            operation.leases--;
            if (operation.leases == 0 && operation.waiters == 0) {
                retireOperation(operation);
            }
            cleanup = requestCleanupWhenUnused(operation);
        }
        if (cleanup) {
            removeWebhook(operation);
        }
    }

    synchronized boolean isLeaseActive(String consumerId, long consumerLease) {
        Map<Long, RequestOperation> leases = activeConsumers.get(consumerId);
        RequestOperation operation = leases == null ? null : leases.get(consumerLease);
        return operation != null && operation.providerEpoch == providerEpoch
                && operation.webhookService.equals(currentProvider());
    }

    private void startWebhookRequest(RequestOperation operation) {
        synchronized (this) {
            if (operation.retired || operation.providerEpoch != providerEpoch
                    || !operation.webhookService.equals(currentProvider())) {
                operation.result
                        .completeExceptionally(new CloudWebhookException("Cloud webhook request was abandoned"));
                return;
            }
            operation.requestSent = true;
        }
        try {
            operation.webhookService.requestWebhook(operation.localPath).whenComplete((webhook, error) -> {
                boolean cleanupLateResult;
                synchronized (this) {
                    cleanupLateResult = operation.cleanupRequested;
                }
                if (error == null) {
                    operation.result.complete(webhook);
                } else {
                    operation.result.completeExceptionally(unwrapCompletionException(error));
                }
                if (cleanupLateResult) {
                    // An ACK failure does not exclude a remote create after eager cleanup of this unique path.
                    removeWebhook(operation);
                }
            });
        } catch (RuntimeException e) {
            operation.result.completeExceptionally(e);
        }
    }

    private void removeWebhook(RequestOperation operation) {
        try {
            operation.webhookService.removeWebhook(operation.localPath).thenApply(ignored -> Boolean.TRUE)
                    .orTimeout(cleanupTimeoutMillis, TimeUnit.MILLISECONDS).whenComplete((ignored, error) -> {
                        if (error == null) {
                            logger.debug("RachioCloud: Retired openHAB Cloud webhook URL removal completed");
                        } else {
                            logger.debug("RachioCloud: Failed to remove retired openHAB Cloud webhook URL",
                                    unwrapCompletionException(error));
                        }
                    });
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to remove retired openHAB Cloud webhook URL", e);
        }
    }

    public void onProviderChanged(@Nullable WebhookService previousProvider, @Nullable WebhookService newProvider) {
        Set<RequestOperation> retired = new HashSet<>();
        Set<RequestOperation> cleanup = new HashSet<>();
        synchronized (this) {
            providerManaged = true;
            managedProvider = newProvider;
            providerEpoch++;
            RequestOperation cached = cachedRequest;
            if (cached != null) {
                retired.add(cached);
            }
            RequestOperation pending = pendingRequest;
            if (pending != null) {
                retired.add(pending);
            }
            activeConsumers.values().forEach(leases -> retired.addAll(leases.values()));
            activeConsumers.clear();
            for (RequestOperation operation : retired) {
                operation.leases = 0;
                retireOperation(operation);
                if (requestCleanupWhenUnused(operation)) {
                    cleanup.add(operation);
                }
            }
        }
        for (RequestOperation operation : retired) {
            operation.result.completeExceptionally(
                    new CloudWebhookException("openHAB Cloud webhook provider changed while requesting the URL"));
        }
        cleanup.forEach(this::removeWebhook);
    }

    void invalidateCachedWebhook(long expectedGeneration) {
        RequestOperation operation;
        boolean cleanup;
        synchronized (this) {
            RequestOperation cached = cachedRequest;
            if (cached == null || cached.generation != expectedGeneration) {
                return;
            }
            operation = cached;
            retireOperation(operation);
            cleanup = requestCleanupWhenUnused(operation);
        }
        if (cleanup) {
            removeWebhook(operation);
        }
    }

    private void releaseRequestWaiter(RequestOperation operation) {
        boolean cleanup;
        synchronized (this) {
            operation.waiters--;
            if (operation.waiters == 0 && operation.leases == 0) {
                retireOperation(operation);
            }
            cleanup = requestCleanupWhenUnused(operation);
        }
        if (cleanup) {
            removeWebhook(operation);
        }
    }

    private void retireOperation(RequestOperation operation) {
        operation.retired = true;
        if (isSameInstance(cachedRequest, operation)) {
            cachedRequest = null;
        }
        if (isSameInstance(pendingRequest, operation)) {
            pendingRequest = null;
        }
    }

    private boolean requestCleanupWhenUnused(RequestOperation operation) {
        if (!operation.retired || operation.leases != 0 || operation.waiters != 0 || !operation.requestSent
                || operation.cleanupRequested) {
            return false;
        }
        operation.cleanupRequested = true;
        return true;
    }

    private @Nullable WebhookService currentProvider() {
        return providerManaged ? managedProvider : webhookServiceSupplier.get();
    }

    synchronized int activeConsumerCount() {
        return activeConsumers.size();
    }

    private CloudWebhookLease activateLease(String consumerId, RequestOperation operation, Webhook webhook)
            throws CloudWebhookException {
        String urlString = webhook.url().toString();
        if (urlString.isBlank()) {
            throw new CloudWebhookException("openHAB Cloud webhook URL is blank");
        }
        long consumerLease = ++consumerLeaseSequence;
        CloudWebhookLease lease = new CloudWebhookLease(urlString, webhook.expiresAt(), operation.generation,
                consumerLease);
        Map<Long, RequestOperation> consumerLeases = activeConsumers.get(consumerId);
        if (consumerLeases == null) {
            consumerLeases = new HashMap<>();
            activeConsumers.put(consumerId, consumerLeases);
        }
        consumerLeases.put(consumerLease, operation);
        operation.leases++;
        logger.debug("RachioCloud: openHAB Cloud webhook URL is available for {} active bridge(s)",
                activeConsumers.size());
        return lease;
    }

    private Throwable unwrapCompletionException(Throwable error) {
        Throwable cause = error;
        while (cause instanceof ExecutionException || cause instanceof CompletionException) {
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

    private static final class RequestOperation {
        private final WebhookService webhookService;
        private final long providerEpoch;
        private final long generation;
        // Remove-by-path providers cannot fence a late operation on a reused path.
        private final String localPath = SERVLET_CLOUD_WEBHOOK_PATH_PREFIX + UUID.randomUUID();
        private final CompletableFuture<Webhook> result = new CompletableFuture<>();
        private @Nullable Webhook webhook;
        private int waiters;
        private int leases;
        private boolean requestSent;
        private boolean retired;
        private boolean cleanupRequested;

        private RequestOperation(WebhookService webhookService, long providerEpoch, long generation) {
            this.webhookService = webhookService;
            this.providerEpoch = providerEpoch;
            this.generation = generation;
        }
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
