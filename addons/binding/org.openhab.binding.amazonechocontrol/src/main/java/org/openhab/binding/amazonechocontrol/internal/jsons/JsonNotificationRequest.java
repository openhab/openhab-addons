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
 * The {@link JsonNotificationRequest} encapsulate the GSON data for a notification request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonNotificationRequest {
    public @Nullable String type = "Reminder"; // "Reminder", "Alarm"
    public @Nullable String status = "ON";
    public long alarmTime;
    public @Nullable String originalTime;
    public @Nullable String originalDate;
    public @Nullable String timeZoneId;
    public @Nullable String reminderIndex;
    public @Nullable JsonNotificationSound sound;
    public @Nullable String deviceSerialNumber;
    public @Nullable String deviceType;
    public @Nullable String recurringPattern;
    public @Nullable String reminderLabel;
    public boolean isSaveInFlight = true;
    public @Nullable String id = "createReminder";
    public boolean isRecurring = false;
    public long createdDate;
}
