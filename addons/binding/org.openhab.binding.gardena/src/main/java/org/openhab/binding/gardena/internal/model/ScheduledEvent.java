/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena scheduled event.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ScheduledEvent {

    private String id;
    private String type;
    @SerializedName("start_at")
    private String start;
    @SerializedName("end_at")
    private String end;
    private String weekday;

    private Recurrence recurrence = new Recurrence();

    /**
     * Returns the id of the scheduled event.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the type of the scheduled event.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the start of the scheduled event.
     */
    public String getStart() {
        return start;
    }

    /**
     * Returns the end of the scheduled event.
     */
    public String getEnd() {
        return end;
    }

    /**
     * Returns the weekday of the scheduled event.
     */
    public String getWeekday() {
        return weekday;
    }

    /**
     * Returns the recurrence of the scheduled event.
     */
    public Recurrence getRecurrence() {
        return recurrence;
    }

}
