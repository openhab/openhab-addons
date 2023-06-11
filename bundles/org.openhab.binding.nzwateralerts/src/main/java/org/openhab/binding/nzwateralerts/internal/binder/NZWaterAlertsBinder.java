/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.nzwateralerts.internal.NZWaterAlertsConfiguration;
import org.openhab.binding.nzwateralerts.internal.api.WaterAlertWebClient;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
    private final ScheduledExecutorService scheduler;

    private int refreshInterval = 5;

    public NZWaterAlertsBinder(final HttpClient httpClient, @Nullable final NZWaterAlertsConfiguration config,
            final ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;

        if (config != null) {
            final String localLocation = config.location;
            if (localLocation == null) {
                for (final NZWaterAlertsBinderListener listener : listeners) {
                    listener.updateBindingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Location is not set.");
                }
            } else {
                this.webClient = new WaterAlertWebClient(httpClient, localLocation);
                refreshInterval = config.refreshInterval;
            }
        } else {
            for (final NZWaterAlertsBinderListener listener : listeners) {
                listener.updateBindingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not create webClient, a parameter is null");
            }
            logger.debug("Create Binder failed due to null config item");
        }
    }

    public void update() {
        final WaterAlertWebClient localWebClient = webClient;
        if (localWebClient != null) {
            final Integer waterLevel = localWebClient.getLevel();

            for (final NZWaterAlertsBinderListener listener : listeners) {
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
     *            registered.
     */
    public void registerListener(final NZWaterAlertsBinderListener alertsBinderInterface) {
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
     *            unregistered.
     */
    public void unregisterListener(final NZWaterAlertsBinderListener alertsBinderInterface) {
        final boolean isRemoved = listeners.remove(alertsBinderInterface);
        if (isRemoved) {
            updatePollingState();
        }
    }

    private void updatePollingState() {
        final ScheduledFuture<?> localFuture = future;

        if (localFuture != null && listeners.isEmpty()) {
            localFuture.cancel(true);
            future = null;
            return;
        }

        if (localFuture == null && !listeners.isEmpty()) {
            future = scheduler.scheduleWithFixedDelay(this::update, 0, refreshInterval, TimeUnit.HOURS);
        }
    }
}
