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
package org.openhab.binding.caddx.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to parse panel log event messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class LogEventMessage {
    private final Logger logger = LoggerFactory.getLogger(LogEventMessage.class);

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
        try {
            StringBuilder sb = new StringBuilder();

            int eventType = Integer.parseInt(type);
            logger.trace("eventType received: {}", eventType);
            LogEventType logEventType = LogEventType.valueOfLogEventType(eventType);

            // Date
            sb.append(String.format("%02d", Integer.parseInt(day))).append('-')
                    .append(String.format("%02d", Integer.parseInt(month))).append(' ')
                    .append(String.format("%02d", Integer.parseInt(hour))).append(':')
                    .append(String.format("%02d", Integer.parseInt(minute))).append(' ');

            if (logEventType == null) {
                sb.append("Unknown log event type");
            } else {
                sb.append(logEventType.description);
                if (logEventType.isPartitionValid) {
                    sb.append(" Partition ").append(Integer.parseInt(partition) + 1);
                }

                switch (logEventType.zud) {
                    case NONE:
                        break;
                    case ZONE:
                        sb.append(" Zone ").append(Integer.parseInt(zud) + 1);
                        break;
                    case USER:
                        sb.append(" User ").append(Integer.parseInt(zud) + 1);
                        break;
                    case DEVICE:
                        sb.append(" Device ").append(zud);
                        break;
                }
            }

            return sb.toString();
        } catch (NumberFormatException e) {
            logger.debug("LogEventMessage error. {}", e.getMessage(), e);
            return "logmessage cannot be constructed";
        }
    }
}
