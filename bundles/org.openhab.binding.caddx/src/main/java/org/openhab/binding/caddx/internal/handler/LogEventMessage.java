/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caddx.internal.CaddxMessage;

/**
 * Used to parse panel log event messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class LogEventMessage {

    public final String number;
    public final String size;
    public final String type;
    public final String zud;
    public final String partition;
    public final String month;
    public final String day;
    public final String hour;
    public final String minute;

    LogEventMessage(CaddxMessage message) {
        this.number = message.getPropertyById("panel_log_event_number");
        this.size = message.getPropertyById("panel_log_event_size");
        this.type = message.getPropertyById("panel_log_event_type");
        this.zud = message.getPropertyById("panel_log_event_zud");
        this.partition = message.getPropertyById("panel_log_event_partition");
        this.month = message.getPropertyById("panel_log_event_month");
        this.day = message.getPropertyById("panel_log_event_day");
        this.hour = message.getPropertyById("panel_log_event_hour");
        this.minute = message.getPropertyById("panel_log_event_minute");
    }

    private final String alarmList[] = { "Alarm", "Alarm restore", "Bypass", "Bypass restore", "Tamper",
            "Tamper restore", "Trouble", "Trouble restore", "TX low battery", "TX low battery restore", "Zone lost",
            "Zone lost restore", "Start of cross time", "Not used", "Not used", "Not used", "Not used",
            "Special expansion event", "Duress", "Manual fire", "Auxiliary 2 panic", "Not used", "Panic",
            "Keypad tamper", "Control box tamper", "Control box tamper restore", "AC fail", "AC fail restore",
            "Low battery", "Low battery restore", "Over-current", "Over-current restore", "Siren tamper",
            "Siren tamper restore", "Telephone fault", "Telephone fault restore", "Expander trouble",
            "Expander trouble restore", "Fail to communicate", "Log full", "Opening", "Closing", "Exit error",
            "Recent closing", "Auto-test None", "Start program", "End program", "Start download", "End download",
            "Cancel", "Ground fault", "Ground fault restore", "Manual test", "Closed with zones bypassed",
            "Start of listen in", "Technician on site", "Technician left", "Control power up", "58-", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "Not used", "First to open", "Last to close", "PIN entered with bit 7 set", "Begin walk-test",
            "End walk-test", "Re-exit", "Output trip", "Data lost" };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Date
        sb.append(String.format("%02d", Integer.parseInt(day))).append('-')
                .append(String.format("%02d", Integer.parseInt(month))).append(' ')
                .append(String.format("%02d", Integer.parseInt(hour))).append(':')
                .append(String.format("%02d", Integer.parseInt(minute))).append(' ');

        int eventType = Integer.parseInt(type);
        if (eventType >= 0 && eventType <= 12) { // Zone, Yes
            sb.append(alarmList[eventType]).append(" Partition ").append(Integer.parseInt(partition) + 1)
                    .append(" Zone ").append(Integer.parseInt(zud) + 1);
        } else if ((eventType >= 17 && eventType <= 17) || (eventType >= 34 && eventType <= 35)
                || (eventType >= 38 && eventType <= 39) || (eventType >= 44 && eventType <= 48)
                || (eventType >= 50 && eventType <= 52) || (eventType >= 54 && eventType <= 57)
                || (eventType >= 123 && eventType <= 124) || (eventType >= 127 && eventType <= 127)) { // None, No
            sb.append(alarmList[eventType]);
        } else if ((eventType >= 18 && eventType <= 20) || (eventType >= 22 && eventType <= 23)
                || (eventType >= 125 && eventType <= 125)) { // None, Yes
            sb.append(alarmList[eventType]).append(" Partition ").append(Integer.parseInt(partition) + 1);
        } else if ((eventType >= 24 && eventType <= 33) || (eventType >= 36 && eventType <= 37)) { // Device, No
            sb.append(alarmList[eventType]).append(" Device ").append(zud);
        } else if ((eventType >= 40 && eventType <= 43) || (eventType >= 49 && eventType <= 49)
                || (eventType >= 53 && eventType <= 53) || (eventType >= 120 && eventType <= 122)) { // User, Yes
            sb.append(alarmList[eventType]).append(" Partition ").append(Integer.parseInt(partition) + 1)
                    .append(" User ").append(Integer.parseInt(zud) + 1);
        } else if ((eventType >= 126 && eventType <= 126)) { // User, No
            sb.append(alarmList[eventType]).append(" User ").append(Integer.parseInt(zud) + 1);
        }

        sb.append("");

        return sb.toString();
    }
}
