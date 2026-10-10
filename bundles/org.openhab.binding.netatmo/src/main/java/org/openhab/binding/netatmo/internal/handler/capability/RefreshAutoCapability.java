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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshAutoCapability} implements probing and auto-adjusting refresh strategy
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshAutoCapability extends RefreshCapability {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(15);
    private static final Duration FIRST_BACKOFF_DELAY = Duration.ofSeconds(30);
    private static final Duration FIVE_MINUTES = Duration.ofMinutes(5);
    private static final Duration TEN_MINUTES = Duration.ofMinutes(10);
    private static final Duration API_BUSY_AFTER_ROUND_TEN_MINUTES = Duration.ofSeconds(60);
    private static final Duration API_BUSY_AFTER_ROUND_FIVE_MINUTES = Duration.ofSeconds(20);
    private static final Duration SPREAD_AFTER_BUSY_WINDOW = Duration.ofSeconds(15);
    private static final int FAILURES_BEFORE_OFFLINE = 5;

    private final Logger logger = LoggerFactory.getLogger(RefreshAutoCapability.class);

    private @Nullable Instant dataTimestamp = null;
    private final Duration slotAfterBusyWindow;
    private int consecutiveFailures = 0;
    private int probesWithoutNewData = 0;

    public RefreshAutoCapability(CommonInterface handler) {
        super(handler);
        slotAfterBusyWindow = slotAfterBusyWindow(thingUID);
    }

    @Override
    public void dispose() {
        consecutiveFailures = 0;
        probesWithoutNewData = 0;
        super.dispose();
    }

    void fetchFailed(NetatmoException e) {
        consecutiveFailures++;
        if (consecutiveFailures == FAILURES_BEFORE_OFFLINE) {
            handler.setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected boolean isRetrying() {
        return consecutiveFailures > 0;
    }

    @Override
    public void expireData() {
        dataTimestamp = null;
        super.expireData();
    }

    @Override
    protected Duration calcDelay() {
        return delayFrom(Instant.now());
    }

    Duration delayFrom(Instant now) {
        Instant planned = now.plus(plannedDelay(now));
        Instant moved = outsideBusyWindow(planned, slotAfterBusyWindow);
        if (!moved.equals(planned)) {
            logger.debug("{} poll moved from {} to {}", thingUID, planned, moved);
        }
        return Duration.between(now, moved);
    }

    private Duration plannedDelay(Instant now) {
        if (consecutiveFailures > 0) {
            return backoffDelay(consecutiveFailures);
        }
        Instant timestamp = dataTimestamp;
        if (timestamp == null) {
            return nextProbeDelay();
        }

        Duration dataAge = Duration.between(timestamp, now);
        Duration delay = dataValidity.minus(dataAge);

        if (delay.isPositive()) {
            probesWithoutNewData = 0;
            return delay.plus(DEFAULT_DELAY);
        }

        logger.debug("{} did not update data in expected time, return to probing", thingUID);
        dataTimestamp = null;
        return nextProbeDelay();
    }

    private Duration nextProbeDelay() {
        probesWithoutNewData++;
        return backoffDelay(probesWithoutNewData);
    }

    static Duration backoffDelay(int attempt) {
        Duration delay = FIRST_BACKOFF_DELAY;
        for (int doubling = 1; doubling < attempt && delay.compareTo(PROBING_INTERVAL) < 0; doubling++) {
            delay = delay.multipliedBy(2);
        }
        return delay.compareTo(PROBING_INTERVAL) < 0 ? delay : PROBING_INTERVAL;
    }

    /** Every thing keeps its own place behind a busy window, so that not all of them poll at its end. */
    static Duration slotAfterBusyWindow(ThingUID thingUID) {
        int thingSpecific = thingUID.getAsString().hashCode();
        return Duration.ofMillis(Math.floorMod(thingSpecific, SPREAD_AFTER_BUSY_WINDOW.toMillis()));
    }

    static Instant outsideBusyWindow(Instant planned, Duration slotAfterBusyWindow) {
        long sinceRoundFiveMinutes = Math.floorMod(planned.toEpochMilli(), FIVE_MINUTES.toMillis());
        long busyLeft = busyWindowLength(planned).toMillis() - sinceRoundFiveMinutes;
        return busyLeft > 0 ? planned.plusMillis(busyLeft).plus(slotAfterBusyWindow) : planned;
    }

    private static Duration busyWindowLength(Instant instant) {
        long sinceRoundTenMinutes = Math.floorMod(instant.toEpochMilli(), TEN_MINUTES.toMillis());
        boolean roundTenMinutes = sinceRoundTenMinutes < FIVE_MINUTES.toMillis();
        return roundTenMinutes ? API_BUSY_AFTER_ROUND_TEN_MINUTES : API_BUSY_AFTER_ROUND_FIVE_MINUTES;
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        if (consecutiveFailures > 0) {
            logger.debug("{} recovered after {} failed polls", thingUID, consecutiveFailures);
            consecutiveFailures = 0;
        }
        dataTimestamp = newData.getLastSeen() instanceof ZonedDateTime ls ? ls.toInstant() : null;
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("probing", Boolean.valueOf(dataTimestamp == null).toString());
        super.afterNewData(newData);
    }
}
