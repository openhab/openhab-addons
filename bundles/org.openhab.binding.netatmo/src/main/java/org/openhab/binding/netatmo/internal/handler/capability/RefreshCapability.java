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
package org.openhab.binding.netatmo.internal.handler.capability;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
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
    private static final Duration DEFAULT_DELAY = Duration.of(20, SECONDS);
    private static final Duration PROBING_INTERVAL = Duration.of(120, SECONDS);

    private final Logger logger = LoggerFactory.getLogger(RefreshCapability.class);
    // private final ApiBridgeHandler apiBridge;
    private final ScheduledExecutorService scheduler;

    private Duration dataValidity;
    private ZonedDateTime dataTimeStamp = ZonedDateTime.now();
    private @Nullable ZonedDateTime dataTimeStamp0;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public RefreshCapability(NACommonInterface handler /* , ApiBridgeHandler apiBridge, */,
            ScheduledExecutorService scheduler, int refreshInterval) {
        super(handler);
        // this.apiBridge = apiBridge;
        this.scheduler = scheduler;
        this.dataValidity = Duration.ofMillis(Math.max(0, refreshInterval));
    }

    @Override
    public void initialize() {
        super.initialize();
        // When setting the connection listener the apiBridge will trigger connectionEvent
        // apiBridge.addConnectionListener(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        // apiBridge.removeConnectionListener(this);
        freeJobAndReschedule(0);
    }

    // TODO : clean this now that connection listener has disappeared
    public void connectionEvent(boolean connected) {
        if (!connected) {
            handler.setThingStatus(ThingStatus.OFFLINE, "@text/status-bridge-offline");
            freeJobAndReschedule(0);
        } else if (!ThingStatus.ONLINE.equals(thing.getStatus())) {
            handler.setThingStatus(ThingStatus.ONLINE, null);
            // Wait a little bit before refreshing because a dispose could be running in parallel
            freeJobAndReschedule(2);
        }
    }

    @Override
    public void expireData() {
        dataTimeStamp = ZonedDateTime.now().minus(dataValidity);
        freeJobAndReschedule(1);
    }

    private Duration dataAge() {
        return Duration.between(dataTimeStamp, ZonedDateTime.now());
    }

    private boolean probing() {
        return dataValidity.getSeconds() <= 0;
    }

    private void proceedWithUpdate() {
        handler.proceedWithUpdate(false);
        long delay = (probing() ? PROBING_INTERVAL : dataValidity.minus(dataAge()).plus(DEFAULT_DELAY)).toSeconds();
        delay = delay < 2 ? PROBING_INTERVAL.toSeconds() : delay;
        logger.debug("Module refreshed, next one in {} s", delay);
        freeJobAndReschedule(delay);
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        newData.getLastSeen().ifPresent(timeStamp -> {
            if (probing()) { // we're still probin
                ZonedDateTime firstTimeStamp = dataTimeStamp0;
                if (firstTimeStamp == null) {
                    dataTimeStamp0 = timeStamp;
                    logger.debug("First data timestamp is {}", dataTimeStamp0);
                } else if (timeStamp.isAfter(firstTimeStamp)) {
                    dataValidity = Duration.between(firstTimeStamp, timeStamp);
                    logger.debug("Data validity period identified to be {}", dataValidity);
                } else {
                    logger.debug("Data validity period not yet found - data timestamp unchanged");
                }
            }
            dataTimeStamp = timeStamp;
        });
    }

    private void freeJobAndReschedule(long delay) {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.ofNullable(delay == 0 ? null //
                : scheduler.schedule(() -> proceedWithUpdate(), delay, TimeUnit.SECONDS));
    }
}
