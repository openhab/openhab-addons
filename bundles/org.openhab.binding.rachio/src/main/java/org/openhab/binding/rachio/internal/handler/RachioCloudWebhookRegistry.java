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
 * @author Kovacs Istvan - Cloud webhook recovery
 */
@NonNullByDefault
public final class RachioCloudWebhookRegistry {
    private static final long CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS = 10;
    private static final long CLOUD_WEBHOOK_REMOVAL_WAIT_TIMEOUT_SECONDS = 60;
    static final String WEBHOOK_SERVICE_UNAVAILABLE = "openHAB core WebhookService is not available";

    private final Logger logger = LoggerFactory.getLogger(RachioCloudWebhookRegistry.class);
    private final Supplier<@Nullable WebhookService> webhookServiceSupplier;
    private final long requestTimeoutMillis;
    private final long removalWaitTimeoutMillis;
    private final Map<String, ActiveConsumerLease> activeConsumers = new HashMap<>();
    private boolean providerManaged;
    private @Nullable WebhookService managedProvider;
    private @Nullable Webhook cachedWebhook;
    private @Nullable WebhookService cachedWebhookProvider;
    private @Nullable RequestOperation pendingRequest;
    private CompletableFuture<Boolean> pendingRemoval = CompletableFuture.completedFuture(Boolean.TRUE);
    private long cachedWebhookGeneration;
    private long consumerLeaseSequence;

