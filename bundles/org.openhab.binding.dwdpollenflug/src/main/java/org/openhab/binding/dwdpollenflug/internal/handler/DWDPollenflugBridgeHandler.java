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
package org.openhab.binding.dwdpollenflug.internal.handler;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dwdpollenflug.internal.DWDPollingException;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugBridgeConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugRegionHandler}
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugBridgeHandler.class);

    private @NonNullByDefault({}) DWDPollenflugBridgeConfiguration bridgeConfig = null;

    private final DWDPollenflugPolling pollingJobRunnable;
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable DWDPollenflug pollenflug;

    private final List<DWDPollenflugRegionListener> regionListeners = new CopyOnWriteArrayList<>();

    public DWDPollenflugBridgeHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        pollingJobRunnable = new DWDPollenflugPolling(this, client);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DWD Pollenflug bridge handler");
        bridgeConfig = getConfigAs(DWDPollenflugBridgeConfiguration.class);

        if (bridgeConfig.isValid()) {
            onUpdate();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private synchronized void onUpdate() {
        if (regionListeners.isEmpty()) {
            stopPolling();
            updateStatus(ThingStatus.ONLINE);
        } else {
            startPolling();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    public void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("Start polling.");
            pollingJob = scheduler.scheduleWithFixedDelay(pollingJobRunnable, INITIAL_DELAY,
                    bridgeConfig.getRefresh() * SECONDS_PER_MINUTE, TimeUnit.SECONDS);
        } else if (pollenflug != null) {
            notifyOnUpdate(pollenflug);
        }
    }

    public void stopPolling() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("Stop polling.");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    public void onBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void onBridgeCommunicationError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    public void onBridgeCommunicationError(DWDPollingException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
    }

    public boolean registerRegionListener(DWDPollenflugRegionListener regionListener) {
        logger.debug("Register region listener");
        boolean result = regionListeners.add(regionListener);
        if (result) {
            startPolling();
        }
        return result;
    }

    public boolean unregisterRegionListener(DWDPollenflugRegionListener regionListener) {
        logger.debug("Unregister region listener");
        boolean result = regionListeners.remove(regionListener);
        if (result && regionListeners.isEmpty()) {
            stopPolling();
        }
        return result;
    }

    public synchronized void notifyOnUpdate(@Nullable DWDPollenflug newState) {
        pollenflug = newState;
        if (newState != null) {
            for (DWDPollenflugRegionListener regionListener : regionListeners) {
                regionListener.notifyOnUpdate(newState);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable DWDPollenflug getPollenflug() {
        return pollenflug;
    }
}
