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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * Base class for Rachio child thing handlers with common bridge lookup, cached refresh, and status propagation logic.
 *
 * @author Jeff James - Initial contribution
 * @author Kovacs Istvan - Adaptation and integration into the openHAB 5.1+ Rachio binding
 */
@NonNullByDefault
public abstract class AbstractRachioThingHandler extends BaseThingHandler implements RachioStatusListener {
    private static final long[] LOCAL_THROTTLE_RETRY_DELAYS_SECONDS = { 15, 30, 60, 120 };
    private static final long MAX_INITIALIZATION_THROTTLE_RETRY_DELAY_SECONDS = 30;
    private static final String INITIALIZATION_THROTTLE_STATUS_MESSAGE = "Waiting for local Rachio API bootstrap budget; initialization will retry automatically.";

    protected String thingId = "";
    protected final Map<String, State> channelData = new HashMap<>();

    protected @Nullable Bridge bridge;

    protected @Nullable RachioBridgeHandler cloudHandler;

    private @Nullable ScheduledFuture<?> localThrottleRetryJob;
    private int localThrottleRetryAttempt = 0;
    private boolean localThrottleInitializationDeferred = false;

    protected AbstractRachioThingHandler(Thing thing) {
        super(thing);
    }

    protected boolean initializeCloudHandler() {
        bridge = getBridge();
        Bridge currentBridge = bridge;
        if (currentBridge == null) {
            return false;
        }

        ThingHandler handler = currentBridge.getHandler();
        if (handler instanceof RachioBridgeHandler bridgeHandler) {
            cloudHandler = bridgeHandler;
            return true;
        }
        return false;
    }

    protected String getThingConfigurationString(String parameterName) {
        Object value = getThing().getConfiguration().getProperties().get(parameterName);
        return value != null ? value.toString().trim() : "";
    }

    protected String getThingConfigurationOrPropertyString(String parameterName) {
        String configValue = getThingConfigurationString(parameterName);
        if (!configValue.isBlank()) {
            return configValue;
        }
        String propertyValue = getThing().getProperties().get(parameterName);
        return propertyValue != null ? propertyValue.trim() : "";
    }

    protected boolean isBridgeOnline() {
        Bridge currentBridge = bridge;
        return currentBridge != null && currentBridge.getStatus() == ThingStatus.ONLINE;
    }

    protected boolean handleRefreshCommand(String channel) {
        State state = channelData.get(channel);
        if (state != null) {
            updateState(channel, state);
            return true;
        }
        postChannelData();
        return false;
    }

    protected boolean updateChannel(String channelName, State newValue) {
        State currentValue = channelData.get(channelName);
        if ((currentValue != null) && currentValue.equals(newValue)) {
            return false;
        }

        if (currentValue == null) {
            channelData.put(channelName, newValue);
        } else {
            channelData.replace(channelName, newValue);
        }

        updateState(channelName, newValue);
        return true;
    }

    protected long scheduleLocalThrottleRetry(String operation, Runnable retryAction) {
        synchronized (this) {
            ScheduledFuture<?> retryJob = localThrottleRetryJob;
            if (retryJob != null && !retryJob.isDone()) {
                return -1;
            }

            long delaySeconds = nextLocalThrottleRetryDelaySeconds();
            localThrottleRetryJob = scheduler.schedule(() -> {
                synchronized (this) {
                    localThrottleRetryJob = null;
                }
                retryAction.run();
            }, delaySeconds, TimeUnit.SECONDS);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Local Rachio API throttle hit while " + operation + "; retry scheduled in " + delaySeconds
                            + " seconds.");
            return delaySeconds;
        }
    }

    protected long scheduleInitializationThrottleRetry(String operation, Runnable retryAction,
            RachioApiThrottledException throttle) {
        synchronized (this) {
            ScheduledFuture<?> retryJob = localThrottleRetryJob;
            if (retryJob != null && !retryJob.isDone()) {
                return -1;
            }

            long suggestedDelaySeconds = throttle.getSuggestedRetryDelay().getSeconds();
            long delaySeconds = Math.max(1,
                    Math.min(MAX_INITIALIZATION_THROTTLE_RETRY_DELAY_SECONDS, suggestedDelaySeconds));
            localThrottleInitializationDeferred = true;
            localThrottleRetryJob = scheduler.schedule(() -> {
                synchronized (this) {
                    localThrottleRetryJob = null;
                }
                retryAction.run();
            }, delaySeconds, TimeUnit.SECONDS);
            updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE, INITIALIZATION_THROTTLE_STATUS_MESSAGE);
            return delaySeconds;
        }
    }

    protected synchronized boolean resetLocalThrottleRetry() {
        boolean hadRetry = localThrottleRetryAttempt > 0 || localThrottleRetryJob != null
                || localThrottleInitializationDeferred;
        cancelLocalThrottleRetry();
        localThrottleRetryAttempt = 0;
        return hadRetry;
    }

    private long nextLocalThrottleRetryDelaySeconds() {
        int index = Math.min(localThrottleRetryAttempt, LOCAL_THROTTLE_RETRY_DELAYS_SECONDS.length - 1);
        localThrottleRetryAttempt++;
        return LOCAL_THROTTLE_RETRY_DELAYS_SECONDS[index];
    }

    @Override
    public void onConfigurationUpdated() {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            onBridgeOnline();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    protected void onBridgeOnline() {
        goOnline();
    }

    public void shutdown() {
        cancelLocalThrottleRetry();
        unregisterStatusListener();
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        cancelLocalThrottleRetry();
        unregisterStatusListener();
        super.dispose();
    }

    private synchronized void cancelLocalThrottleRetry() {
        ScheduledFuture<?> retryJob = localThrottleRetryJob;
        if (retryJob != null && !retryJob.isCancelled()) {
            retryJob.cancel(true);
        }
        localThrottleRetryJob = null;
        localThrottleInitializationDeferred = false;
    }

    private void unregisterStatusListener() {
        RachioBridgeHandler handler = cloudHandler;
        if (handler != null) {
            handler.unregisterStatusListener(this);
        }
    }

    protected abstract void goOnline();

    protected abstract void postChannelData();
}
