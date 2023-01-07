/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.command;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that reads one record from the event log.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ReadEventCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final byte COMMAND_CODE = (byte) 0x8c;

    /**
     * Event class: zone alarms, partition alarms, arming, troubles, etc.
     *
     * @author Krzysztof Goworek - Initial contribution
     *
     */
    public enum EventClass {
        ZONE_ALARMS("zone and tamper alarms"),
        PARTITION_ALARMS("partition and expander alarms"),
        ARMING("arming, disarming, alarm clearing"),
        BYPASSES("zone bypasses and unbypasses"),
        ACCESS_CONTROL("access control"),
        TROUBLES("troubles"),
        USER_FUNCTIONS("user functions"),
        SYSTEM_EVENTS("system events");

        private String description;

        EventClass(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Creates new command class instance to read a record under given index.
     *
     * @param eventIndex index of event record to retrieve, -1 for the most recent one
     */
    public ReadEventCommand(int eventIndex) {
        super(COMMAND_CODE, getIndexBytes(eventIndex));
    }

    private static byte[] getIndexBytes(int index) {
        return new byte[] { (byte) ((index >> 16) & 0xff), (byte) ((index >> 8) & 0xff), (byte) (index & 0xff) };
    }

    /**
     * Checks whether response data contains valid event record.
     *
     * @return <code>true</code> if returned record is empty (likely the last
     *         record in the log)
     */
    public boolean isEmpty() {
        return (getResponse().getPayload()[0] & 0x20) == 0;
    }

    /**
     * Checks whether event record is present in the response data.
     *
     * @return <code>true</code> if event data is present in the response
     */
    public boolean isEventPresent() {
        return (getResponse().getPayload()[0] & 0x10) != 0;
    }

    /**
     * Returns date and time of the event.
     *
     * @return date and time of the event
     */
    public LocalDateTime getTimestamp() {
        final byte[] payload = getResponse().getPayload();
        final int currentYear = LocalDateTime.now().getYear();
        final int yearBase = currentYear / 4;
        final int yearMarker = (payload[0] >> 6) & 0x03;
        int year = 4 * yearBase + yearMarker;
        final int minutes = ((payload[2] & 0x0f) << 8) + (payload[3] & 0xff);

        if (year > currentYear) {
            year -= 4;
        }
        LocalDateTime result = LocalDateTime.of(year, (payload[2] >> 4) & 0x0f, payload[1] & 0x1f, minutes / 60,
                minutes % 60);
        return result;
    }

    /**
     * Returns class of the event.
     *
     * @return event class of the event
     * @see EventClass
     */
    public EventClass getEventClass() {
        final int eventClassIdx = (getResponse().getPayload()[1] >> 5) & 0x07;
        return EventClass.values()[eventClassIdx];
    }

    /**
     * Returns number of partion the event is about.
     *
     * @return partition number
     */
    public int getPartition() {
        return ((getResponse().getPayload()[4] >> 3) & 0x1f) + 1;
    }

    /**
     * Returns number of partition keypad related to the event.
     *
     * @return partition keypad number
     */
    public int getPartitionKeypad() {
        return ((getResponse().getPayload()[4] >> 2) & 0x3f) + 1;
    }

    /**
     * Returns event code the describes the event. It can be used to retrieve description text for this event.
     *
     * @return event code
     * @see ReadEventDescCommand
     */
    public int getEventCode() {
        final byte[] payload = getResponse().getPayload();
        return ((payload[4] & 0x03) << 8) + (payload[5] & 0xff);
    }

    /**
     * Returns state restoration flag.
     *
     * @return <code>true</code> if this is restoration of some state (i.e.
     *         arming and disarming have the same code but different restoration
     *         flag)
     */
    public boolean isRestore() {
        return (getResponse().getPayload()[4] & 0x04) != 0;
    }

    /**
     * Return source of the event.
     *
     * @return event source (zone number, user number, etc depending on event)
     */
    public int getSource() {
        return getResponse().getPayload()[6] & 0xff;
    }

    /**
     * Returns object number for the event.
     *
     * @return object number (0..7)
     */
    public int getObject() {
        return (getResponse().getPayload()[7] >> 5) & 0x07;
    }

    /**
     * Returns user control number for the event.
     *
     * @return user control number
     */
    public int getUserControlNumber() {
        return getResponse().getPayload()[7] & 0x1f;
    }

    /**
     * Return index of previous event in the log. Can be used to iterate over tha event log.
     *
     * @return index of previous event record in the log
     */
    public int getNextIndex() {
        final byte[] payload = getResponse().getPayload();
        return (payload[8] << 16) + ((payload[9] & 0xff) << 8) + (payload[10] & 0xff);
    }

    /**
     * Returns current event index.
     *
     * @return index of current record echoed by communication module
     */
    public int getCurrentIndex() {
        final byte[] payload = getResponse().getPayload();
        return (payload[11] << 16) + ((payload[12] & 0xff) << 8) + (payload[13] & 0xff);
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getPayload().length != 14) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }
}
