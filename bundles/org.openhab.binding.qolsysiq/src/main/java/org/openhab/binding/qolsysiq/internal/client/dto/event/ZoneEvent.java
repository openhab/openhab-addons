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
package org.openhab.binding.qolsysiq.internal.client.dto.event;

import com.google.gson.annotations.SerializedName;

/**
 * A Zone {@link Event} message sent by the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class ZoneEvent extends Event {
    @SerializedName("zone_event_type")
    public ZoneEventType type;

    public ZoneEvent(ZoneEventType type) {
        super(EventType.ZONE_EVENT);
        this.type = type;
    }
}
