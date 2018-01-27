/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

/**
 * The {@link JsonNotificationRequest} encapsulate the GSON data for a notification request
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonNotificationRequest {
    public String type = "Reminder"; // "Reminder", "Alarm"
    public String status = "ON";
    public long alarmTime;
    public String originalTime;
    public String originalDate;
    public String timeZoneId;
    public String reminderIndex;
    public JsonNotificationSound sound;
    public String deviceSerialNumber;
    public String deviceType;
    public String recurringPattern;
    public String reminderLabel;
    public boolean isSaveInFlight = true;
    public String id = "createReminder";
    public boolean isRecurring = false;
    public long createdDate;

}
