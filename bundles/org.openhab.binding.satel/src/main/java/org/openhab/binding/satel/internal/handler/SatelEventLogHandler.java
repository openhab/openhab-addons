/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.action.SatelEventLogActions;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.util.DeviceNameResolver;
import org.openhab.binding.satel.internal.util.EventLogReader;
import org.openhab.binding.satel.internal.util.EventLogReader.EventDescription;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelEventLogHandler} is responsible for handling commands, which are
 * sent to one of the event log channels.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelEventLogHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_EVENTLOG);

    private static final long CACHE_CLEAR_INTERVAL = TimeUnit.MINUTES.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(SatelEventLogHandler.class);
    private @Nullable EventLogReader eventLogReader;
    private @Nullable ScheduledFuture<?> cacheExpirationJob;

    /**
     * Represents single record of the event log.
     *
     * @author Krzysztof Goworek
     */
    public static class EventLogEntry {

        private final int index;
        private final int prevIndex;
        private final ZonedDateTime timestamp;
        private final String description;
        private final String details;

        private EventLogEntry(int index, int prevIndex, ZonedDateTime timestamp, String description, String details) {
            this.index = index;
            this.prevIndex = prevIndex;
            this.timestamp = timestamp;
            this.description = description;
            this.details = details;
        }

        /**
         * @return index of this record entry
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return index of the previous record entry in the log
         */
        public int getPrevIndex() {
            return prevIndex;
        }

        /**
         * @return date and time when the event occurred
         */
        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        /**
         * @return description of the event
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return details about zones, partitions, users, etc
         */
        public String getDetails() {
            return details;
        }

        @Override
        public String toString() {
            return "EventLogEntry [index=" + index + ", prevIndex=" + prevIndex + ", timestamp=" + timestamp
                    + ", description=" + description + ", details=" + details + "]";
        }
    }

    public SatelEventLogHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        withBridgeHandlerPresent(bridgeHandler -> this.eventLogReader = new EventLogReader(bridgeHandler,
                new DeviceNameResolver(bridgeHandler)));

        final ScheduledFuture<?> cacheExpirationJob = this.cacheExpirationJob;
        if (cacheExpirationJob == null || cacheExpirationJob.isCancelled()) {
            // for simplicity all cache entries are cleared every 30 minutes
            this.cacheExpirationJob = scheduler.scheduleWithFixedDelay(this::clearCache, CACHE_CLEAR_INTERVAL,
                    CACHE_CLEAR_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        final ScheduledFuture<?> cacheExpirationJob = this.cacheExpirationJob;
        if (cacheExpirationJob != null && !cacheExpirationJob.isCancelled()) {
            cacheExpirationJob.cancel(true);
        }
        this.cacheExpirationJob = null;
        this.eventLogReader = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("New command for {}: {}", channelUID, command);

        if (CHANNEL_INDEX.equals(channelUID.getId()) && command instanceof DecimalType decimalCommand) {
            int eventIndex = decimalCommand.intValue();
            withBridgeHandlerPresent(bridgeHandler -> readEvent(eventIndex).ifPresent(entry -> {
                // update items
                updateState(CHANNEL_INDEX, new DecimalType(entry.getIndex()));
                updateState(CHANNEL_PREV_INDEX, new DecimalType(entry.getPrevIndex()));
                updateState(CHANNEL_TIMESTAMP, new DateTimeType(entry.getTimestamp()));
                updateState(CHANNEL_DESCRIPTION, new StringType(entry.getDescription()));
                updateState(CHANNEL_DETAILS, new StringType(entry.getDetails()));
            }));
        }
    }

    @Override
    public void incomingEvent(ConnectionStatusEvent event) {
        logger.trace("Handling incoming event: {}", event);
        // we have just connected, change thing's status
        if (event.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SatelEventLogActions.class);
    }

    /**
     * Reads one record from the event log.
     *
     * @param eventIndex record index
     * @return record data or {@linkplain Optional#empty()} if there is no record under given index
     */
    public Optional<EventLogEntry> readEvent(int eventIndex) {
        final EventLogReader eventLogReader = this.eventLogReader;
        if (eventLogReader == null) {
            logger.warn("Unable to read event: handler is not properly initialized");
            return Optional.empty();
        }

        return readEvent(eventLogReader, eventIndex);
    }

    private Optional<EventLogEntry> readEvent(EventLogReader eventLogReader, int eventIndex) {
        return eventLogReader.readEvent(eventIndex)
                .map(eventDescription -> combineWithDetails(eventLogReader, eventDescription));
    }

    private EventLogEntry combineWithDetails(EventLogReader eventLogReader, EventDescription eventDescription) {
        String eventDetails = eventLogReader.buildDetails(eventDescription);

        return new EventLogEntry(eventDescription.getCurrentIndex(), eventDescription.getNextIndex(),
                eventDescription.getTimestamp().atZone(getBridgeHandler().getZoneId()), eventDescription.getText(),
                eventDetails);
    }

    private void clearCache() {
        final EventLogReader eventLogReader = this.eventLogReader;
        if (eventLogReader != null) {
            eventLogReader.clearCache();
        }
    }
}
