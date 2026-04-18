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
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshCapability} is the base class used to define refreshing policies
 * It implements of a fixed refresh rate strategy.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshCapability extends Capability {
    public static final Duration ASAP = Duration.ofSeconds(2);
    protected static final Duration OFFLINE_DELAY = Duration.ofMinutes(15);
    protected static final Duration PROBING_INTERVAL = Duration.ofMinutes(2);

    private final Logger logger = LoggerFactory.getLogger(RefreshCapability.class);

    protected Duration dataValidity = PROBING_INTERVAL;
    private @Nullable ScheduledFuture<?> refreshJob;
    private boolean expiring = false;

    public RefreshCapability(CommonInterface handler) {
        super(handler);
    }

    public void setInterval(Duration dataValidity) {
        if (!dataValidity.isPositive()) {
            throw new IllegalArgumentException("refreshInterval must be positive");
        }
        this.dataValidity = dataValidity;
        expireData();
    }

    @Override
    public void dispose() {
        stopJob();
        super.dispose();
    }

    @Override
    public void expireData() {
        if (!expiring) {
            expiring = true;
            rescheduleJob(ASAP);
        }
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        expiring = false;
        super.afterNewData(newData);
    }

    protected Duration calcDelay() {
        return dataValidity;
    }

    private void proceedWithUpdate() {
        Duration delay;
        handler.proceedWithUpdate();
        if (handler.getAccountHandler() instanceof ApiBridgeHandler accountHandler
                && !ThingStatus.ONLINE.equals(accountHandler.getThing().getStatus())) {
            delay = accountHandler.getIdleTime();
            delay = delay != null ? delay.plus(ASAP) : OFFLINE_DELAY;
            logger.debug("Bridge is not ONLINE, will wait for him to come-back in {}", delay);
        } else if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
            delay = OFFLINE_DELAY;
            logger.debug("Thing '{}' is not ONLINE, special refresh interval {} used", thingUID, delay);
        } else {
            delay = calcDelay();
        }
        rescheduleJob(delay);
    }

    private void rescheduleJob(Duration delay) {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            Instant now = Instant.now();
            Instant expectedExecution = now.plus(delay);
            Instant scheduledExecution = now.plusMillis(refreshJob.getDelay(TimeUnit.MILLISECONDS));
            if (Math.abs(ChronoUnit.SECONDS.between(expectedExecution, scheduledExecution)) <= 3) {
                logger.debug("'{}' refresh as already pending roughly as the same time, will not reschedule", thingUID);
                return;
            }
            stopJob();
        }
        logger.debug("'{}' next refresh in {}", thingUID, delay);
        this.refreshJob = handler.schedule(this::proceedWithUpdate, delay);
    }

    private void stopJob() {
        if (refreshJob instanceof ScheduledFuture job) {
            job.cancel(true);
        }
        refreshJob = null;
    }
}
