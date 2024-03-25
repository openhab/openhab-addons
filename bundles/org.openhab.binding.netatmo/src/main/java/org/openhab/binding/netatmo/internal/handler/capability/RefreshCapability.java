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
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshCapability} is the base class used to define refreshing policies
 * It implementats of a fixed refresh rate strategy.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshCapability extends Capability {
    private static final Duration ASAP = Duration.ofSeconds(2);
    private static final Duration OFFLINE_DELAY = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(RefreshCapability.class);

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    protected Duration dataValidity;

    public RefreshCapability(CommonInterface handler, Duration dataValidity) {
        super(handler);
        this.dataValidity = dataValidity;
        if (dataValidity.isNegative()) {
            throw new IllegalArgumentException("RefreshInterval must be positive or nul");
        }
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
        freeJobAndReschedule(ASAP);
    }

    private void proceedWithUpdate() {
        handler.proceedWithUpdate();
        Duration delay = calcDelay();
        if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
            logger.debug("{} is not ONLINE, special refresh interval is used", thingUID);
            delay = OFFLINE_DELAY;
        }
        logger.debug("{} refreshed, next one in {}", thingUID, delay);
        freeJobAndReschedule(delay);
    }

    protected Duration calcDelay() {
        return dataValidity;
    }

    private void freeJobAndReschedule(@Nullable Duration delay) {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.ofNullable(delay != null
                ? handler.getScheduler().schedule(() -> proceedWithUpdate(), delay.toSeconds(), TimeUnit.SECONDS)
                : null);
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("dataValidity", dataValidity.toString());
        super.afterNewData(newData);
    }
}
