/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshCapability} is the class used to embed the refreshing needs calculation for devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshCapability extends Capability {
    private static final Duration NOW_DELAY = Duration.ofSeconds(2);
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(20);
    private static final Duration PROBING_INTERVAL = Duration.ofMinutes(2);
    private static final Duration OFFLINE_INTERVAL = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(RefreshCapability.class);

    private Duration dataValidity;
    private Instant dataTimeStamp = Instant.MIN;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public RefreshCapability(CommonInterface handler, int refreshInterval) {
        super(handler);
        // refreshInterval set to -1 if not defined on the thing (default value)
        this.dataValidity = Duration.ofSeconds(Math.max(0, refreshInterval));
    }

    @Override
    public void initialize() {
        expireData();
    }

    @Override
    public void dispose() {
        freeJobAndReschedule(null);
        super.dispose();
    }

    @Override
    public void expireData() {
        dataTimeStamp = Instant.MIN;
        freeJobAndReschedule(NOW_DELAY);
    }

    private boolean probing() {
        return dataValidity.getSeconds() <= 0;
    }

    private void proceedWithUpdate() {
        handler.proceedWithUpdate();
        Duration delay;
        if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
            logger.debug("{} is not ONLINE, special refresh interval is used", thingUID);
            delay = OFFLINE_INTERVAL;
            if (probing()) {
                dataTimeStamp = Instant.MIN;
            }
        } else if (probing()) {
            delay = PROBING_INTERVAL;
        } else {
            Duration dataAge = Duration.between(dataTimeStamp, Instant.now());
            delay = dataValidity.minus(dataAge).plus(DEFAULT_DELAY);
            delay = delay.compareTo(NOW_DELAY) < 0 ? PROBING_INTERVAL : delay;
        }
        logger.debug("{} refreshed, next one in {}s", thingUID, delay);
        freeJobAndReschedule(delay);
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        newData.getLastSeen().map(ZonedDateTime::toInstant).ifPresent(lastSeen -> {
            if (probing()) {
                if (Instant.MIN.equals(dataTimeStamp)) {
                    logger.debug("First data timestamp for {} is {}", thingUID, lastSeen);
                } else if (lastSeen.isAfter(dataTimeStamp)) {
                    dataValidity = Duration.between(dataTimeStamp, lastSeen);
                    logger.debug("Data validity period for {} identified to be {}", thingUID, dataValidity);
                } else {
                    logger.debug("Data validity period for {} not yet found, reference timestamp unchanged", thingUID);
                }
            }
            dataTimeStamp = lastSeen;
        });
    }

    private void freeJobAndReschedule(@Nullable Duration delay) {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.ofNullable(delay != null
                ? handler.getScheduler().schedule(() -> proceedWithUpdate(), delay.toSeconds(), TimeUnit.SECONDS)
                : null);
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("Data Validity", dataValidity.toString() + (probing() ? " (probing)" : ""));
        super.afterNewData(newData);
    }
}
