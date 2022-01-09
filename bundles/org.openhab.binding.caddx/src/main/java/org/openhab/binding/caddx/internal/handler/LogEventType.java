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
package org.openhab.binding.caddx.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * All the log event types
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public enum LogEventType {
    ALARM(0, ZoneUserDevice.ZONE, true, "Alarm"),
    ALARM_RESTORE(1, ZoneUserDevice.ZONE, true, "Alarm restore"),
    BYPASS(2, ZoneUserDevice.ZONE, true, "Bypass"),
    BYPASS_RESTORE(3, ZoneUserDevice.ZONE, true, "Bypass restore"),
    TAMPER(4, ZoneUserDevice.ZONE, true, "Tamper"),
    TAMPER_RESTORE(5, ZoneUserDevice.ZONE, true, "Tamper restore"),
    TROUBLE(6, ZoneUserDevice.ZONE, true, "Trouble"),
    TROUBLE_RESTORE(7, ZoneUserDevice.ZONE, true, "Trouble restore"),
    TX_LOW_BATTERY(8, ZoneUserDevice.ZONE, true, "TX low battery"),
    TX_LOW_BATTERY_RESTORE(9, ZoneUserDevice.ZONE, true, "TX low battery restore"),
    ZONE_LOST(10, ZoneUserDevice.ZONE, true, "Zone lost"),
    ZONE_LOST_RESTORE(11, ZoneUserDevice.ZONE, true, "Zone lost restore"),
    START_OF_CROSS_TIME(12, ZoneUserDevice.ZONE, true, "Start of cross time"),
    SPECIAL_EXPANSION_EVENT(17, ZoneUserDevice.NONE, false, "Special expansion event"),
    DURESS(18, ZoneUserDevice.NONE, true, "Duress"),
    MANUAL_FIRE(19, ZoneUserDevice.NONE, true, "Manual fire"),
    AUXILIARY2_PANIC(20, ZoneUserDevice.NONE, true, "Auxiliary 2 panic"),
    PANIC(22, ZoneUserDevice.NONE, true, "Panic"),
    KEYPAD_TAMPER(23, ZoneUserDevice.NONE, true, "Keypad tamper"),
    CONTROL_BOX_TAMPER(24, ZoneUserDevice.DEVICE, false, "Control box tamper"),
    CONTROL_BOX_TAMPER_RESTORE(25, ZoneUserDevice.DEVICE, false, "Control box tamper restore"),
    AC_FAIL(26, ZoneUserDevice.DEVICE, false, "AC fail"),
    AC_FAIL_RESTORE(27, ZoneUserDevice.DEVICE, false, "AC fail restore"),
    LOW_BATTERY(28, ZoneUserDevice.DEVICE, false, "Low battery"),
    LOW_BATTERY_RESTORE(29, ZoneUserDevice.DEVICE, false, "Low battery restore"),
    OVER_CURRENT(30, ZoneUserDevice.DEVICE, false, "Over-current"),
    OVER_CURRENT_RESTORE(31, ZoneUserDevice.DEVICE, false, "Over-current restore"),
    SIREN_TAMPER(32, ZoneUserDevice.DEVICE, false, "Siren tamper"),
    SIREN_TAMPER_RESTORE(33, ZoneUserDevice.DEVICE, false, "Siren tamper restore"),
    TELEPHONE_FAULT(34, ZoneUserDevice.NONE, false, "Telephone fault"),
    TELEPHONE_FAULT_RESTORE(35, ZoneUserDevice.NONE, false, "Telephone fault restore"),
    EXPANDER_TROUBLE(36, ZoneUserDevice.DEVICE, false, "Expander trouble"),
    EXPANDER_TROUBLE_RESTORE(37, ZoneUserDevice.DEVICE, false, "Expander trouble restore"),
    FAIL_TO_COMMUNICATE(38, ZoneUserDevice.NONE, false, "Fail to communicate"),
    LOG_FULL(39, ZoneUserDevice.NONE, false, "Log full"),
    OPENING(40, ZoneUserDevice.USER, true, "Opening"),
    CLOSING(41, ZoneUserDevice.USER, true, "Closing"),
    EXIT_ERROR(42, ZoneUserDevice.USER, true, "Exit error"),
    RECENT_CLOSING(43, ZoneUserDevice.USER, true, "Recent closing"),
    AUTO_TEST(44, ZoneUserDevice.NONE, false, "Auto-test"),
    START_PROGRAM(45, ZoneUserDevice.NONE, false, "Start program"),
    END_PROGRAM(46, ZoneUserDevice.NONE, false, "End program"),
    START_DOWNLOAD(47, ZoneUserDevice.NONE, false, "Start download"),
    END_DOWNLOAD(48, ZoneUserDevice.NONE, false, "End download"),
    CANCEL(49, ZoneUserDevice.USER, true, "Cancel"),
    GROUND_FAULT(50, ZoneUserDevice.NONE, false, "Ground fault"),
    GROUND_FAULT_RESTORE(51, ZoneUserDevice.NONE, false, "Ground fault restore"),
    MANUAL_TEST(52, ZoneUserDevice.NONE, false, "Manual test"),
    CLOSED_WITH_ZONES_BYPASSED(53, ZoneUserDevice.USER, true, "Closed with zones bypassed"),
    START_OF_LISTEN_IN(54, ZoneUserDevice.NONE, false, "Start of listen in"),
    TECHNICIAN_ON_SITE(55, ZoneUserDevice.NONE, false, "Technician on site"),
    TECHNICIAN_LEFT(56, ZoneUserDevice.NONE, false, "Technician left"),
    CONTROL_POWER_UP(57, ZoneUserDevice.NONE, false, "Control power up"),
    FIRST_TO_OPEN(120, ZoneUserDevice.USER, true, "First to open"),
    LAST_TO_CLOSE(121, ZoneUserDevice.USER, true, "Last toC close"),
    PIN_ENTERED_WITH_BIT7_SET(122, ZoneUserDevice.USER, true, "PIN entered with bit 7 set"),
    BEGIN_WALK_TEST(123, ZoneUserDevice.NONE, false, "Begin walk-test"),
    END_WALK_TEST(124, ZoneUserDevice.NONE, false, "End walk-test"),
    RE_EXIT(125, ZoneUserDevice.NONE, true, "Re-exit"),
    OUTPUT_TRIP(126, ZoneUserDevice.USER, false, "Output trip"),
    DATA_LOST(127, ZoneUserDevice.NONE, false, "Data Lost");

    private static final Map<Integer, LogEventType> BY_LOG_EVENT_TYPE = new HashMap<>();
    public final int eventType;
    public final ZoneUserDevice zud;
    public final boolean isPartitionValid;
    public final String description;

    LogEventType(int eventType, ZoneUserDevice zud, boolean isPartitionValid, String description) {
        this.eventType = eventType;
        this.zud = zud;
        this.isPartitionValid = isPartitionValid;
        this.description = description;
    }

    static {
        for (LogEventType logEventType : values()) {
            BY_LOG_EVENT_TYPE.put(logEventType.eventType, logEventType);
        }
    }

    public static @Nullable LogEventType valueOfLogEventType(int eventType) {
        return BY_LOG_EVENT_TYPE.get(eventType);
    }
}
