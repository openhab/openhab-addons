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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    protected static final Duration PROBING_INTERVAL = Duration.ofMinutes(2);

    private final Logger logger = LoggerFactory.getLogger(RefreshCapability.class);

    protected Duration dataValidity = PROBING_INTERVAL;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public RefreshCapability(CommonInterface handler) {
        super(handler);
    }

    public void setInterval(Duration dataValidity) {
        if (dataValidity.isNegative() || dataValidity.isZero()) {
            throw new IllegalArgumentException("refreshInterval must be positive or nul");
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
        rescheduleJob(ASAP);
    }

    protected Duration calcDelay() {
        return dataValidity;
    }

    private void proceedWithUpdate() {
        Duration delay;
        handler.proceedWithUpdate();
        if (!ThingStatus.ONLINE.equals(handler.getThing().getStatus())) {
            delay = OFFLINE_DELAY;
            logger.debug("Thing '{}' is not ONLINE, using special refresh interval", thingUID);
        } else {
            delay = calcDelay();
        }
        rescheduleJob(delay);
    }

    private void rescheduleJob(Duration delay) {
        stopJob();
        logger.debug("'{}' next refresh in {}", thingUID, delay);
        refreshJob = handler.schedule(this::proceedWithUpdate, delay);
    }

    private void stopJob() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

}
