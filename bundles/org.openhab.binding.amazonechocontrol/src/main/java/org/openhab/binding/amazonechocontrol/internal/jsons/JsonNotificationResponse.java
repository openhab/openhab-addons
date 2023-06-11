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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonNotificationResponse} encapsulate the GSON data for the result of a notification request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonNotificationResponse {
    // This is only a partial definition, see the example JSON below
    public long alarmTime;
    public long createdDate;
    public @Nullable String deviceSerialNumber;
    public @Nullable String deviceType;
    public @Nullable String id;
    public @Nullable String status;
    public @Nullable String type;
    public long remainingTime;
    public @Nullable String recurringPattern;
    public @Nullable String originalDate;
    public @Nullable String originalTime;
}

/*
 * Example JSON:
 * {
 *    "alarmTime":1518864868060,
 *    "createdDate":1518864863801,
 *    "deviceSerialNumber":"XXXXXXXXXX",
 *    "deviceType":"XXXXXXXXXX",
 *    "id":"XXXXXXXXXX-XXXXXXXXXX-XXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXX",
 *    "musicAlarmId":null,
 *    "musicEntity":null,
 *    "notificationIndex":"XXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXX",
 *    "originalDate":null,
 *    "originalTime":"11:54:28.060",
 *    "provider":null,
 *    "recurringPattern":null,
 *    "remainingTime":0,
 *    "reminderLabel":null,
 *    "sound":{
 *       "displayName":"Clarity",
 *       "folder":null,
 *       "id":"system_alerts_melodic_05",
 *       "providerId":"ECHO",
 *       "sampleUrl":"https://s3.amazonaws.com/deeappservice.prod.notificationtones/system_alerts_melodic_05.mp3"
 *    },
 *    "status":"OFF",
 *    "timeZoneId":null,
 *    "timerLabel":null,
 *    "triggerTime":0,
 *    "type":"Alarm",
 *    "version":"2",
 *    "alarmIndex":null,
 *    "isSaveInFlight":true
 * }
 *
 */
