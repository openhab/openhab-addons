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
package org.openhab.binding.groheondus.internal.handler;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_LAST_UPDATE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_PAUSE;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.openhab.binding.groheondus.internal.GroheOndusBindingConstants;
import org.openhab.binding.groheondus.internal.api.ApplianceInfo;
import org.openhab.binding.groheondus.internal.api.ApplianceRef;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.api.GroheOndusAuthenticationException;
import org.openhab.binding.groheondus.internal.api.GroheOndusNotFoundException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common polling, status and command handling for GROHE ONDUS appliances.
 *
 * @param <S> snapshot of everything fetched from the API in one poll
 *
 * @author Florian Schmidt - Initial contribution
 * @author Arne Seime - Publish historic measurements as time series, use own API client, improve error handling
 */
@NonNullByDefault
public abstract class GroheOndusBaseHandler<S> extends BaseThingHandler {
    private static final int MAX_PAUSE_MINUTES = 240;
    private static final DateTimeFormatter STATUS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Logger logger = LoggerFactory.getLogger(GroheOndusBaseHandler.class);

    protected final TimeZoneProvider timeZoneProvider;
    protected GroheOndusApplianceConfiguration config = new GroheOndusApplianceConfiguration();

    private final int applianceType;
    // Spaces the first poll of the things apart to avoid rate limiting by the service
    private final int initialDelaySeconds;
    private final ReentrantLock pollLock = new ReentrantLock();

    private @Nullable ScheduledFuture<?> poller;
    private int currentPollingInterval;

    private volatile @Nullable Instant snoozeUntil;
    private volatile boolean pauseStateKnown = false;

    // Whether the device itself has stopped reporting, although the cloud API still answers
    private volatile boolean deviceSilent;

    // Timestamp of the newest sample already sent as time series, to avoid resending the whole window on every poll
    private volatile @Nullable Instant lastPublishedSample;

    // Changed on every initialize and dispose, so that commands queued under an earlier lifecycle are discarded
    private volatile int commandGeneration;

    protected GroheOndusBaseHandler(Thing thing, int applianceType, int thingCounter,
            TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.applianceType = applianceType;
        this.initialDelaySeconds = thingCounter;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        synchronized (this) {
            commandGeneration++;
        }
        config = getConfigAs(GroheOndusApplianceConfiguration.class);
        if (config.applianceId.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.missing.applianceid");
            return;
        }
        lastPublishedSample = null;
        updateStatus(ThingStatus.UNKNOWN);
        int interval = config.pollingInterval > 0 ? config.pollingInterval
                : GroheOndusBindingConstants.DEFAULT_POLLING_INTERVAL;
        schedulePolling(initialDelaySeconds, interval);
    }

