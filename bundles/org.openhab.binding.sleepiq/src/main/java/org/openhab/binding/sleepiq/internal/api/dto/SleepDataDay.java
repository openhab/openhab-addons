/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.dto;

import java.time.LocalDate;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SleepDataDay} holds the information for a daily sleep session.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SleepDataDay {

    @SerializedName("date")
    private LocalDate date;

    @SerializedName("message")
    private String dailyMessage;

    @SerializedName("sessions")
    private List<SleepDataSession> dailySessions;

    public LocalDate getDate() {
        return date;
    }

    public String getMessage() {
        return dailyMessage;
    }

    public List<SleepDataSession> getDailySessions() {
        return dailySessions;
    }
}
