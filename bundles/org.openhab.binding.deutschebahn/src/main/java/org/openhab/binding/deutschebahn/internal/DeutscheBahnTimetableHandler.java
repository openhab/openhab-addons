/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.TimetableLoader;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1Api;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1ApiFactory;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The {@link DeutscheBahnTimetableHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class DeutscheBahnTimetableHandler extends BaseBridgeHandler {

    /**
     * Wrapper containing things grouped by their position and calculates the max. required position.
     */
    private static final class GroupedThings {

        private int maxPosition = 0;
        private final Map<Integer, List<Thing>> thingsPerPosition = new HashMap<>();

        public void addThing(Thing thing) {
            if (isTrain(thing)) {
                int position = thing.getConfiguration().as(DeutscheBahnTrainConfiguration.class).position;
                this.maxPosition = Math.max(this.maxPosition, position);
                List<Thing> thingsAtPosition = this.thingsPerPosition.get(position);
                if (thingsAtPosition == null) {
                    thingsAtPosition = new ArrayList<>();
                    this.thingsPerPosition.put(position, thingsAtPosition);
                }
                thingsAtPosition.add(thing);
            }
        }

        /**
         * Returns the things at the given position.
         */
        @Nullable
        public List<Thing> getThingsAtPosition(int position) {
            return this.thingsPerPosition.get(position);
        }

        /**
         * Returns the max. configured position.
         */
        public int getMaxPosition() {
            return this.maxPosition;
        }
    }

    private static final long UPDATE_INTERVAL_SECONDS = 30;

    private final Lock monitor = new ReentrantLock();
    private @Nullable ScheduledFuture<?> updateJob;

    private final Logger logger = LoggerFactory.getLogger(DeutscheBahnTimetableHandler.class);
    private @Nullable DeutscheBahnTimetableConfiguration config;
    private @Nullable TimetableLoader loader;

    private TimetablesV1ApiFactory timetablesV1ApiFactory;

    private Supplier<Date> currentTimeProvider;

    /**
     * Creates an new {@link DeutscheBahnTimetableHandler}.
     */
    public DeutscheBahnTimetableHandler( //
            final Bridge bridge, //
            TimetablesV1ApiFactory timetablesV1ApiFactory, //
            final Supplier<Date> currentTimeProvider) {
        super(bridge);
        this.timetablesV1ApiFactory = timetablesV1ApiFactory;
        this.currentTimeProvider = currentTimeProvider;
    }

    private List<TimetableStop> loadTimetable() {
        if (this.loader == null) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return Collections.emptyList();
        }

        try {
            @SuppressWarnings("null")
            final List<TimetableStop> stops = this.loader.getTimetableStops();
            this.updateStatus(ThingStatus.ONLINE);
            return stops;
        } catch (final IOException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * The Bridge-Handler does not handle any commands.
     */
    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        this.config = this.getConfigAs(DeutscheBahnTimetableConfiguration.class);

        try {
            final TimetablesV1Api api = this.timetablesV1ApiFactory.create(this.config.accessToken,
                    HttpUtil::executeUrl);

            final TimetableStopFilter stopFilter = this.config.getTimetableStopFilter();

            final EventType eventSelection = stopFilter == TimetableStopFilter.ARRIVALS ? EventType.ARRIVAL
                    : EventType.ARRIVAL;

            this.loader = new TimetableLoader( //
                    api, //
                    stopFilter, //
                    eventSelection, //
                    currentTimeProvider, //
                    this.config.evaNo, //
                    1); // will be updated on first call

            this.updateStatus(ThingStatus.UNKNOWN);

            this.scheduler.execute(() -> {
                this.updateChannels();
                this.restartJob();
            });
        } catch (JAXBException | SAXException | URISyntaxException e) {
            this.logger.error("Error initializing api", e);
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        this.stopUpdateJob();
    }

    /**
     * Schedules an job that updates the timetable every 30 seconds.
     */
    private void restartJob() {
        this.logger.debug("Restarting jobs for bridge {}", this.getThing().getUID());
        this.monitor.lock();
        try {
            this.stopUpdateJob();
            if (this.getThing().getStatus() == ThingStatus.ONLINE) {
                this.updateJob = this.scheduler.scheduleWithFixedDelay(//
                        this::updateChannels, //
                        0L, //
                        UPDATE_INTERVAL_SECONDS, //
                        TimeUnit.SECONDS //
                );

                this.logger.debug("Scheduled {} update of deutsche bahn timetable", this.updateJob);
            }
        } finally {
            this.monitor.unlock();
        }
    }

    /**
     * Stops the update job.
     */
    private void stopUpdateJob() {
        this.monitor.lock();
        try {
            final ScheduledFuture<?> job = this.updateJob;
            if (job != null) {
                job.cancel(true);
            }
            this.updateJob = null;
        } finally {
            this.monitor.unlock();
        }
    }

    @SuppressWarnings("null")
    private void updateChannels() {
        if (this.loader == null) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        final GroupedThings groupedThings = this.groupThingsPerPosition();
        this.loader.setStopCount(groupedThings.getMaxPosition());
        final List<TimetableStop> timetableStops = this.loadTimetable();
        if (timetableStops.isEmpty()) {
            return;
        }

        this.logger.debug("Retrieved {} timetable stops.", timetableStops.size());
        this.updateThings(groupedThings, timetableStops);
    }

    private void updateThings(GroupedThings groupedThings, final List<TimetableStop> timetableStops) {
        int position = 1;
        for (final TimetableStop stop : timetableStops) {
            final List<Thing> thingsAtPosition = groupedThings.getThingsAtPosition(position);

            if (thingsAtPosition != null) {
                for (Thing thing : thingsAtPosition) {
                    final ThingHandler thingHandler = thing.getHandler();
                    if (thingHandler != null) {
                        assert thingHandler instanceof DeutscheBahnTrainHandler;
                        ((DeutscheBahnTrainHandler) thingHandler).updateChannels(stop);
                    }
                }
            }
            position++;
        }
    }

    /**
     * Returns an map containing the things grouped by timetable stop position.
     */
    private GroupedThings groupThingsPerPosition() {
        final GroupedThings groupedThings = new GroupedThings();
        for (Thing child : this.getThing().getThings()) {
            groupedThings.addThing(child);
        }
        return groupedThings;
    }

    private static boolean isTrain(Thing thing) {
        final ThingTypeUID thingTypeUid = thing.getThingTypeUID();
        return thingTypeUid.equals(DeutscheBahnBindingConstants.TRAIN_TYPE);
    }
}
