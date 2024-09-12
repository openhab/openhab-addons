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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digiplex.internal.communication.events.AreaEvent;
import org.openhab.binding.digiplex.internal.communication.events.AreaEventType;
import org.openhab.binding.digiplex.internal.communication.events.GenericEvent;
import org.openhab.binding.digiplex.internal.communication.events.SpecialAlarmEvent;
import org.openhab.binding.digiplex.internal.communication.events.SpecialAlarmType;
import org.openhab.binding.digiplex.internal.communication.events.TroubleEvent;
import org.openhab.binding.digiplex.internal.communication.events.TroubleStatus;
import org.openhab.binding.digiplex.internal.communication.events.TroubleType;
import org.openhab.binding.digiplex.internal.communication.events.ZoneEvent;
import org.openhab.binding.digiplex.internal.communication.events.ZoneEventType;
import org.openhab.binding.digiplex.internal.communication.events.ZoneStatusEvent;

/**
 * Resolves serial messages to appropriate classes
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class DigiplexResponseResolver {

    private static final String OK = "&ok";
    // TODO: handle failures
    private static final String FAIL = "&fail";

    public static DigiplexResponse resolveResponse(String message) {
        if (message.length() < 4) { // sanity check: try to filter out malformed responses
            return new UnknownResponse(message);
        }

        int zoneNo, areaNo;
        String commandType = message.substring(0, 2);
        switch (commandType) {
            case "CO": // communication status
                if (message.contains(FAIL)) {
                    return CommunicationStatus.FAILURE;
                } else {
                    return CommunicationStatus.OK;
                }
            case "ZL": // zone label
                zoneNo = Integer.valueOf(message.substring(2, 5));
                if (message.contains(FAIL)) {
                    return ZoneLabelResponse.failure(zoneNo);
                } else {
                    return ZoneLabelResponse.success(zoneNo, message.substring(5).trim());
                }
            case "AL": // area label
                areaNo = Integer.valueOf(message.substring(2, 5));
                if (message.contains(FAIL)) {
                    return AreaLabelResponse.failure(areaNo);
                } else {
                    return AreaLabelResponse.success(areaNo, message.substring(5).trim());
                }
            case "RZ": // zone status
                zoneNo = Integer.valueOf(message.substring(2, 5));
                if (message.contains(FAIL)) {
                    return ZoneStatusResponse.failure(zoneNo);
                } else {
                    return ZoneStatusResponse.success(zoneNo, // zone number
                            ZoneStatus.fromMessage(message.charAt(5)), // status
                            toBoolean(message.charAt(6)), // alarm
                            toBoolean(message.charAt(7)), // fire alarm
                            toBoolean(message.charAt(8)), // supervision lost
                            toBoolean(message.charAt(9))); // battery low
                }
            case "RA": // area status
                areaNo = Integer.valueOf(message.substring(2, 5));
                if (message.contains(FAIL)) {
                    return AreaStatusResponse.failure(areaNo);
                } else {
                    return AreaStatusResponse.success(areaNo, // zone number
                            AreaStatus.fromMessage(message.charAt(5)), // status
                            toBoolean(message.charAt(6)), // zone in memory
                            toBoolean(message.charAt(7)), // trouble
                            !toBoolean(message.charAt(8)), // ready (note ! in front)
                            toBoolean(message.charAt(9)), // in programming
                            toBoolean(message.charAt(10)), // in alarm
                            toBoolean(message.charAt(11))); // strobe
                }
            case "AA": // area arm
            case "AQ": // area quick arm
            case "AD": // area disarm
                areaNo = Integer.valueOf(message.substring(2, 5));
                if (message.contains(FAIL)) {
                    return AreaArmDisarmResponse.failure(areaNo, ArmDisarmType.fromMessage(commandType));
                } else {
                    return AreaArmDisarmResponse.success(areaNo, ArmDisarmType.fromMessage(commandType));
                }
            case "UL": // user label
            case "PG": // PGM events
            default:
                if (message.startsWith("G")) {
                    return resolveSystemEvent(message);
                } else {
                    return new UnknownResponse(message);
                }
        }
    }

    private static boolean toBoolean(char value) {
        return value != 'O';
    }

    private static DigiplexResponse resolveSystemEvent(String message) {
        int eventGroup = Integer.parseInt(message.substring(1, 4));
        int eventNumber = Integer.parseInt(message.substring(5, 8));
        int areaNumber = Integer.parseInt(message.substring(9, 12));
        switch (eventGroup) {
            case 0:
                return new ZoneStatusEvent(eventNumber, ZoneStatus.CLOSED, areaNumber);
            case 1:
                return new ZoneStatusEvent(eventNumber, ZoneStatus.OPEN, areaNumber);
            case 2:
                return new ZoneStatusEvent(eventNumber, ZoneStatus.TAMPERED, areaNumber);
            case 3:
                return new ZoneStatusEvent(eventNumber, ZoneStatus.FIRE_LOOP_TROUBLE, areaNumber);
            case 8:
                return new ZoneEvent(eventNumber, ZoneEventType.TX_DELAY_ZONE_ALARM, areaNumber);
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                return new AreaEvent(AreaEventType.DISARMED, areaNumber);
            case 23:
                return new ZoneEvent(eventNumber, ZoneEventType.BYPASSED, areaNumber);
            case 24:
                return new ZoneEvent(eventNumber, ZoneEventType.ALARM, areaNumber);
            case 25:
                return new ZoneEvent(eventNumber, ZoneEventType.FIRE_ALARM, areaNumber);
            case 26:
                return new ZoneEvent(eventNumber, ZoneEventType.ALARM_RESTORE, areaNumber);
            case 27:
                return new ZoneEvent(eventNumber, ZoneEventType.FIRE_ALARM_RESTORE, areaNumber);
            case 30:
                return new SpecialAlarmEvent(areaNumber, SpecialAlarmType.fromMessage(eventNumber));
            case 32:
                return new ZoneEvent(eventNumber, ZoneEventType.SHUTDOWN, areaNumber);
            case 33:
                return new ZoneEvent(eventNumber, ZoneEventType.TAMPER, areaNumber);
            case 34:
                return new ZoneEvent(eventNumber, ZoneEventType.TAMPER_RESTORE, areaNumber);
            case 36:
                return new TroubleEvent(TroubleType.fromEventNumber(eventNumber), TroubleStatus.TROUBLE_STARTED,
                        areaNumber);
            case 37:
                return new TroubleEvent(TroubleType.fromEventNumber(eventNumber), TroubleStatus.TROUBLE_RESTORED,
                        areaNumber);
            case 41:
                return new ZoneEvent(eventNumber, ZoneEventType.LOW_BATTERY, areaNumber);
            case 42:
                return new ZoneEvent(eventNumber, ZoneEventType.SUPERVISION_TROUBLE, areaNumber);
            case 43:
                return new ZoneEvent(eventNumber, ZoneEventType.LOW_BATTERY_RESTORE, areaNumber);
            case 44:
                return new ZoneEvent(eventNumber, ZoneEventType.SUPERVISION_TROUBLE_RESTORE, areaNumber);
            case 55:
                return new ZoneEvent(eventNumber, ZoneEventType.INTELLIZONE_TRIGGERED, areaNumber);
            case 64:
                switch (eventNumber) {
                    case 0:
                        return new AreaEvent(AreaEventType.ARMED, areaNumber);
                    case 1:
                        return new AreaEvent(AreaEventType.ARMED_FORCE, areaNumber);
                    case 2:
                        return new AreaEvent(AreaEventType.ARMED_STAY, areaNumber);
                    case 3:
                        return new AreaEvent(AreaEventType.ARMED_INSTANT, areaNumber);
                    case 4:
                        return new AreaEvent(AreaEventType.ALARM_STROBE, areaNumber);
                    case 5:
                        return new AreaEvent(AreaEventType.ALARM_SILENT, areaNumber);
                    case 6:
                        return new AreaEvent(AreaEventType.ALARM_AUDIBLE, areaNumber);
                    case 7:
                        return new AreaEvent(AreaEventType.ALARM_FIRE, areaNumber);
                }
                break;
            case 65:
                switch (eventNumber) {
                    case 0:
                        return new AreaEvent(AreaEventType.READY, areaNumber);
                    case 1:
                        return new AreaEvent(AreaEventType.EXIT_DELAY, areaNumber);
                    case 2:
                        return new AreaEvent(AreaEventType.ENTRY_DELAY, areaNumber);
                    case 3:
                        return new AreaEvent(AreaEventType.SYSTEM_IN_TROUBLE, areaNumber);
                    case 4:
                        return new AreaEvent(AreaEventType.ALARM_IN_MEMORY, areaNumber);
                    case 5:
                        return new AreaEvent(AreaEventType.ZONES_BYPASSED, areaNumber);
                }
        }
        return new GenericEvent(eventGroup, eventNumber, areaNumber);
    }
}
