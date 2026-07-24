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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Rachio bridge-like handlers that own child status listeners and scheduled refresh work.
 *
 * @author Jeff James - Initial contribution
 * @author Kovacs Istvan - Adaptation and integration into the openHAB 5.1+ Rachio binding
 */
@NonNullByDefault
public abstract class AbstractRachioBridgeHandler extends ConfigStatusBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractRachioBridgeHandler.class);
    protected final Set<RachioStatusListener> rachioStatusListeners = new CopyOnWriteArraySet<>();

    private @Nullable ScheduledFuture<?> pollingJob;
    private boolean refreshPending = false;
    private boolean pollingPreviouslyStarted = false;

    protected AbstractRachioBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Start or stop a background polling job based on whether any child handlers need refresh notifications.
     */
    protected synchronized void updateListenerManagement() {
        ScheduledFuture<?> job = pollingJob;
        int listenerCount = rachioStatusListeners.size();
        int pollingInterval = getPollingIntervalSeconds();
        if (listenerCount > 0 && (job == null || job.isDone())) {
            logger.debug("RachioCloud: {} scheduled polling (listeners={}, pollingInterval={}s)",
                    pollingPreviouslyStarted ? "Restarting" : "Starting", listenerCount, pollingInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::runScheduledRefresh, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
            pollingPreviouslyStarted = true;
        } else if (listenerCount == 0) {
            cancelPollingJob("no status listeners");
        }
    }

    /**
     * Register the given listener to receive Rachio device and zone status updates.
     *
     * @param listener the listener to register
     */
    public void registerStatusListener(final RachioStatusListener listener) {
        boolean added = rachioStatusListeners.add(listener);
        logger.debug("RachioCloud: Status listener registration {} (listeners={})", added ? "added" : "already present",
                rachioStatusListeners.size());
        updateListenerManagement();
    }

    /**
     * Unregister the given listener from further Rachio device and zone status updates.
     *
     * @param listener the listener to unregister
     * @return <code>true</code> if listener was previously registered and is now unregistered; <code>false</code>
     *         otherwise
     */
    public boolean unregisterStatusListener(final RachioStatusListener listener) {
        boolean result = rachioStatusListeners.remove(listener);
        logger.debug("RachioCloud: Status listener unregistration {} (listeners={})", result ? "removed" : "not found",
                rachioStatusListeners.size());
        if (result) {
            updateListenerManagement();
        }

        return result;
    }

    protected boolean beginRefresh() {
        synchronized (this) {
            if (refreshPending) {
                return false;
            }
            refreshPending = true;
            return true;
        }
    }

    protected synchronized void endRefresh() {
        refreshPending = false;
    }

    protected void notifyThingStateChanged(@Nullable RachioDevice device, @Nullable RachioZone zone) {
        for (RachioStatusListener listener : rachioStatusListeners) {
            try {
                listener.onThingStateChanged(device, zone);
            } catch (RuntimeException e) {
                logger.debug("RachioCloud: Status listener update failed (listener={})",
                        listener.getClass().getSimpleName(), e);
            }
        }
    }

    protected abstract int getPollingIntervalSeconds();

    protected abstract void runScheduledRefresh();

    public void shutdown() {
        cancelPollingJob("bridge shutdown");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Override
    public synchronized void dispose() {
        cancelPollingJob("bridge disposal");
        super.dispose();
    }

    synchronized int getStatusListenerCount() {
        return rachioStatusListeners.size();
    }

    synchronized boolean isPollingJobActive() {
        ScheduledFuture<?> job = pollingJob;
        return job != null && !job.isDone();
    }

    private synchronized void cancelPollingJob(String reason) {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            logger.debug("RachioCloud: Cancelling scheduled polling ({}, listeners={}, pollingInterval={}s)", reason,
                    rachioStatusListeners.size(), getPollingIntervalSeconds());
            job.cancel(true);
            pollingJob = null;
        }
    }
}
