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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Notification} class defines the dto for Smarther API notification object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Notification {

    private String id;
    @SerializedName("eventType")
    private String eventType;
    private String subject;
    @SerializedName("eventTime")
    private String eventTime;
    private ModuleStatus data;

    /**
     * Returns the identifier of this notification.
     *
     * @return a string containing the notification identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the event type of this notification.
     *
     * @return a string containing the notification event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the subject of this notification.
     *
     * @return a string containing the notification subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns the event time of this notification.
     *
     * @return a string containing the notification event time
     */
    public String getEventTime() {
        return eventTime;
    }

    /**
     * Returns the module status data (i.e. the payload) of this notification.
     *
     * @return the module status data, or {@code null} in case of no data found
     */
    public @Nullable ModuleStatus getData() {
        return data;
    }

    /**
     * Returns the chronothermostat details of this notification.
     *
     * @return the chronothermostat details, or {@code null} in case of no data found
     */
    public @Nullable Chronothermostat getChronothermostat() {
        if (data != null) {
            return data.toChronothermostat();
        }
        return null;
    }

    /**
     * Returns the sender details of this notification.
     *
     * @return the sender details, or {@code null} in case of no data found
     */
    public @Nullable Sender getSender() {
        if (data != null) {
            final Chronothermostat chronothermostat = data.toChronothermostat();
            if (chronothermostat != null) {
                return chronothermostat.getSender();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("id=%s, eventType=%s, subject=%s, eventTime=%s, data=[%s]", id, eventType, subject,
                eventTime, data);
    }
}
