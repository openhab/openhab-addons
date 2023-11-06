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
