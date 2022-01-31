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
package org.openhab.binding.fmiweather.internal;

import static org.openhab.binding.fmiweather.internal.BindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fmiweather.internal.client.Client;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.Request;
import org.openhab.binding.fmiweather.internal.client.exception.FMIResponseException;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractWeatherHandler extends BaseThingHandler {

    private static final ZoneId UTC = ZoneId.of("UTC");
    protected static final String PROP_LONGITUDE = "longitude";
    protected static final String PROP_LATITUDE = "latitude";
    protected static final String PROP_NAME = "name";
    protected static final String PROP_REGION = "region";
    private static final long REFRESH_THROTTLE_MILLIS = 10_000;

    protected static final int TIMEOUT_MILLIS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(AbstractWeatherHandler.class);

    protected volatile @NonNullByDefault({}) Client client;
    protected final AtomicReference<@Nullable ScheduledFuture<?>> futureRef = new AtomicReference<>();
    protected volatile @Nullable FMIResponse response;
    protected volatile int pollIntervalSeconds = 120; // reset by subclasses

    private volatile long lastRefreshMillis = 0;
    private final AtomicReference<@Nullable ScheduledFuture<?>> updateChannelsFutureRef = new AtomicReference<>();

    public AbstractWeatherHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            ScheduledFuture<?> prevFuture = updateChannelsFutureRef.get();
            ScheduledFuture<?> newFuture = updateChannelsFutureRef
                    .updateAndGet(fut -> fut == null || fut.isDone() ? submitUpdateChannelsThrottled() : fut);
            assert newFuture != null; // invariant
            if (logger.isTraceEnabled()) {
                long delayRemainingMillis = newFuture.getDelay(TimeUnit.MILLISECONDS);
                if (delayRemainingMillis <= 0) {
                    logger.trace("REFRESH received. Channels are updated");
                } else {
                    logger.trace("REFRESH received. Delaying by {} ms to avoid throttle excessive REFRESH",
                            delayRemainingMillis);
                }
                // Compare by reference to check if the future changed
                if (prevFuture == newFuture) {
                    logger.trace("REFRESH received. Previous refresh ongoing, will wait for it to complete in {} ms",
                            lastRefreshMillis + REFRESH_THROTTLE_MILLIS - System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public void initialize() {
        client = new Client();
        updateStatus(ThingStatus.UNKNOWN);
        rescheduleUpdate(0, false);
    }

    /**
     * Call updateChannels asynchronously, possibly in a delayed fashion to throttle updates. This protects against a
     * situation where many channels receive REFRESH command, e.g. when openHAB is requesting to update channels
     *
     * @return scheduled future
     */
    private ScheduledFuture<?> submitUpdateChannelsThrottled() {
        long now = System.currentTimeMillis();
        long nextRefresh = lastRefreshMillis + REFRESH_THROTTLE_MILLIS;
        lastRefreshMillis = now;
        if (now > nextRefresh) {
            return scheduler.schedule(this::updateChannels, 0, TimeUnit.MILLISECONDS);
        } else {
            long delayMillis = nextRefresh - now;
            return scheduler.schedule(this::updateChannels, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    protected abstract void updateChannels();

    protected abstract Request getRequest();

    protected void update(int retry) {
        if (retry < RETRIES) {
            try {
                response = client.query(getRequest(), TIMEOUT_MILLIS);
            } catch (FMIUnexpectedResponseException e) {
                handleError(e, retry);
                return;
            } catch (FMIResponseException e) {
                handleError(e, retry);
                return;
            }
        } else {
            logger.trace("Query failed. Retries exhausted, not trying again until next poll.");
        }
        // Update channel (if we have received a response)
        updateChannels();
        // Channels updated successfully or exhausted all retries. Reschedule new update
        rescheduleUpdate(pollIntervalSeconds * 1000, false);
    }

    @Override
    public void dispose() {
        super.dispose();
        response = null;
        cancel(futureRef.getAndSet(null), true);
        cancel(updateChannelsFutureRef.getAndSet(null), true);
    }

    protected static int lastValidIndex(Data data) {
        if (data.values.length < 2) {
            throw new IllegalStateException("Excepted at least two data items");
        }
        for (int i = data.values.length - 1; i >= 0; i--) {
            if (data.values[i] != null) {
                return i;
            }
        }
        // if we have reached here, it means that array was full of nulls
        return -1;
    }

    protected static long floorToEvenMinutes(long epochSeconds, int roundMinutes) {
        long roundSecs = roundMinutes * 60;
        return (epochSeconds / roundSecs) * roundSecs;
    }

    protected static long ceilToEvenMinutes(long epochSeconds, int roundMinutes) {
        double epochDouble = epochSeconds;
        long roundSecs = roundMinutes * 60;
        double roundSecsDouble = (roundMinutes * 60);
        return (long) Math.ceil(epochDouble / roundSecsDouble) * roundSecs;
    }

    /**
     * Update QuantityType channel state
     *
     * @param channelUID channel UID
     * @param epochSecond value to update
     * @param unit unit associated with the value
     */
    protected <T extends Quantity<T>> void updateEpochSecondStateIfLinked(ChannelUID channelUID, long epochSecond) {
        if (isLinked(channelUID)) {
            updateState(channelUID, new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), UTC)
                    .withZoneSameInstant(ZoneId.systemDefault())));
        }
    }

    /**
     * Update QuantityType or DecimalType channel state
     *
     * Updates UNDEF state when value is null
     *
     * @param channelUID channel UID
     * @param value value to update
     * @param unit unit associated with the value
     */
    protected void updateStateIfLinked(ChannelUID channelUID, @Nullable BigDecimal value, @Nullable Unit<?> unit) {
        if (isLinked(channelUID)) {
            if (value == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else if (unit == null) {
                updateState(channelUID, new DecimalType(value));
            } else {
                updateState(channelUID, new QuantityType<>(value, unit));
            }
        }
    }

    /**
     * Unwrap optional value and log with ERROR if value is not present
     *
     * This should be used only when we expect value to be present, and the reason for missing value corresponds to
     * description of {@link FMIUnexpectedResponseException}.
     *
     * @param optional optional to unwrap
     * @param messageIfNotPresent logging message
     * @param args arguments to logging
     * @throws FMIUnexpectedResponseException when value is not present
     * @return unwrapped value of the optional
     */
    protected <T> T unwrap(Optional<T> optional, String messageIfNotPresent, Object... args)
            throws FMIUnexpectedResponseException {
        if (optional.isPresent()) {
            return optional.get();
        } else {
            // logger.error(messageIfNotPresent, args) avoided due to static analyzer
            String formattedMessage = String.format(messageIfNotPresent, args);
            throw new FMIUnexpectedResponseException(formattedMessage);
        }
    }

    protected void handleError(FMIResponseException e, int retry) {
        response = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
        logger.trace("Query failed. Increase retry count {} and try again. Error: {} {}", retry, e.getClass().getName(),
                e.getMessage());
        // Try again, with increased retry count
        rescheduleUpdate(RETRY_DELAY_MILLIS, false, retry + 1);
    }

    protected void rescheduleUpdate(long delayMillis, boolean mayInterruptIfRunning) {
        rescheduleUpdate(delayMillis, mayInterruptIfRunning, 0);
    }

    protected void rescheduleUpdate(long delayMillis, boolean mayInterruptIfRunning, int retry) {
        cancel(futureRef.getAndSet(scheduler.schedule(() -> this.update(retry), delayMillis, TimeUnit.MILLISECONDS)),
                mayInterruptIfRunning);
    }

    private static void cancel(@Nullable ScheduledFuture<?> future, boolean mayInterruptIfRunning) {
        if (future != null) {
            future.cancel(mayInterruptIfRunning);
        }
    }
}
