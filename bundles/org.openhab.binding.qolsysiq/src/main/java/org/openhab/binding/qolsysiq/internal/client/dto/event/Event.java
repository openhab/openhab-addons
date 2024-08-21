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
package org.openhab.binding.qolsysiq.internal.client.dto.event;

import com.google.gson.annotations.SerializedName;

/**
 * The base type for various event messages sent by the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class Event {
    @SerializedName("event")
    public EventType eventType;
    public String nonce;
    @SerializedName("requestID")
    public String requestID;

    public Event(EventType eventType) {
        this.eventType = eventType;
    }
}
