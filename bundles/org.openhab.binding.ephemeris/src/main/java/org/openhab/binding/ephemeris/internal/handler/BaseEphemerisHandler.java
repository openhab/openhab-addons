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
package org.openhab.binding.ephemeris.internal.handler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ephemeris.internal.EphemerisException;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseEphemerisHandler} is the base class for Ephemeris Things. It takes care of
 * update logic and update scheduling once a day.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class BaseEphemerisHandler extends BaseThingHandler {
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 0;
    private static final int REFRESH_FIRST_MINUTE_OF_DAY = 1;

    private final Logger logger = LoggerFactory.getLogger(BaseEphemerisHandler.class);
    private final ZoneId zoneId;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    protected final EphemerisManager ephemeris;

    public BaseEphemerisHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing);
        this.zoneId = zoneId;
        this.ephemeris = ephemerisManager;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = Optional.of(scheduler.schedule(this::updateData, 1, TimeUnit.SECONDS));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
        super.dispose();
    }

    private void updateData() {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameLocal(zoneId);

        logger.debug("Updating {} channels", getThing().getUID());
        try {
            internalUpdate(now.truncatedTo(ChronoUnit.DAYS));

            updateStatus(ThingStatus.ONLINE);
            ZonedDateTime nextUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                    .withMinute(REFRESH_FIRST_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);
            long delay = ChronoUnit.MINUTES.between(now, nextUpdate);
            logger.debug("Scheduling next {} update in {} minutes", getThing().getUID(), delay);
            refreshJob = Optional.of(scheduler.schedule(this::updateData, delay, TimeUnit.MINUTES));
        } catch (EphemerisException e) {
            updateStatus(ThingStatus.OFFLINE, e.getStatusDetail(), e.getMessage());
        }
    }

    protected abstract void internalUpdate(ZonedDateTime today) throws EphemerisException;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            updateData();
        }
    }
}
