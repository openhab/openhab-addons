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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.action.SatelEventLogActions;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.command.ReadEventCommand;
import org.openhab.binding.satel.internal.command.ReadEventDescCommand;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.types.IntegraType;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_EVENTLOG);

    private static final String NOT_AVAILABLE_TEXT = "N/A";
    private static final String DETAILS_SEPARATOR = ", ";
    private static final long CACHE_CLEAR_INTERVAL = TimeUnit.MINUTES.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(SatelEventLogHandler.class);
    private final Map<String, @Nullable EventDescriptionCacheEntry> eventDescriptions = new ConcurrentHashMap<>();
    private final Map<String, @Nullable String> deviceNameCache = new ConcurrentHashMap<>();
    private @Nullable ScheduledFuture<?> cacheExpirationJob;
    private Charset encoding = Charset.defaultCharset();

    /**
     * Represents single record of the event log.
     *
     * @author Krzysztof Goworek
     *
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

        withBridgeHandlerPresent(bridgeHandler -> {
            this.encoding = bridgeHandler.getEncoding();
        });

        final ScheduledFuture<?> cacheExpirationJob = this.cacheExpirationJob;
        if (cacheExpirationJob == null || cacheExpirationJob.isCancelled()) {
            // for simplicity all cache entries are cleared every 30 minutes
            this.cacheExpirationJob = scheduler.scheduleWithFixedDelay(deviceNameCache::clear, CACHE_CLEAR_INTERVAL,
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("New command for {}: {}", channelUID, command);

        if (CHANNEL_INDEX.equals(channelUID.getId()) && command instanceof DecimalType) {
            int eventIndex = ((DecimalType) command).intValue();
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
        return Collections.singleton(SatelEventLogActions.class);
    }

    /**
     * Reads one record from the event log.
     *
     * @param eventIndex record index
     * @return record data or {@linkplain Optional#empty()} if there is no record under given index
     */
    public Optional<EventLogEntry> readEvent(int eventIndex) {
        return getEventDescription(eventIndex).flatMap(eventDesc -> {
            ReadEventCommand readEventCmd = eventDesc.readEventCmd;
            int currentIndex = readEventCmd.getCurrentIndex();
            String eventText = eventDesc.getText();
            boolean upperZone = getBridgeHandler().getIntegraType() == IntegraType.I256_PLUS
                    && readEventCmd.getUserControlNumber() > 0;
            String eventDetails;

            switch (eventDesc.getKind()) {
                case 0:
                    eventDetails = "";
                    break;
                case 1:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getZoneExpanderKeypadDescription(readEventCmd.getSource(), upperZone);
                    break;
                case 2:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getUserDescription(readEventCmd.getSource());
                    break;
                case 3:
                    eventDetails = getDeviceDescription(DeviceType.EXPANDER, readEventCmd.getPartitionKeypad())
                            + DETAILS_SEPARATOR + getUserDescription(readEventCmd.getSource());
                    break;
                case 4:
                    eventDetails = getZoneExpanderKeypadDescription(readEventCmd.getSource(), upperZone);
                    break;
                case 5:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition());
                    break;
                case 6:
                    eventDetails = getDeviceDescription(DeviceType.KEYPAD, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getUserDescription(readEventCmd.getSource());
                    break;
                case 7:
                    eventDetails = getUserDescription(readEventCmd.getSource());
                    break;
                case 8:
                    eventDetails = getDeviceDescription(DeviceType.EXPANDER, readEventCmd.getSource());
                    break;
                case 9:
                    eventDetails = getTelephoneDescription(readEventCmd.getSource());
                    break;
                case 11:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getDataBusDescription(readEventCmd.getSource());
                    break;
                case 12:
                    if (readEventCmd.getSource() <= getBridgeHandler().getIntegraType().getOnMainboard()) {
                        eventDetails = getOutputExpanderDescription(readEventCmd.getSource(), upperZone);
                    } else {
                        eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                                + DETAILS_SEPARATOR + getOutputExpanderDescription(readEventCmd.getSource(), upperZone);
                    }
                    break;
                case 13:
                    if (readEventCmd.getSource() <= 128) {
                        eventDetails = getOutputExpanderDescription(readEventCmd.getSource(), upperZone);
                    } else {
                        eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                                + DETAILS_SEPARATOR + getOutputExpanderDescription(readEventCmd.getSource(), upperZone);
                    }
                    break;
                case 14:
                    eventDetails = getTelephoneDescription(readEventCmd.getPartition()) + DETAILS_SEPARATOR
                            + getUserDescription(readEventCmd.getSource());
                    break;
                case 15:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getDeviceDescription(DeviceType.TIMER, readEventCmd.getSource());
                    break;
                case 31:
                    // this description consists of two records, so we must read additional record from the log
                    eventDetails = "." + readEventCmd.getSource() + "."
                            + (readEventCmd.getObject() * 32 + readEventCmd.getUserControlNumber());
                    Optional<EventDescription> eventDescNext = getEventDescription(readEventCmd.getNextIndex());
                    if (!eventDescNext.isPresent()) {
                        return Optional.empty();
                    }
                    final EventDescription eventDescNextItem = eventDescNext.get();
                    if (eventDescNextItem.getKind() != 30) {
                        logger.info("Unexpected event record kind {} at index {}", eventDescNextItem.getKind(),
                                readEventCmd.getNextIndex());
                        return Optional.empty();
                    }
                    readEventCmd = eventDescNextItem.readEventCmd;
                    eventText = eventDescNextItem.getText();
                    eventDetails = getDeviceDescription(DeviceType.KEYPAD, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + "ip: " + readEventCmd.getSource() + "."
                            + (readEventCmd.getObject() * 32 + readEventCmd.getUserControlNumber()) + eventDetails;
                    break;
                case 32:
                    eventDetails = getDeviceDescription(DeviceType.PARTITION, readEventCmd.getPartition())
                            + DETAILS_SEPARATOR + getDeviceDescription(DeviceType.ZONE, readEventCmd.getSource());
                    break;
                default:
                    logger.info("Unsupported device kind code {} at index {}", eventDesc.getKind(),
                            readEventCmd.getCurrentIndex());
                    eventDetails = String.join(DETAILS_SEPARATOR, "kind=" + eventDesc.getKind(),
                            "partition=" + readEventCmd.getPartition(), "source=" + readEventCmd.getSource(),
                            "object=" + readEventCmd.getObject(), "ucn=" + readEventCmd.getUserControlNumber());
            }

            return Optional.of(new EventLogEntry(currentIndex, readEventCmd.getNextIndex(),
                    readEventCmd.getTimestamp().atZone(getBridgeHandler().getZoneId()), eventText, eventDetails));
        });
    }

    private Optional<EventDescription> getEventDescription(int eventIndex) {
        ReadEventCommand readEventCmd = new ReadEventCommand(eventIndex);
        if (!getBridgeHandler().sendCommand(readEventCmd, false)) {
            logger.info("Unable to read event record for given index: {}", eventIndex);
            return Optional.empty();
        } else if (readEventCmd.isEmpty()) {
            logger.info("No record under given index: {}", eventIndex);
            return Optional.empty();
        } else {
            return Optional.of(readEventDescription(readEventCmd));
        }
    }

    private static class EventDescriptionCacheEntry {
        private final String eventText;
        private final int descKind;

        EventDescriptionCacheEntry(String eventText, int descKind) {
            this.eventText = eventText;
            this.descKind = descKind;
        }

        String getText() {
            return eventText;
        }

        int getKind() {
            return descKind;
        }
    }

    private static class EventDescription extends EventDescriptionCacheEntry {
        private final ReadEventCommand readEventCmd;

        EventDescription(ReadEventCommand readEventCmd, String eventText, int descKind) {
            super(eventText, descKind);
            this.readEventCmd = readEventCmd;
        }
    }

    private EventDescription readEventDescription(ReadEventCommand readEventCmd) {
        int eventCode = readEventCmd.getEventCode();
        boolean restore = readEventCmd.isRestore();
        String mapKey = String.format("%d_%b", eventCode, restore);
        EventDescriptionCacheEntry mapValue = eventDescriptions.computeIfAbsent(mapKey, k -> {
            ReadEventDescCommand cmd = new ReadEventDescCommand(eventCode, restore, true);
            if (!getBridgeHandler().sendCommand(cmd, false)) {
                logger.info("Unable to read event description: {}, {}", eventCode, restore);
                return null;
            }
            return new EventDescriptionCacheEntry(cmd.getText(encoding), cmd.getKind());
        });
        if (mapValue == null) {
            return new EventDescription(readEventCmd, NOT_AVAILABLE_TEXT, 0);
        } else {
            return new EventDescription(readEventCmd, mapValue.getText(), mapValue.getKind());
        }
    }

    private String getOutputExpanderDescription(int deviceNumber, boolean upperOutput) {
        if (deviceNumber == 0) {
            return "mainboard";
        } else if (deviceNumber <= 128) {
            return getDeviceDescription(DeviceType.OUTPUT, upperOutput ? 128 + deviceNumber : deviceNumber);
        } else if (deviceNumber <= 192) {
            return getDeviceDescription(DeviceType.EXPANDER, deviceNumber);
        } else {
            return "invalid output|expander device: " + deviceNumber;
        }
    }

    private String getZoneExpanderKeypadDescription(int deviceNumber, boolean upperZone) {
        if (deviceNumber == 0) {
            return "mainboard";
        } else if (deviceNumber <= 128) {
            return getDeviceDescription(DeviceType.ZONE, upperZone ? 128 + deviceNumber : deviceNumber);
        } else if (deviceNumber <= 192) {
            return getDeviceDescription(DeviceType.EXPANDER, deviceNumber);
        } else {
            return getDeviceDescription(DeviceType.KEYPAD, deviceNumber);
        }
    }

    private String getUserDescription(int deviceNumber) {
        return deviceNumber == 0 ? "user: unknown" : getDeviceDescription(DeviceType.USER, deviceNumber);
    }

    private String getDataBusDescription(int deviceNumber) {
        return "data bus: " + deviceNumber;
    }

    private String getTelephoneDescription(int deviceNumber) {
        return deviceNumber == 0 ? "telephone: unknown" : getDeviceDescription(DeviceType.TELEPHONE, deviceNumber);
    }

    private String getDeviceDescription(DeviceType deviceType, int deviceNumber) {
        return String.format("%s: %s", deviceType.name().toLowerCase(), readDeviceName(deviceType, deviceNumber));
    }

    private String readDeviceName(DeviceType deviceType, int deviceNumber) {
        String cacheKey = String.format("%s_%d", deviceType, deviceNumber);
        String result = deviceNameCache.computeIfAbsent(cacheKey, k -> {
            ReadDeviceInfoCommand cmd = new ReadDeviceInfoCommand(deviceType, deviceNumber);
            if (!getBridgeHandler().sendCommand(cmd, false)) {
                logger.info("Unable to read device info: {}, {}", deviceType, deviceNumber);
                return null;
            }
            return cmd.getName(encoding);
        });
        return result == null ? NOT_AVAILABLE_TEXT : result;
    }
}
