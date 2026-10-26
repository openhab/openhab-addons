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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.net.dto.ServiceAlert;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TransitPollingHandler} contains the polling lifecycle shared by all Things of a Transit bridge.
 * <p>
 * The data is requested without holding the handler lock and published while holding the handler lock, the same lock
 * that {@link #bridgeStatusChanged} and {@link #updateStatus} use, so no lock-order inversion is possible. Results
 * that arrive after polling was stopped or the bridge went offline are discarded.
 *
 * @param <T> type of the API result
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public abstract class TransitPollingHandler<T> extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean pollInProgress = new AtomicBoolean();
    private @Nullable ScheduledFuture<?> refreshJob;

    protected TransitPollingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Reads and validates the Thing configuration.
     *
     * @return {@code null} if the configuration is valid, otherwise the status description (e.g. an {@code @text/}
     *         key) for the {@link ThingStatusDetail#CONFIGURATION_ERROR}
     */
    protected abstract @Nullable String loadConfiguration();

    /**
     * @return the configured refresh interval in seconds
     */
    protected abstract long getRefreshInterval();

    /**
     * Requests the data from the API. Called without holding the handler lock.
     */
    protected abstract T fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException;

    /**
     * Publishes the result. Called while holding the handler lock and only if polling is still active.
     */
    protected abstract void publish(T result, long nowSeconds);

    /**
     * Called while holding the handler lock after a communication error, so subclasses can reset their channels.
     */
    protected void onCommunicationError() {
    }

    @Override
    public void initialize() {
        String configError = loadConfiguration();
        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (this) {
            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Bridge is ONLINE, starting polling for {}", getThing().getUID());
                updateStatus(ThingStatus.UNKNOWN);
                startPolling();
            } else {
                logger.debug("Bridge is {}, stopping polling for {}", bridgeStatusInfo.getStatus(),
                        getThing().getUID());
                stopPolling();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    private void startPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job == null || job.isCancelled()) {
                long refreshInterval = Math.max(MIN_REFRESH_INTERVAL_SECONDS, getRefreshInterval());
                refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void stopPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job != null && !job.isCancelled()) {
                job.cancel(true);
                refreshJob = null;
            }
        }
    }

    /**
     * Must be called while holding the handler lock. Returns {@code false} if polling was stopped or the bridge went
     * offline while a request was in flight, so that outdated results are not published.
     */
    private boolean isPollingActive() {
        Bridge bridge = getBridge();
        return refreshJob != null && bridge != null && bridge.getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private void pollTransitApi() {
        // skip if the previous poll (scheduled or triggered by REFRESH) is still running
        if (!pollInProgress.compareAndSet(false, true)) {
            logger.debug("Previous poll still running, skipping this one for {}", getThing().getUID());
            return;
        }
        try {
            poll();
        } finally {
            pollInProgress.set(false);
        }
    }

    private void poll() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not ONLINE. Skipping poll for {}", getThing().getUID());
            return;
        }

        try {
            TransitBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }
            T result = fetch(bridgeHandler);
            synchronized (this) {
                if (!isPollingActive()) {
                    logger.debug("Polling stopped while request was in flight, discarding result for {}",
                            getThing().getUID());
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                publish(result, System.currentTimeMillis() / 1000);
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Polling task interrupted for {}", getThing().getUID());
        } catch (IOException e) {
            synchronized (this) {
                if (!isPollingActive()) {
                    return;
                }
                String errorMessage = e.getMessage();
                logger.debug("Communication error while polling {}: {}", getThing().getUID(), errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                onCommunicationError();
            }
        }
    }

    public @Nullable TransitBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    /**
     * Trims the entries of a comma-separated list and removes empty entries.
     */
    protected static String normalizeList(String list) {
        return Arrays.stream(list.split(",")).map(String::trim).filter(entry -> !entry.isEmpty())
                .collect(Collectors.joining(","));
    }

    // helpers for publishing states

    protected void updateGroupState(String group, String channel, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    protected static State toStringState(@Nullable String value) {
        return value == null || value.isEmpty() ? UnDefType.UNDEF : new StringType(value);
    }

    protected static State toOnOffState(@Nullable Boolean value) {
        return value == null ? UnDefType.UNDEF : OnOffType.from(value);
    }

    protected static State toDateTimeState(@Nullable Instant value) {
        return value == null ? UnDefType.UNDEF
                : new DateTimeType(ZonedDateTime.ofInstant(value, ZoneId.systemDefault()));
    }

    /**
     * Publishes the number of active alerts and the details of the most severe active alert to the given group.
     */
    protected void publishAlerts(String group, @Nullable List<ServiceAlert> alerts, long nowSeconds) {
        List<ServiceAlert> activeAlerts = ServiceAlert.getActiveAlerts(alerts, nowSeconds);
        updateGroupState(group, CHANNEL_ACTIVE_ALERTS_COUNT, new DecimalType(activeAlerts.size()));

        ServiceAlert alert = ServiceAlert.getMostSevere(activeAlerts);
        updateGroupState(group, CHANNEL_ALERT_TITLE, toStringState(alert != null ? alert.getTitle() : null));
        updateGroupState(group, CHANNEL_ALERT_DESCRIPTION,
                toStringState(alert != null ? alert.getDescription() : null));
        updateGroupState(group, CHANNEL_ALERT_SEVERITY, toStringState(alert != null ? alert.getSeverity() : null));
        updateGroupState(group, CHANNEL_ALERT_EFFECT, toStringState(alert != null ? alert.getEffect() : null));
        updateGroupState(group, CHANNEL_ALERT_CAUSE, toStringState(alert != null ? alert.getCause() : null));
    }

    /**
     * Converts the wheelchair accessibility of the API ({@code 0} = unknown, {@code 1} = accessible, {@code 2} = not
     * accessible) to a switch state.
     */
    protected static State toWheelchairState(@Nullable Integer value) {
        if (value != null && value == 1) {
            return OnOffType.ON;
        } else if (value != null && value == 2) {
            return OnOffType.OFF;
        }
        return UnDefType.UNDEF;
    }
}
