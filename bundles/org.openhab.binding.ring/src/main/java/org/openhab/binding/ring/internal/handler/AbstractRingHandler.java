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
package org.openhab.binding.ring.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.data.Tokens;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractRingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public abstract class AbstractRingHandler extends BaseThingHandler {

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AbstractRingHandler.class);

    // Scheduler
    protected @Nullable ScheduledFuture<?> refreshJob;
    private boolean fcmSubscribed = false; // NEW: Tracks successful FCM subscription

    protected AbstractRingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AbstractRingHandler for {}", getThing().getUID());

        // Attempt subscription immediately in case the bridge is ALREADY online
        checkAndSubscribeFCM();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        // Attempt subscription if the bridge JUST came online
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("Account Bridge went ONLINE. Triggering FCM check.");
            checkAndSubscribeFCM();
        }
    }

    private void checkAndSubscribeFCM() {
        logger.debug("FCM Check: Evaluating push subscription for {}", getThing().getUID());

        if (fcmSubscribed) {
            return;
        }

        if (getBridge() == null || getBridge().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        AccountHandler accountBridge = (AccountHandler) getBridge().getHandler();
        String currentDeviceId = null;

        // 1. Target the Configuration map for config.id
        Object configId = getThing().getConfiguration().get("id");
        if (configId != null) {
            currentDeviceId = String.valueOf(configId);
        }

        // 2. Fallbacks just in case older cameras use properties
        if (currentDeviceId == null) {
            currentDeviceId = getThing().getProperties().get("doorbotId");
        }
        if (currentDeviceId == null) {
            currentDeviceId = getThing().getProperties().get("id");
        }

        // Validate that we have a purely numeric ID before sending it to the Ring API
        if (currentDeviceId == null || !currentDeviceId.matches("\\d+")) {
            logger.warn("FCM Subscription aborted: Extracted ID '{}' is missing or not numeric!", currentDeviceId);
            return;
        }

        if (accountBridge != null) {
            Tokens currentTokens = accountBridge.getTokens();
            if (currentTokens != null) {
                try {
                    // WE DO NOT NEED THIS! The AccountHandler already registered the socket.
                    // accountBridge.getRestClient().subscribeDeviceToPush(currentDeviceId, currentTokens);

                    fcmSubscribed = true;
                    logger.debug("Bypassed per-device subscription for {}. Relying on Account-level FCM socket.",
                            currentDeviceId);
                } catch (Exception e) {
                    logger.warn("Failed to subscribe device {} to FCM push events.", currentDeviceId, e);
                }
            }
        }
    }

    /**
     * Refresh the state of channels that may have changed by (re-)initialization.
     */
    protected abstract void refreshState();

    /**
     * Called every minute
     */
    protected abstract void minuteTick();

    private void refresh() {
        try {
            // Self-healing FCM check: Ensures the device subscribes even if initialize() is overridden
            if (!fcmSubscribed) {
                checkAndSubscribeFCM();
            }

            minuteTick();
        } catch (final Exception e) {
            logger.debug("AbstractHandler - Exception occurred during execution of startAutomaticRefresh(): {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Check every 60 seconds if one of the alarm times is reached.
     */
    protected void startAutomaticRefresh(final int refreshInterval) {
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshInterval, TimeUnit.SECONDS);
        refreshState();
    }

    protected void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Dispose off the refreshJob nicely.
     */
    @Override
    public void dispose() {
        stopAutomaticRefresh();
    }
}
