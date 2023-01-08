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
package org.openhab.binding.zoneminder.internal.handler;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Event} represents the attributes of a Zoneminder event
 * that are relevant to this binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Event {

    private String id;
    private String name;
    private String cause;
    private String notes;
    private Date start;
    private Date end;
    private int frames;
    private int alarmFrames;
    private double length;

    public Event(String id, String name, String cause, String notes, Date start, Date end) {
        this.id = id;
        this.name = name;
        this.cause = cause;
        this.notes = notes;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public String setId(String id) {
        return this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getFrames() {
        return frames;
    }

    public void setFrames(@Nullable Integer frames) {
        this.frames = frames != null ? frames : 0;
    }

    public int getAlarmFrames() {
        return alarmFrames;
    }

    public void setAlarmFrames(@Nullable Integer alarmFrames) {
        this.alarmFrames = alarmFrames != null ? alarmFrames : 0;
    }

    public double getLength() {
        return length;
    }

    public void setLength(@Nullable Double length) {
        this.length = length != null ? length : 0;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", cause=").append(cause);
        sb.append(", start=").append(start.toString());
        sb.append(", end=").append(end.toString());
        sb.append(", frames=").append(String.format("%d", frames));
        sb.append(", alarmFrames=").append(String.format("%d", alarmFrames));
        sb.append(", length=").append(String.format("%6.2", length));
        sb.append(")");
        return sb.toString();
    }
}
