/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Smarther API Notification DTO class.
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

    public String getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSubject() {
        return subject;
    }

    public String getEventTime() {
        return eventTime;
    }

    public ModuleStatus getData() {
        return data;
    }

    public boolean hasData() {
        return (data != null);
    }

    @Override
    public String toString() {
        return String.format("id=%s, eventType=%s, subject=%s, eventTime=%s, data=[%s]", id, eventType, subject,
                eventTime, data);
    }

}
