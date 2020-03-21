/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int eventType = Integer.parseInt(type);
        LogEventType logEventType = LogEventType.valueOfLogEventType(eventType);

        // Date
        sb.append(String.format("%02d", Integer.parseInt(day))).append('-')
                .append(String.format("%02d", Integer.parseInt(month))).append(' ')
                .append(String.format("%02d", Integer.parseInt(hour))).append(':')
                .append(String.format("%02d", Integer.parseInt(minute))).append(' ');

        sb.append(logEventType.description);
        if (logEventType.isPartitionValid) {
            sb.append(" Partition ").append(Integer.parseInt(partition) + 1);
        }

        switch (logEventType.zud) {
            case None:
                break;
            case Zone:
                sb.append(" Zone ").append(Integer.parseInt(zud) + 1);
                break;
            case User:
                sb.append(" User ").append(Integer.parseInt(zud) + 1);
                break;
            case Device:
                sb.append(" Device ").append(zud);
                break;
        }

        return sb.toString();
    }
}
