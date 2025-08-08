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
package org.openhab.binding.satel.internal.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.command.ReadEventCommand;
import org.openhab.binding.satel.internal.command.ReadEventDescCommand;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class for reading event log records.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class EventLogReader {

    private static final String DETAILS_SEPARATOR = ", ";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, @Nullable EventDescriptionCacheEntry> eventDescriptions = new ConcurrentHashMap<>();
    private final SatelBridgeHandler bridgeHandler;
    private final DeviceNameResolver deviceNameResolver;

    public EventLogReader(SatelBridgeHandler bridgeHandler, DeviceNameResolver deviceNameResolver) {
        this.bridgeHandler = bridgeHandler;
        this.deviceNameResolver = deviceNameResolver;
    }

    /**
     * Reads one record from the event log.
     *
     * @param eventIndex record index
     * @return record description wrapped in {@linkplain Optional} or {@linkplain Optional#empty()} if there is no
     *         record under given index or read command failed
     */
    public Optional<EventDescription> readEvent(int eventIndex) {
        ReadEventCommand readEventCmd = new ReadEventCommand(eventIndex);
        if (!bridgeHandler.sendCommand(readEventCmd, false)) {
            logger.warn("Unable to read event record for given index: {}", eventIndex);
            return Optional.empty();
        } else if (readEventCmd.isEmpty()) {
            logger.warn("No record under given index: {}", eventIndex);
            return Optional.empty();
        } else {
            return Optional.of(readEventDescription(readEventCmd));
        }
    }

    /**
     * Builds detailed text description for given event record.
     *
     * @param eventDescription event record
     * @return string with event details
     */
    public String buildDetails(EventDescription eventDescription) {
        String eventDetails = getDetails(eventDescription);
        if (eventDescription.isMultipartEvent()) {
            // this description consists of two records, so we must read additional record from the log
            eventDetails = readSecondPartOfDetails(eventDescription).orElse("") + eventDetails;
        }
        return eventDetails;
    }

    /**
     * Removes all device names from the cache.
     */
    public void clearCache() {
        deviceNameResolver.clearCache();
    }

    private static class EventDescriptionCacheEntry {
        private final String eventText;
        private final int descKind;

        private EventDescriptionCacheEntry(String eventText, int descKind) {
            this.eventText = eventText;
            this.descKind = descKind;
        }

        public String getText() {
            return eventText;
        }

        int getKind() {
            return descKind;
        }
    }

    /**
     * Contains decoded data of an event record.
     */
    public class EventDescription extends EventDescriptionCacheEntry {
        private final int currentIndex;
        private int nextIndex;
        private final int userControlNumber;
        private final int partition;
        private final int partitionKeypad;
        private final int source;
        private final int object;
        private final LocalDateTime timestamp;

        EventDescription(ReadEventCommand readEventCmd, String eventText, int descKind) {
            super(eventText, descKind);
            this.currentIndex = readEventCmd.getCurrentIndex();
            this.nextIndex = readEventCmd.getNextIndex();
            this.userControlNumber = readEventCmd.getUserControlNumber();
            this.partition = readEventCmd.getPartition();
            this.partitionKeypad = readEventCmd.getPartitionKeypad();
            this.source = readEventCmd.getSource();
            this.object = readEventCmd.getObject();
            this.timestamp = readEventCmd.getTimestamp();
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public int getNextIndex() {
            return nextIndex;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        private void setNextIndex(int nextIndex) {
            this.nextIndex = nextIndex;
        }

        private int getUserControlNumber() {
            return userControlNumber;
        }

        private int getPartition() {
            return partition;
        }

        private int getPartitionKeypad() {
            return partitionKeypad;
        }

        private int getSource() {
            return source;
        }

        private int getObject() {
            return object;
        }

        private boolean isMultipartEvent() {
            return getKind() == 31;
        }

        private boolean isSecondPart() {
            if (getKind() != 30) {
                logger.warn("Unexpected event record kind {} at index {}", getKind(), getCurrentIndex());
                return false;
            }
            return true;
        }
    }

    private EventDescription readEventDescription(ReadEventCommand readEventCmd) {
        int eventCode = readEventCmd.getEventCode();
        boolean restore = readEventCmd.isRestore();
        String mapKey = String.format("%d_%b", eventCode, restore);
        EventDescriptionCacheEntry mapValue = eventDescriptions.computeIfAbsent(mapKey, k -> {
            ReadEventDescCommand cmd = new ReadEventDescCommand(eventCode, restore, true);
            if (!bridgeHandler.sendCommand(cmd, false)) {
                logger.warn("Unable to read event description: {}, {}", eventCode, restore);
                return null;
            }
            return new EventDescriptionCacheEntry(cmd.getText(bridgeHandler.getEncoding()), cmd.getKind());
        });
        if (mapValue == null) {
            String eventText = String.format("event #%d%s", eventCode, restore ? " (restore)" : "");
            return new EventDescription(readEventCmd, eventText, 0);
        } else {
            return new EventDescription(readEventCmd, mapValue.getText(), mapValue.getKind());
        }
    }

    private String getDetails(EventDescription eventDesc) {
        boolean upperZone = bridgeHandler.getIntegraType() == IntegraType.I256_PLUS
                && eventDesc.getUserControlNumber() > 0;

        return switch (eventDesc.getKind()) {
            case 0 -> "";
            case 1 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveZoneExpanderKeypad(eventDesc.getSource(), upperZone);
            case 2 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveUser(eventDesc.getSource());
            case 3 -> deviceNameResolver.resolvePartitionKeypad(eventDesc.getPartitionKeypad()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveUser(eventDesc.getSource());
            case 4 -> deviceNameResolver.resolveZoneExpanderKeypad(eventDesc.getSource(), upperZone);
            case 5 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition());
            case 6 -> deviceNameResolver.resolve(DeviceType.KEYPAD, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveUser(eventDesc.getSource());
            case 7 -> deviceNameResolver.resolveUser(eventDesc.getSource());
            case 8 -> deviceNameResolver.resolve(DeviceType.EXPANDER, eventDesc.getSource());
            case 9 -> deviceNameResolver.resolveTelephone(eventDesc.getSource());
            case 10 -> deviceNameResolver.resolveTelephoneRelay(eventDesc.getSource());
            case 11 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveDataBus(eventDesc.getSource());
            case 12 -> (eventDesc.getSource() <= bridgeHandler.getIntegraType().getOnMainboard())
                    ? deviceNameResolver.resolveOutputExpander(eventDesc.getSource(), upperZone)
                    : deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                            + deviceNameResolver.resolveOutputExpander(eventDesc.getSource(), upperZone);
            case 13 -> (eventDesc.getSource() <= 128)
                    ? deviceNameResolver.resolveOutputExpander(eventDesc.getSource(), upperZone)
                    : deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                            + deviceNameResolver.resolveOutputExpander(eventDesc.getSource(), upperZone);
            case 14 -> deviceNameResolver.resolveTelephone(eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolveUser(eventDesc.getSource());
            case 15 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolve(DeviceType.TIMER, eventDesc.getSource());
            case 30 ->
                deviceNameResolver.resolve(DeviceType.KEYPAD, eventDesc.getPartition()) + DETAILS_SEPARATOR + "ip: "
                        + eventDesc.getSource() + "." + (eventDesc.getObject() * 32 + eventDesc.getUserControlNumber());
            case 31 ->
                "." + eventDesc.getSource() + "." + (eventDesc.getObject() * 32 + eventDesc.getUserControlNumber());
            case 32 -> deviceNameResolver.resolve(DeviceType.PARTITION, eventDesc.getPartition()) + DETAILS_SEPARATOR
                    + deviceNameResolver.resolve(DeviceType.ZONE, eventDesc.getSource());
            default -> {
                logger.warn("Unsupported device kind code {} at index {}", eventDesc.getKind(),
                        eventDesc.getCurrentIndex());
                yield String.join(DETAILS_SEPARATOR, "kind=" + eventDesc.getKind(),
                        "partition=" + eventDesc.getPartition(), "source=" + eventDesc.getSource(),
                        "object=" + eventDesc.getObject(), "ucn=" + eventDesc.getUserControlNumber());
            }
        };
    }

    private Optional<String> readSecondPartOfDetails(EventDescription eventDesc) {
        return readEvent(eventDesc.getNextIndex()).filter(EventDescription::isSecondPart).map(descNext -> {
            eventDesc.setNextIndex(descNext.getNextIndex());
            return getDetails(descNext);
        });
    }
}
