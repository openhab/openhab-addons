/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
