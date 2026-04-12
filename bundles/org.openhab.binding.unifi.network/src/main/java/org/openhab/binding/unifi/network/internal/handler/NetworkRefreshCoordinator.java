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
package org.openhab.binding.unifi.network.internal.handler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.network.internal.api.UniFiController;
import org.openhab.binding.unifi.network.internal.api.UniFiException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-bridge coordinator for the UniFi Network child binding. Replaces the now-removed
 * {@code UniFiControllerThingHandler}: owns a single {@link UniFiController} instance per UniFi console bridge,
 * runs the periodic refresh loop, and notifies subscribing {@link UniFiBaseThingHandler}s when fresh cache data
 * is available.
 * <p>
 * The coordinator is keyed by the parent controller bridge's {@link ThingUID} so multiple UniFi consoles each
 * get their own refresh loop. All consoles share the same {@link UniFiSession} plumbing, rate limiter, and HTTP
 * client published by the shared parent binding, so adding Network things does not trigger additional logins.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NetworkRefreshCoordinator {

    private static final Map<ThingUID, NetworkRefreshCoordinator> INSTANCES = new ConcurrentHashMap<>();

    private static final int DEFAULT_REFRESH_SECONDS = 10;
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(NetworkRefreshCoordinator.class);

    private final UniFiControllerBridgeHandler bridgeHandler;
    private final Set<UniFiBaseThingHandler<?, ?>> subscribers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Object refreshLock = new Object();
    private final int refreshSeconds;
    private final int timeoutSeconds;
    private final boolean unifios;

    private volatile @Nullable UniFiController controller;
    private volatile @Nullable Throwable lastError;
    private @Nullable ScheduledFuture<?> refreshJob;

    private NetworkRefreshCoordinator(UniFiControllerBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        Thing bridgeThing = bridgeHandler.getThing();
        Object unifiosObj = bridgeThing.getConfiguration().get("unifios");
        this.unifios = unifiosObj instanceof Boolean b ? b : true;
        Object timeoutObj = bridgeThing.getConfiguration().get("timeoutSeconds");
        this.timeoutSeconds = timeoutObj instanceof Number n ? n.intValue() : DEFAULT_TIMEOUT_SECONDS;
        Object refreshObj = bridgeThing.getConfiguration().get("refresh");
        this.refreshSeconds = refreshObj instanceof Number n ? n.intValue() : DEFAULT_REFRESH_SECONDS;
    }

    /**
     * Attach the given subscriber to the coordinator for its bridge, creating one if this is the first subscriber
     * for that bridge.
     */
    public static NetworkRefreshCoordinator attach(UniFiControllerBridgeHandler bridgeHandler,
            UniFiBaseThingHandler<?, ?> subscriber) {
        NetworkRefreshCoordinator coordinator = INSTANCES.computeIfAbsent(bridgeHandler.getThing().getUID(),
                uid -> new NetworkRefreshCoordinator(bridgeHandler));
        coordinator.subscribers.add(subscriber);
        coordinator.ensureStarted();
        return coordinator;
    }

    /**
     * Detach the given subscriber. When no subscribers remain, the coordinator cancels its refresh job and
     * removes itself from the registry.
     */
    public static void detach(ThingUID bridgeUid, UniFiBaseThingHandler<?, ?> subscriber) {
        NetworkRefreshCoordinator coordinator = INSTANCES.get(bridgeUid);
        if (coordinator == null) {
            return;
        }
        coordinator.subscribers.remove(subscriber);
        if (coordinator.subscribers.isEmpty()) {
            coordinator.dispose();
            INSTANCES.remove(bridgeUid, coordinator);
        }
    }

    public @Nullable UniFiController getController() {
        return controller;
    }

    public @Nullable Throwable getLastError() {
        return lastError;
    }

    private void ensureStarted() {
        synchronized (refreshLock) {
            if (refreshJob == null) {
                refreshJob = bridgeHandler.getScheduler().scheduleWithFixedDelay(this::runRefresh, 0, refreshSeconds,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void runRefresh() {
        UniFiSession session;
        try {
            session = bridgeHandler.getSessionAsync().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (TimeoutException e) {
            logger.trace("Timed out waiting for bridge session; skipping refresh");
            return;
        } catch (Exception e) {
            logger.debug("Bridge session not available, skipping refresh: {}", e.getMessage());
            lastError = e;
            notifySubscribers();
            return;
        }

        UniFiController ctrl;
        synchronized (refreshLock) {
            UniFiController existing = controller;
            if (existing == null) {
                existing = new UniFiController(bridgeHandler.getHttpClient(), session, unifios, timeoutSeconds);
                controller = existing;
            }
            ctrl = existing;
        }

        try {
            logger.trace("Refreshing UniFi Network cache for bridge {}", bridgeHandler.getThing().getUID());
            ctrl.refresh();
            lastError = null;
            notifySubscribers();
        } catch (UniFiException | RuntimeException e) {
            logger.debug("Unhandled error during Network refresh for bridge {}", bridgeHandler.getThing().getUID(), e);
            lastError = e;
            notifySubscribers();
        }
    }

    private void notifySubscribers() {
        for (UniFiBaseThingHandler<?, ?> subscriber : subscribers) {
            try {
                subscriber.onCoordinatorRefresh();
            } catch (RuntimeException e) {
                logger.debug("Subscriber {} threw during refresh notification", subscriber.getThing().getUID(), e);
            }
        }
    }

    private void dispose() {
        synchronized (refreshLock) {
            ScheduledFuture<?> job = refreshJob;
            if (job != null) {
                job.cancel(true);
                refreshJob = null;
            }
            controller = null;
        }
    }
}
