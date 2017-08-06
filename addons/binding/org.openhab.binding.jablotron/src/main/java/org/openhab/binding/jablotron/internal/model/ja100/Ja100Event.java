/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.ja100;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The {@link Ja100StatusResponse} class defines the JA100 last event
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class Ja100Event {
    private String date;

    @SerializedName("section-name")
    private String section;

    @SerializedName("event-text")
    private String event;

    @SerializedName("icon-type")
    private String eventClass;

    public String getDate() {
        return date;
    }

    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.parse(date.substring(0,22) + ":" + date.substring(22,24), DateTimeFormatter.ISO_DATE_TIME);
    }

    public String getEvent() {
        return event;
    }

    public String getEventClass() {
        return eventClass;
    }

    public String getSection() {
        return section;
    }

    @Override
    public String toString() {
        return "Ja100Event{" +
                "date=" + date +
                ", section='" + section + '\'' +
                ", event='" + event + '\'' +
                ", eventClass='" + eventClass + '\'' +
                '}';
    }
}
