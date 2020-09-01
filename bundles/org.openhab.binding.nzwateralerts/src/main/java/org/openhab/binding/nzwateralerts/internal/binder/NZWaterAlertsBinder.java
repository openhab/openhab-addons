/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nzwateralerts.internal.binder;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nzwateralerts.internal.NZWaterAlertsConfiguration;
import org.openhab.binding.nzwateralerts.internal.api.WaterAlertWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NZWaterAlertsController} is responsible for handling the connection
 * between the handler and API.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class NZWaterAlertsBinder {
    private @Nullable WaterAlertWebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(NZWaterAlertsBinder.class);

    private final Set<NZWaterAlertsBinderListener> listeners = new CopyOnWriteArraySet<>();
    private @Nullable ScheduledFuture<?> future;
    private @Nullable ScheduledExecutorService scheduler;

    private int refreshInterval = 5;

    public NZWaterAlertsBinder(@Nullable HttpClient httpClient, @Nullable NZWaterAlertsConfiguration config, @Nullable
            ScheduledExecutorService scheduler) {

        if (httpClient != null && config != null && scheduler != null) {
            if (config.location == null) {
                for (NZWaterAlertsBinderListener listener : listeners) {
                    listener.updateBindingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Location is not set.");
                }
            } else {
                this.webClient = new WaterAlertWebClient(httpClient, config.location);
                this.scheduler = scheduler;
                refreshInterval = config.refreshInterval;
            }
        } else {
            for (NZWaterAlertsBinderListener listener : listeners) {
                listener.updateBindingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could not create webClient, a parameter is null");
            }
            logger.debug("Create Binder failed due to null item; httpClient {} config {} scheduler {}", httpClient != null, config != null, scheduler != null);
        }
    }

    public void update() {
        if (webClient != null) {
            Integer waterLevel = webClient.getLevel();

            for (NZWaterAlertsBinderListener listener : listeners) {
                if (waterLevel == null) {
                    listener.updateBindingStatus(ThingStatus.OFFLINE);
                } else {
                    listener.updateBindingStatus(ThingStatus.ONLINE);
                    listener.updateWaterLevel(waterLevel);
                }
            }
        }
    }

    /**
     * Registers the given {@link NZWaterAlertsBinderListener}. If it is already
     * registered, this method returns immediately.
     *
     * @param alertsBinderInterface The {@link NZWaterAlertsBinderListener} to be
     *                              registered.
     */
    public void registerListener(NZWaterAlertsBinderListener alertsBinderInterface) {
        final boolean isAdded = listeners.add(alertsBinderInterface);
        if (isAdded) {
            updatePollingState();
        }
    }

    /**
     * Unregisters the given {@link NZWaterAlertsBinderListener}. If it is already
     * unregistered, this method returns immediately.
     *
     * @param alertsBinderInterface The {@link NZWaterAlertsBinderListener} to be
     *                              unregistered.
     */
    public void unregisterListener(NZWaterAlertsBinderListener alertsBinderInterface) {
        final boolean isRemoved = listeners.remove(alertsBinderInterface);
        if (isRemoved) {
            updatePollingState();
        }
    }

    private void updatePollingState() {
        boolean isPolling = future != null;
        if (isPolling && listeners.isEmpty()) {
            if (future != null)
            future.cancel(true);
            future = null;
            return;
        }

        if (!isPolling && !listeners.isEmpty()) {
            future = scheduler.scheduleWithFixedDelay(() -> {
                update();
            }, 0, refreshInterval, TimeUnit.HOURS);
        }
    }
}
