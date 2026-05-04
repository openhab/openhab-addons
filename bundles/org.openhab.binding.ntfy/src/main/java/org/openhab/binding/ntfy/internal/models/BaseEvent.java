/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.models;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for events received from the ntfy service.
 * <p>
 * This class contains common properties that are present on all events
 * such as the event id, timestamp, raw event string and the topic name.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public abstract class BaseEvent {
    @SerializedName("id")
    private String id = "";

    @SerializedName("time")
    private Instant time = Instant.MIN;

    @SerializedName("event")
    private String eventAsString = "";

    @SerializedName("topic")
    private String topic = "";

    /**
     * Returns the identifier of the event.
     *
     * @return the event id as a string (never null)
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of the event.
     *
     * @param id the event id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return the event time as {@link Instant}
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Sets the timestamp when the event occurred.
     *
     * @param time the event time
     */
    public void setTime(Instant time) {
        this.time = time;
    }

    /**
     * Returns the raw event type string as provided by the server (for example "message").
     *
     * @return the raw event type string
     */
    public String getEventAsString() {
        return eventAsString;
    }

    /**
     * Sets the raw event type string.
     *
     * @param eventAsString the raw event type
     */
    public void setEventAsString(String eventAsString) {
        this.eventAsString = eventAsString;
    }

    /**
     * Returns the parsed {@link EventType} for the underlying raw event string.
     *
     * @return the {@link EventType} representation of the event
     */
    public EventType getEvent() {
        return EventType.fromString(this.eventAsString);
    }

    /**
     * Returns the topic name this event belongs to.
     *
     * @return the topic name
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the topic name this event belongs to.
     *
     * @param topic the topic name
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
