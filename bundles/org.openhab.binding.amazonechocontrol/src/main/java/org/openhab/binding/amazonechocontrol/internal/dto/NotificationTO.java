/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link NotificationTO} encapsulate a single notification
 *
 * @author Jan N. Klug - Initial contribution
 */
public class NotificationTO {
    public String id;
    public String type;
    public String version;

    public String deviceSerialNumber;
    public String deviceType;

    public long alarmTime;
    public long createdDate;
    public Object musicAlarmId;
    public Object musicEntity;
    public String notificationIndex;
    public String originalDate;
    public String originalTime;
    public Object provider;
    public boolean isRecurring;
    public String recurringPattern;
    public String reminderLabel;
    public String reminderIndex;
    public NotificationSoundTO sound = new NotificationSoundTO();
    public String status;
    public String timeZoneId;
    public String timerLabel;
    public Object alarmIndex;
    public boolean isSaveInFlight;
    public Long triggerTime;
    public Long remainingTime;

    @Override
    public @NonNull String toString() {
        return "NotificationTO{id='" + id + "', type='" + type + "', version='" + version + "', deviceSerialNumber='"
                + deviceSerialNumber + "', deviceType='" + deviceType + "', alarmTime=" + alarmTime + ", createdDate="
                + createdDate + ", musicAlarmId=" + musicAlarmId + ", musicEntity=" + musicEntity
                + ", notificationIndex='" + notificationIndex + "', originalDate='" + originalDate + "', originalTime='"
                + originalTime + "', provider=" + provider + ", isRecurring=" + isRecurring + ", recurringPattern='"
                + recurringPattern + "', reminderLabel='" + reminderLabel + "', reminderIndex='" + reminderIndex
                + "', sound=" + sound + ", status='" + status + "', timeZoneId='" + timeZoneId + "', timerLabel='"
                + timerLabel + "', alarmIndex=" + alarmIndex + ", isSaveInFlight=" + isSaveInFlight + ", triggerTime="
                + triggerTime + ", remainingTime=" + remainingTime + "}";
    }
}
