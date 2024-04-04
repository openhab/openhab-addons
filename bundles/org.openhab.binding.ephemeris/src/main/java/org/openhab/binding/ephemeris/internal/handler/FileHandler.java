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

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.configuration.FileConfiguration;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FileHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FileHandler extends BaseThingHandler {
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 0;
    private static final int REFRESH_FIRST_MINUTE_OF_DAY = 1;
    private final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    private final ZoneId zoneId;
    private final EphemerisManager ephemeris;
    private Optional<File> definitionFile = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public FileHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing);
        this.zoneId = zoneId;
        this.ephemeris = ephemerisManager;
    }

    @Override
    public void initialize() {
        FileConfiguration config = getConfigAs(FileConfiguration.class);
        File file = new File(BINDING_DATA_PATH, config.fileName);

        updateStatus(ThingStatus.UNKNOWN);

        if (!file.exists()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "File is absent: " + file.getAbsolutePath());
            return;
        }

        definitionFile = Optional.of(file);
        scheduler.execute(this::updateData);
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
        super.dispose();
    }

    private void updateData() {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameLocal(zoneId);
        definitionFile.map(def -> def.getAbsolutePath()).ifPresent(path -> {
            try {

                // ZonedDateTime today = LocalDate.now().atStartOfDay(zoneId);
                ZonedDateTime today = LocalDate.of(2024, 3, 3).atStartOfDay(zoneId);
                String todayEvent = ephemeris.getBankHolidayName(today, path);
                updateState(CHANNEL_CURRENT_EVENT_TITLE, toStringType(todayEvent));

                String nextEvent = "";
                ZonedDateTime nextDay = today;

                for (int offset = 1; offset < 366 && (nextEvent == null || nextEvent.isEmpty()); offset++) {
                    nextDay = today.plusDays(offset);
                    nextEvent = ephemeris.getBankHolidayName(nextDay, path);
                }

                updateState(CHANNEL_NEXT_EVENT_TITLE, toStringType(nextEvent));
                updateState(CHANNEL_NEXT_REMAINING,
                        new QuantityType<>(Duration.between(today, nextDay).toDays(), Units.DAY));
                updateState(CHANNEL_NEXT_EVENT_START, new DateTimeType(nextDay));

                updateStatus(ThingStatus.ONLINE);
            } catch (IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Incorrect syntax");
            } catch (FileNotFoundException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "File is absent: " + path);
            }
        });

        ZonedDateTime nextUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                .withMinute(REFRESH_FIRST_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);
        long delay = ChronoUnit.MINUTES.between(now, nextUpdate);
        logger.info("Scheduling next ephemeris update in {} minutes", delay);
        refreshJob = Optional.of(scheduler.schedule(this::updateData, delay, TimeUnit.MINUTES));
    }

    private State toStringType(@Nullable String event) {
        return event == null || event.isEmpty() ? UnDefType.NULL : new StringType(event);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