    @Override
    public void dispose() {
        synchronized (this) {
            commandGeneration++;
            ScheduledFuture<?> poller = this.poller;
            if (poller != null) {
                poller.cancel(true);
                this.poller = null;
            }
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // Restart polling so that the first poll happens soon, keeping the stagger between things
            synchronized (this) {
                if (poller != null) {
                    schedulePolling(initialDelaySeconds, currentPollingInterval);
                }
            }
        } else {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::poll);
        } else if (CHANNEL_PAUSE.equals(channelUID.getIdWithoutGroup())) {
            handlePauseCommand(channelUID, command);
        } else {
            handleApplianceCommand(channelUID, command);
        }
    }

    protected void handleApplianceCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command {} for channel {} is not supported", command, channelUID);
    }

    private synchronized void schedulePolling(int initialDelay, int interval) {
        ScheduledFuture<?> poller = this.poller;
        if (poller != null) {
            // Do not interrupt: rescheduling happens from within the running poll
            poller.cancel(false);
        }
        currentPollingInterval = interval;
        this.poller = scheduler.scheduleWithFixedDelay(this::poll, initialDelay, interval, TimeUnit.SECONDS);
        logger.debug("Scheduled polling every {}s for appliance {}", interval, thing.getUID());
    }

    private void adjustPollingInterval(ApplianceInfo appliance) {
        int interval = getPollingInterval(appliance);
        boolean changed;
        synchronized (this) {
            changed = interval != currentPollingInterval && poller != null;
        }
        if (changed) {
            schedulePolling(interval, interval);
        }
    }

    protected int getPollingInterval(ApplianceInfo appliance) {
        return config.pollingInterval > 0 ? config.pollingInterval
                : GroheOndusBindingConstants.DEFAULT_POLLING_INTERVAL;
    }

    protected ApplianceRef getApplianceRef() {
        return new ApplianceRef(config.locationId, config.roomId, config.applianceId);
    }

    void poll() {
        // Skip if a poll is already running, e.g. a REFRESH command arriving during a scheduled poll
        if (!pollLock.tryLock()) {
            return;
        }
        try {
            GroheOndusApi api = getApi();
            if (api == null) {
                // While the account is still logging in, stay UNKNOWN; bridgeStatusChanged restarts polling
                Bridge bridge = getBridge();
                if (bridge == null || bridge.getStatus() != ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
                }
                return;
            }
            ApplianceInfo appliance = api.getAppliance(getApplianceRef());
            if (appliance.type() != applianceType) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.wrongtype");
                return;
            }
            adjustPollingInterval(appliance);
            updateApplianceProperties(appliance.properties());
            S snapshot = fetch(api, appliance);
            Instant newestSample = getNewestSampleTime(snapshot);
            Instant lastTransmission = appliance.lastTransmission();
            Instant lastReport = lastTransmission != null ? lastTransmission : newestSample;
            Boolean connected = isConnected(snapshot);
            boolean silent = isSilent(lastReport, Instant.now(), getMaxSilence()) || Boolean.FALSE.equals(connected);
            deviceSilent = silent;

            getThing().getChannels().forEach(channel -> {
                if (CHANNEL_LAST_UPDATE.equals(channel.getUID().getIdWithoutGroup())) {
                    updateState(channel.getUID(), lastReport == null ? UnDefType.UNDEF : new DateTimeType(lastReport));
                } else {
                    updateChannel(channel.getUID(), appliance, snapshot);
                }
            });
            if (newestSample != null) {
                lastPublishedSample = newestSample;
            }
            updateDeviceStatus(silent, lastReport, connected);
        } catch (GroheOndusApiException e) {
            handleApiException(e);
            getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
        } catch (RuntimeException e) {
            // Keep the scheduled polling alive; an uncaught exception would silently cancel it
            logger.warn("Unexpected error while updating {}", thing.getUID(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            pollLock.unlock();
        }
    }

    private void updateDeviceStatus(boolean silent, @Nullable Instant lastReport, @Nullable Boolean connected) {
        if (!silent) {
            updateStatus(ThingStatus.ONLINE);
            return;
        }
        if (lastReport == null) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.device.nodata");
            return;
        }
        ZoneId zone = timeZoneProvider.getTimeZone();
        String since = STATUS_DATE_FORMAT.format(lastReport.atZone(zone));
        String key = Boolean.FALSE.equals(connected) ? "error.device.disconnected" : "error.device.silent";
        logger.debug("Device {} has not reported since {} (connected: {})", thing.getUID(), since, connected);
        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/" + key + " [\"" + since + "\"]");
    }

    /**
     * Replaces the appliance properties so that values the cloud no longer reports are removed, as
     * {@link #updateProperties(Map)} only adds or changes entries.
     */
    private void updateApplianceProperties(Map<String, String> reported) {
        Map<String, String> current = getThing().getProperties();
        Map<String, String> merged = mergeApplianceProperties(current, reported);
        if (!merged.equals(current)) {
            updateThing(editThing().withProperties(merged).build());
        }
    }

    static Map<String, String> mergeApplianceProperties(Map<String, String> current, Map<String, String> reported) {
        Map<String, String> merged = new HashMap<>(current);
        merged.keySet().removeAll(ApplianceInfo.PROPERTY_KEYS);
        merged.putAll(reported);
        return merged;
    }

    /**
     * @return true if the device has not reported for longer than {@code maxAge}, or never
     */
    static boolean isSilent(@Nullable Instant lastReport, Instant now, Duration maxAge) {
        return lastReport == null || Duration.between(lastReport, now).compareTo(maxAge) > 0;
    }

    /**
     * @return true if the last poll found that the device itself has stopped reporting
     */
    protected boolean isDeviceSilent() {
        return deviceSilent;
    }

    /**
     * @return the maximum time without data from the device before it is considered silent
     */
    protected abstract Duration getMaxSilence();

    /**
     * @return the cloud's view of whether the device is connected, or null if not known
     */
    protected @Nullable Boolean isConnected(S snapshot) {
        return null;
    }

    protected void handleApiException(GroheOndusApiException e) {
        if (e instanceof GroheOndusAuthenticationException) {
            GroheOndusAccountHandler accountHandler = getAccountHandler();
            if (accountHandler != null) {
                accountHandler.reauthenticate();
            }
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof GroheOndusNotFoundException) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.appliance.notfound");
        } else {
            logger.debug("Communication with GROHE ONDUS failed for {}", thing.getUID(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Fetches all data needed to update the channels. Called from the polling thread.
     */
    protected abstract S fetch(GroheOndusApi api, ApplianceInfo appliance) throws GroheOndusApiException;

    /**
     * Updates a channel from the fetched snapshot. Must not perform I/O.
     */
    protected abstract void updateChannel(ChannelUID channelUID, ApplianceInfo appliance, S snapshot);

    protected abstract @Nullable Instant getNewestSampleTime(S snapshot);

    /**
     * Sends all samples newer than the last published sample as a time series, so that values the device reported
     * in batch are persisted with their original timestamps and not only the most recent one.
     */
    protected <T, Q extends Quantity<Q>> void sendHistory(ChannelUID channelUID, List<T> samples,
            Function<T, Instant> timestampFunction, Function<T, @Nullable Float> valueFunction, Unit<Q> unit) {
        TimeSeries timeSeries = buildTimeSeries(samples, timestampFunction, valueFunction, unit, lastPublishedSample);
        if (timeSeries.size() > 0) {
            sendTimeSeries(channelUID, timeSeries);
        }
    }

    static <T, Q extends Quantity<Q>> TimeSeries buildTimeSeries(List<T> samples,
            Function<T, Instant> timestampFunction, Function<T, @Nullable Float> valueFunction, Unit<Q> unit,
            @Nullable Instant after) {
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.ADD);
        for (T sample : samples) {
            Instant timestamp = timestampFunction.apply(sample);
            Float value = valueFunction.apply(sample);
            if (value != null && (after == null || timestamp.isAfter(after))) {
                timeSeries.add(timestamp, new QuantityType<>(value, unit));
            }
        }
        return timeSeries;
    }

    protected static <Q extends Quantity<Q>> State toState(@Nullable Float value, Unit<Q> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State getPauseState() {
        if (!pauseStateKnown) {
            return UnDefType.UNDEF;
        }
        Instant snoozeUntil = this.snoozeUntil;
        if (snoozeUntil == null) {
            return new QuantityType<>(0, Units.MINUTE);
        }

        long remainingSeconds = Duration.between(Instant.now(), snoozeUntil).getSeconds();
        if (remainingSeconds <= 0) {
            this.snoozeUntil = null;
            return new QuantityType<>(0, Units.MINUTE);
        }

        long remainingMinutes = (remainingSeconds + 59) / 60;
        return new QuantityType<>(remainingMinutes, Units.MINUTE);
    }

    private void handlePauseCommand(ChannelUID channelUID, Command command) {
        Integer durationMinutes = null;
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> minutes = quantityCommand.toUnit(Units.MINUTE);
            if (minutes != null) {
                durationMinutes = minutes.intValue();
            }
        } else if (command instanceof DecimalType decimalCommand) {
            durationMinutes = decimalCommand.intValue();
        }

        if (durationMinutes == null) {
            logger.debug("Invalid command received for channel {}. Expected Number:Time, received {}.", channelUID,
                    command.getClass().getName());
            return;
        }
        if (durationMinutes < 0 || durationMinutes > MAX_PAUSE_MINUTES) {
            logger.debug("Pause duration for thing {} must be between 0 and {} minutes, got {}", thing.getUID(),
                    MAX_PAUSE_MINUTES, durationMinutes);
            return;
        }

        final int finalDurationMinutes = durationMinutes;
        runApiCommand(api -> {
            api.setPause(getApplianceRef(), finalDurationMinutes);
            this.snoozeUntil = finalDurationMinutes == 0 ? null : Instant.now().plusSeconds(finalDurationMinutes * 60L);
            this.pauseStateKnown = true;
            updateState(CHANNEL_PAUSE, new QuantityType<>(finalDurationMinutes, Units.MINUTE));
        });
    }

    @FunctionalInterface
    protected interface ApiCommand {
        void execute(GroheOndusApi api) throws GroheOndusApiException;
    }

    /**
     * Runs a command against the API on the scheduler so that command handling does not block. The command is
     * discarded if the handler is disposed or re-initialized before it runs.
     */
    protected void runApiCommand(ApiCommand command) {
        int generation = commandGeneration;
        submitCommand(() -> {
            if (generation != commandGeneration) {
                logger.debug("Discarding command for {}, handler was disposed or re-initialized", thing.getUID());
                return;
            }
            GroheOndusApi api = getApi();
            if (api == null) {
                logger.debug("Cannot send command for {}, bridge is not online", thing.getUID());
                return;
            }
            try {
                command.execute(api);
            } catch (GroheOndusApiException e) {
                if (generation == commandGeneration) {
                    handleApiException(e);
                }
            }
        });
    }

    void submitCommand(Runnable task) {
        scheduler.execute(task);
    }

    protected @Nullable GroheOndusAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        if (handler instanceof GroheOndusAccountHandler accountHandler) {
            return accountHandler;
        }
        return null;
    }

    protected @Nullable GroheOndusApi getApi() {
        GroheOndusAccountHandler handler = getAccountHandler();
        return handler == null ? null : handler.getApi();
    }
}
