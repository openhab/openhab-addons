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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared owner for the single Rachio servlet path exposed through the openHAB Cloud webhook provider.
 */
@NonNullByDefault
public final class RachioCloudWebhookRegistry {
    private static final long CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(RachioCloudWebhookRegistry.class);
    private final Supplier<@Nullable Object> webhookServiceSupplier;
    private final Set<String> activeConsumers = ConcurrentHashMap.newKeySet();
    private @Nullable Object cachedWebhook;
    private @Nullable Object cachedWebhookProvider;
    private long cachedWebhookGeneration;

    public RachioCloudWebhookRegistry(Supplier<@Nullable Object> webhookServiceSupplier) {
        this.webhookServiceSupplier = webhookServiceSupplier;
    }

    synchronized CloudWebhookLease acquire(String consumerId) throws CloudWebhookException, InterruptedException {
        Object webhookService = webhookServiceSupplier.get();
        if (webhookService == null) {
            throw new CloudWebhookException("WebhookServiceUnavailable");
        }

        Object webhook = cachedWebhook;
        if (webhook != null && webhookService.equals(cachedWebhookProvider)) {
            CloudWebhookLease lease = webhookLease(webhook, cachedWebhookGeneration);
            activeConsumers.add(consumerId);
            return lease;
        }

        cachedWebhook = null;
        cachedWebhookProvider = webhookService;

        CompletableFuture<?> webhookFuture = requestWebhook(webhookService);
        boolean consumerAdded = false;
        try {
            webhook = requireNonNull(webhookFuture.get(CLOUD_WEBHOOK_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "requestWebhook");
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
            throw new CloudWebhookException(webhookFailureCause(e), e);
        }
    }

    synchronized void release(String consumerId) {
        if (!activeConsumers.remove(consumerId) || !activeConsumers.isEmpty()) {
            return;
        }

        cachedWebhook = null;
        Object webhookService = webhookServiceSupplier.get();
        cachedWebhookProvider = webhookService;
        if (webhookService == null) {
            return;
        }

        try {
            removeWebhook(webhookService)
                    .whenComplete((ignored, error) -> logger.debug("RachioCloud: openHAB Cloud webhook URL removal {}",
                            error == null ? "completed" : "failed: " + webhookFailureCause(error)));
        } catch (CloudWebhookException | RuntimeException e) {
            String diagnosticCause = e instanceof CloudWebhookException cloudWebhookException
                    ? cloudWebhookException.diagnosticCause()
                    : e.getClass().getSimpleName();
            logger.debug("RachioCloud: openHAB Cloud webhook URL removal failed: {}", diagnosticCause);
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

    private CompletableFuture<?> requestWebhook(Object webhookService) throws CloudWebhookException {
        return invokeCompletableFutureMethod(webhookService, "requestWebhook");
    }

    private CompletableFuture<?> removeWebhook(Object webhookService) throws CloudWebhookException {
        return invokeCompletableFutureMethod(webhookService, "removeWebhook");
    }

    private CompletableFuture<?> invokeCompletableFutureMethod(Object target, String methodName)
            throws CloudWebhookException {
        try {
            @Nullable
            Object result = invokeMethod(target, methodName, SERVLET_WEBHOOK_PATH);
            if (result instanceof CompletableFuture<?> future) {
                return future;
            }
            throw new CloudWebhookException("ClassCastException",
                    new ClassCastException(methodName + " did not return CompletableFuture"));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new CloudWebhookException(webhookFailureCause(e), e);
        } catch (InvocationTargetException e) {
            Throwable cause = invocationCause(e);
            throw new CloudWebhookException(webhookFailureCause(cause), cause);
        } catch (RuntimeException e) {
            throw new CloudWebhookException(webhookFailureCause(e), e);
        }
    }

    private CloudWebhookLease webhookLease(Object webhook, long generation) throws CloudWebhookException {
        try {
            Object url = requireNonNull(invokeMethod(webhook, "url"), "url");
            String urlString = url.toString();
            if (urlString.isBlank()) {
                throw new CloudWebhookException("IllegalArgumentException",
                        new IllegalArgumentException("url().toString() returned blank"));
            }

            @Nullable
            Object expiresAt = invokeMethod(webhook, "expiresAt");
            if (!(expiresAt instanceof Instant expiration)) {
                throw new CloudWebhookException("ClassCastException",
                        new ClassCastException("expiresAt() did not return Instant"));
            }
            return new CloudWebhookLease(urlString, expiration, generation);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new CloudWebhookException(webhookFailureCause(e), e);
        } catch (InvocationTargetException e) {
            Throwable cause = invocationCause(e);
            throw new CloudWebhookException(webhookFailureCause(cause), cause);
        } catch (RuntimeException e) {
            throw new CloudWebhookException(webhookFailureCause(e), e);
        }
    }

    private Object requireNonNull(@Nullable Object value, String methodName) throws CloudWebhookException {
        if (value == null) {
            throw new CloudWebhookException("NullPointerException", new NullPointerException(methodName));
        }
        return value;
    }

    private @Nullable Object invokeMethod(Object target, String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = target.getClass().getMethod(methodName, parameterTypes(args));
        return method.invoke(target, args);
    }

    private Class<?>[] parameterTypes(Object[] args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }

    private Throwable invocationCause(InvocationTargetException error) {
        Throwable cause = error.getCause();
        return cause != null ? cause : error;
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
