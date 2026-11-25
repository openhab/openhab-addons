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
package org.openhab.binding.homie.internal.homie300;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes subscription setup and invalidates pending setup before cleanup, without
 * holding a lock while calling MQTT or handler code. Metadata reception remains asynchronous.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
final class SubscriptionContext {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionContext.class);
    private CompletableFuture<@Nullable Void> tail;
    private @Nullable CompletableFuture<@Nullable Void> stopFuture;

    SubscriptionContext() {
        this(CompletableFuture.completedFuture(null));
    }

    SubscriptionContext(CompletableFuture<@Nullable Void> predecessor) {
        tail = predecessor.handle((v, error) -> {
            if (error != null) {
                logger.debug("Previous subscription cleanup failed; continuing with a new subscription", error);
            }
            return null;
        });
    }

    CompletableFuture<@Nullable Void> run(Supplier<CompletableFuture<@Nullable Void>> action) {
        CompletableFuture<@Nullable Void> previous;
        CompletableFuture<@Nullable Void> started = new CompletableFuture<>();
        synchronized (this) {
            if (stopFuture != null) {
                return CompletableFuture.failedFuture(new CancellationException("Subscription stopped"));
            }
            previous = tail;
            tail = started;
        }
        CompletableFuture<@Nullable Void> result = new CompletableFuture<>();
        previous.whenComplete((v, e) -> {
            try {
                if (e != null) {
                    result.completeExceptionally(e);
                    return;
                }
                if (!isActive()) {
                    result.completeExceptionally(new CancellationException("Subscription stopped"));
                    return;
                }
                action.get().whenComplete((r, error) -> {
                    if (error == null) {
                        result.complete(r);
                    } else {
                        result.completeExceptionally(error);
                    }
                });
            } catch (RuntimeException error) {
                result.completeExceptionally(error);
            } finally {
                started.complete(null);
            }
        });
        return result;
    }

    synchronized boolean isActive() {
        return stopFuture == null;
    }

    CompletableFuture<@Nullable Void> stop(Supplier<CompletableFuture<@Nullable Void>> cleanup) {
        CompletableFuture<@Nullable Void> previous;
        CompletableFuture<@Nullable Void> stopped = new CompletableFuture<>();
        synchronized (this) {
            CompletableFuture<@Nullable Void> stopFuture = this.stopFuture;
            if (stopFuture != null) {
                return stopFuture;
            }
            this.stopFuture = stopped;
            previous = tail;
        }
        // Setup already in progress may finish starting a subscription. Cleanup must follow it.
        previous.handle((v, e) -> null).thenCompose(v -> cleanup.get()).whenComplete((v, error) -> {
            if (error == null) {
                stopped.complete(null);
            } else {
                stopped.completeExceptionally(error);
            }
        });
        return stopped;
    }
}
