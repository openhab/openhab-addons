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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

/**
 * Base class for Rachio bridge-like handlers that own child status listeners and scheduled refresh work.
 *
 * @author Jeff James - Initial architectural concept
 * @author Kovacs Istvan - Adaptation and integration into the openHAB 5.1+ Rachio binding
 */
@NonNullByDefault
public abstract class AbstractRachioBridgeHandler extends ConfigStatusBridgeHandler {
    protected final List<RachioStatusListener> rachioStatusListeners = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> pollingJob;
    private boolean refreshPending = false;

    protected AbstractRachioBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Start or stop a background polling job based on whether any child handlers need refresh notifications.
     */
    protected synchronized void updateListenerManagement() {
        ScheduledFuture<?> job = pollingJob;
        if (!rachioStatusListeners.isEmpty() && (job == null || job.isCancelled())) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::runScheduledRefresh, getPollingIntervalSeconds(),
                    getPollingIntervalSeconds(), TimeUnit.SECONDS);
        } else if (rachioStatusListeners.isEmpty() && job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Register the given listener to receive Rachio device and zone status updates.
     *
     * @param listener the listener to register
     */
    public void registerStatusListener(final RachioStatusListener listener) {
        rachioStatusListeners.add(listener);
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
        rachioStatusListeners.stream().forEach(l -> l.onThingStateChanged(device, zone));
    }

    protected abstract int getPollingIntervalSeconds();

    protected abstract void runScheduledRefresh();

    public void shutdown() {
        cancelPollingJob();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Override
    public synchronized void dispose() {
        cancelPollingJob();
        super.dispose();
    }

    private void cancelPollingJob() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
        }
    }
}