    public RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier) {
        this(webhookServiceSupplier, TimeUnit.SECONDS.toMillis(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS));
    }

    RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier, long requestTimeoutMillis) {
        this(webhookServiceSupplier, requestTimeoutMillis,
                TimeUnit.SECONDS.toMillis(CLOUD_WEBHOOK_REMOVAL_WAIT_TIMEOUT_SECONDS));
    }

    RachioCloudWebhookRegistry(Supplier<@Nullable WebhookService> webhookServiceSupplier, long requestTimeoutMillis,
            long removalWaitTimeoutMillis) {
        this.webhookServiceSupplier = webhookServiceSupplier;
        if (requestTimeoutMillis < 0 || removalWaitTimeoutMillis < 0) {
            throw new IllegalArgumentException("Cloud webhook timeouts must not be negative");
        }
        this.requestTimeoutMillis = requestTimeoutMillis;
        this.removalWaitTimeoutMillis = removalWaitTimeoutMillis;
    }

    CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        RequestOperation operation;
        CompletableFuture<Boolean> removalBeforeRequest = CompletableFuture.completedFuture(Boolean.TRUE);
        boolean startRequest = false;
        WebhookService webhookService;
        synchronized (this) {
            WebhookService currentProvider = currentProvider();
            if (currentProvider == null) {
                throw new CloudWebhookException(WEBHOOK_SERVICE_UNAVAILABLE);
            }
            webhookService = currentProvider;
            Webhook webhook = cachedWebhook;
            if (webhook != null && webhookService.equals(cachedWebhookProvider)) {
                return activateLease(consumerId, webhook, cachedWebhookGeneration);
            }

            cachedWebhook = null;
            cachedWebhookProvider = webhookService;
            RequestOperation request = pendingRequest;
            if (request != null && webhookService.equals(request.webhookService)) {
                operation = request;
            } else {
                removalBeforeRequest = pendingRemoval;
                operation = new RequestOperation(webhookService);
                pendingRequest = operation;
                startRequest = true;
            }
        }
        if (startRequest) {
            startWebhookRequest(operation, removalBeforeRequest);
        }

        boolean requestStarted = false;
        try {
            operation.started.get(removalWaitTimeoutMillis, TimeUnit.MILLISECONDS);
            requestStarted = true;
            Webhook webhook = operation.result.get(requestTimeoutMillis, TimeUnit.MILLISECONDS);
            synchronized (this) {
                Webhook currentWebhook = cachedWebhook;
                if (currentWebhook != null && webhookService.equals(cachedWebhookProvider)) {
                    return activateLease(consumerId, currentWebhook, cachedWebhookGeneration);
                }
                if (!isSameInstance(pendingRequest, operation) || !webhookService.equals(currentProvider())) {
                    throw new CloudWebhookException(
                            "openHAB Cloud webhook provider changed while requesting the webhook URL");
                }
                webhookLease(webhook, cachedWebhookGeneration + 1, 0);
                cachedWebhook = webhook;
                cachedWebhookGeneration++;
                pendingRequest = null;
                currentWebhook = webhook;
                return activateLease(consumerId, currentWebhook, cachedWebhookGeneration);
            }
        } catch (InterruptedException e) {
            abandonPendingRequest(operation, e);
            throw e;
        } catch (TimeoutException e) {
            abandonPendingRequest(operation, e);
            throw new CloudWebhookException(requestStarted ? "Timed out while requesting openHAB Cloud webhook URL"
                    : "Timed out while waiting for previous openHAB Cloud webhook removal", e);
        } catch (CloudWebhookException e) {
            abandonPendingRequest(operation, e);
            throw e;
        } catch (ExecutionException | RuntimeException e) {
            abandonPendingRequest(operation, e);
            throw new CloudWebhookException("Failed to request openHAB Cloud webhook URL",
                    unwrapCompletionException(e));
        }
    }

    void release(String consumerId, long consumerLease) {
        RemovalOperation removal;
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
                webhookService = currentProvider();
                cachedWebhookProvider = webhookService;
            }
            if (webhookService == null) {
                return;
            }

            removal = prepareWebhookRemoval(webhookService);
        }
        executeWebhookRemoval(removal);
    }

    private RemovalOperation prepareWebhookRemoval(WebhookService webhookService) {
        return prepareWebhookRemoval(webhookService, CompletableFuture.completedFuture(Boolean.TRUE));
    }

    private RemovalOperation prepareWebhookRemoval(WebhookService webhookService,
            CompletableFuture<Boolean> requestFinished) {
        return prepareWebhookRemoval(webhookService, requestFinished, false);
    }

    private RemovalOperation prepareWebhookRemoval(WebhookService webhookService,
            CompletableFuture<Boolean> requestFinished, boolean onlyWhenUnused) {
        CompletableFuture<Boolean> removalFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> previousOperation = pendingRemoval.handle((ignored, error) -> Boolean.TRUE)
                .thenCompose(ignored -> requestFinished);
        RemovalOperation removal = new RemovalOperation(webhookService, previousOperation, removalFuture,
                onlyWhenUnused);
        pendingRemoval = removalFuture;
        return removal;
    }

    private void executeWebhookRemoval(RemovalOperation removal) {
        startWebhookRemoval(removal);
        removal.result().whenComplete((ignored, error) -> {
            if (error == null) {
                logger.debug("RachioCloud: openHAB Cloud webhook URL removal completed");
            } else {
                logger.debug("RachioCloud: Failed to remove openHAB Cloud webhook URL",
                        unwrapCompletionException(error));
            }
        });
    }

    private void startWebhookRequest(RequestOperation operation, CompletableFuture<Boolean> removalBeforeRequest) {
        removalBeforeRequest.handle((ignored, error) -> Boolean.TRUE).thenAccept(ignored -> {
            boolean sendRequest;
            synchronized (this) {
                sendRequest = isSameInstance(pendingRequest, operation) && !operation.result.isDone()
                        && operation.webhookService.equals(currentProvider());
                if (sendRequest) {
                    operation.requestSent = true;
                }
            }
            if (!sendRequest) {
                discardRequestResult(operation,
                        new CloudWebhookException("Cloud webhook request was abandoned before sending"));
                return;
            }
            try {
                operation.webhookService.requestWebhook(SERVLET_WEBHOOK_PATH).whenComplete((webhook, error) -> {
                    try {
                        if (error == null) {
                            if (!operation.result.complete(webhook)) {
                                synchronized (this) {
                                    if (operation.cleanupScheduled) {
                                        return;
                                    }
                                }
                                removeAbandonedWebhook(operation.webhookService);
                            }
                        } else {
                            operation.result.completeExceptionally(unwrapCompletionException(error));
                        }
                    } finally {
                        operation.finished.complete(Boolean.TRUE);
                    }
                });
            } catch (RuntimeException e) {
                operation.result.completeExceptionally(e);
                operation.finished.complete(Boolean.TRUE);
            } finally {
                operation.started.complete(Boolean.TRUE);
            }
        });
    }

    private void startWebhookRemoval(RemovalOperation removal) {
        removal.previousRemoval().handle((ignored, error) -> Boolean.TRUE).thenCompose(ignored -> {
            synchronized (this) {
                if (removal.onlyWhenUnused() && isProviderInUse(removal.webhookService())) {
                    return CompletableFuture.completedFuture(Boolean.TRUE);
                }
            }
            return removal.webhookService().removeWebhook(SERVLET_WEBHOOK_PATH).thenApply(removed -> Boolean.TRUE);
        }).whenComplete((ignored, error) -> {
            if (error == null) {
                removal.result().complete(Boolean.TRUE);
            } else {
                removal.result().completeExceptionally(unwrapCompletionException(error));
            }
        });
    }

    public void onProviderChanged(@Nullable WebhookService previousProvider, @Nullable WebhookService newProvider) {
        RemovalOperation removal = null;
        RequestOperation abandonedRequest;
        synchronized (this) {
            providerManaged = true;
            managedProvider = newProvider;
            WebhookService registeredProvider = cachedWebhookProvider;
            if (registeredProvider == null) {
                registeredProvider = previousProvider;
            }
            boolean registrationMayExist = cachedWebhook != null || !activeConsumers.isEmpty();
            abandonedRequest = pendingRequest;

            cachedWebhook = null;
            cachedWebhookProvider = newProvider;
            pendingRequest = null;
            activeConsumers.clear();

            boolean requestInFlight = abandonedRequest != null && abandonedRequest.requestSent;
            if (abandonedRequest != null && requestInFlight) {
                // Both providers remove by servlet path, so stale cleanup must finish before replacement registration.
                abandonedRequest.cleanupScheduled = true;
                removal = prepareWebhookRemoval(abandonedRequest.webhookService, abandonedRequest.finished);
            } else if (registrationMayExist && registeredProvider != null) {
                removal = prepareWebhookRemoval(registeredProvider);
            }
        }
        if (removal != null) {
            executeWebhookRemoval(removal);
        }
        if (abandonedRequest != null) {
            discardRequestResult(abandonedRequest,
                    new CloudWebhookException("openHAB Cloud webhook provider changed while requesting the URL"));
        }
    }

    private void removeAbandonedWebhook(WebhookService webhookService) {
        RemovalOperation removal;
        synchronized (this) {
            if (isProviderInUse(webhookService)) {
                return;
            }
            removal = prepareWebhookRemoval(webhookService);
        }
        executeWebhookRemoval(removal);
    }

    private boolean isProviderInUse(WebhookService webhookService) {
        RequestOperation request = pendingRequest;
        return webhookService.equals(cachedWebhookProvider) && (cachedWebhook != null || !activeConsumers.isEmpty())
                || request != null && webhookService.equals(request.webhookService);
    }

    private void abandonPendingRequest(RequestOperation request, Throwable failure) {
        RemovalOperation removal = null;
        synchronized (this) {
            if (!isSameInstance(pendingRequest, request)) {
                return;
            }
            pendingRequest = null;
            if (request.requestSent && !request.result.isCompletedExceptionally()) {
                request.cleanupScheduled = true;
                removal = prepareWebhookRemoval(request.webhookService, request.finished, true);
            }
        }
        if (removal != null) {
            executeWebhookRemoval(removal);
        }
        discardRequestResult(request, failure);
    }

    private void discardRequestResult(RequestOperation request, Throwable failure) {
        boolean requestNotSent;
        boolean cleanupScheduled;
        synchronized (this) {
            requestNotSent = !request.requestSent;
            cleanupScheduled = request.cleanupScheduled;
        }
        if (requestNotSent) {
            request.started.completeExceptionally(failure);
            request.finished.complete(Boolean.TRUE);
        }
        if (!request.result.completeExceptionally(failure) && !request.result.isCompletedExceptionally()
                && !cleanupScheduled) {
            removeAbandonedWebhook(request.webhookService);
        }
    }

    private @Nullable WebhookService currentProvider() {
        return providerManaged ? managedProvider : webhookServiceSupplier.get();
    }

    void invalidateCachedWebhook(long expectedGeneration) {
        synchronized (this) {
            // A stale bridge must not invalidate the replacement already acquired by another bridge.
            if (cachedWebhook != null && cachedWebhookGeneration == expectedGeneration) {
                cachedWebhook = null;
            }
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

    private static final class RequestOperation {
        private final WebhookService webhookService;
        private final CompletableFuture<Boolean> started = new CompletableFuture<>();
        private final CompletableFuture<Webhook> result = new CompletableFuture<>();
        private final CompletableFuture<Boolean> finished = new CompletableFuture<>();
        private boolean requestSent;
        private boolean cleanupScheduled;

        private RequestOperation(WebhookService webhookService) {
            this.webhookService = webhookService;
        }
    }

    private record RemovalOperation(WebhookService webhookService, CompletableFuture<Boolean> previousRemoval,
            CompletableFuture<Boolean> result, boolean onlyWhenUnused) {
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
