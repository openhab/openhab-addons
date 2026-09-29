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
package org.openhab.binding.unifi.internal.network.handler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.UniFiSession;
import org.openhab.binding.unifi.internal.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.internal.network.api.UniFiController;
import org.openhab.binding.unifi.internal.network.api.UniFiException;
import org.openhab.core.config.core.Configuration;
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
 * <p>
 * A coordinator is bound to one bridge handler instance and rebuilds its {@link UniFiController} whenever that
 * handler publishes a new session, because the bridge replaces its HTTP client and session each time it is
 * (re-)initialized.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mikhail Obodnikov - Follow bridge handler replacement and re-initialization
 */
@NonNullByDefault
public class NetworkRefreshCoordinator {

    private static final Map<ThingUID, NetworkRefreshCoordinator> INSTANCES = new ConcurrentHashMap<>();

    private static final int DEFAULT_REFRESH_SECONDS = 10;
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(NetworkRefreshCoordinator.class);

    private final UniFiControllerBridgeHandler bridgeHandler;
    private final ThingUID bridgeUID;
    private final Set<UniFiBaseThingHandler<?, ?>> subscribers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Object refreshLock = new Object();
    private final int refreshSeconds;

    private volatile @Nullable UniFiController controller;
    private @Nullable UniFiSession controllerSession;
    private volatile @Nullable Throwable lastError;
    private @Nullable ScheduledFuture<?> refreshJob;

    private NetworkRefreshCoordinator(UniFiControllerBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        Thing bridgeThing = bridgeHandler.getThing();
        this.bridgeUID = bridgeThing.getUID();
        Object refreshObj = bridgeThing.getConfiguration().get("refresh");
        this.refreshSeconds = refreshObj instanceof Number n ? n.intValue() : DEFAULT_REFRESH_SECONDS;
    }

    /**
     * Attach the given subscriber to the coordinator for its bridge, creating one if this is the first subscriber
     * for that bridge or if the existing coordinator is bound to a previous instance of the bridge handler.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static NetworkRefreshCoordinator attach(UniFiControllerBridgeHandler bridgeHandler,
            UniFiBaseThingHandler<?, ?> subscriber) {
        // compute() serializes attach and detach for the same bridge, so a subscriber cannot be added to a
        // coordinator that a concurrent detach is disposing.
        return Objects.requireNonNull(INSTANCES.compute(bridgeHandler.getThing().getUID(), (uid, existing) -> {
            NetworkRefreshCoordinator coordinator = existing;
            if (coordinator == null || coordinator.bridgeHandler != bridgeHandler) {
                if (coordinator != null) {
                    // The bridge handler was replaced (e.g. the bridge Thing was removed and added again). The old
                    // handler is disposed and will never provide a session again, so its refresh job must go.
                    coordinator.logger.debug(
                            "Replacing Network refresh coordinator bound to a previous handler of bridge {}", uid);
                    coordinator.dispose();
                }
                coordinator = new NetworkRefreshCoordinator(bridgeHandler);
            }
            coordinator.subscribers.add(subscriber);
            coordinator.ensureStarted();
            return coordinator;
        }));
    }

    /**
     * Detach the given subscriber. When no subscribers remain, the coordinator cancels its refresh job and
     * removes itself from the registry.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    void detach(UniFiBaseThingHandler<?, ?> subscriber) {
        INSTANCES.compute(bridgeUID, (uid, current) -> {
            subscribers.remove(subscriber);
            if (!subscribers.isEmpty()) {
                return current;
            }
            dispose();
            // The registry may already hold a coordinator for a newer bridge handler; keep that one.
            return current == this ? null : current;
        });
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
        UniFiSession session = bridgeHandler.getSession();
        if (session == null) {
            logger.trace("Bridge session not available yet; skipping refresh");
            return;
        }

        try {
            // Obtained inside the try: an exception escaping this task would cancel all later runs of the schedule.
            UniFiController ctrl = getOrCreateController(session);
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

    /**
     * Returns the controller for the given bridge session, building a new one when the session changed. The bridge
     * creates a new HTTP client and session each time it is (re-)initialized, e.g. after a configuration change;
     * a controller built on the previous ones would keep using a stopped HTTP client.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private UniFiController getOrCreateController(UniFiSession session) {
        synchronized (refreshLock) {
            UniFiController existing = controller;
            if (existing == null || controllerSession != session) {
                Configuration config = bridgeHandler.getThing().getConfiguration();
                Object unifiosObj = config.get("unifios");
                boolean unifios = unifiosObj instanceof Boolean b ? b : true;
                Object timeoutObj = config.get("timeoutSeconds");
                int timeoutSeconds = timeoutObj instanceof Number n ? n.intValue() : DEFAULT_TIMEOUT_SECONDS;
                existing = new UniFiController(bridgeHandler.getHttpClient(), session, unifios, timeoutSeconds);
                controller = existing;
                controllerSession = session;
            }
            return existing;
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
            controllerSession = null;
        }
    }
}
