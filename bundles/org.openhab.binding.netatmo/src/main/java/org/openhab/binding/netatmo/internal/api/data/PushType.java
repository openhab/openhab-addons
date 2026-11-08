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
package org.openhab.binding.netatmo.internal.api.data;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This enum describes types of events pushed in webhooks according to
 * API documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum PushType {
    UNKNOWN,

    @SerializedName("webhook_activation") // A user linked the webhook to their account
    WEBHOOK_ACTIVATION,

    @SerializedName("webhook_deletion") // A user deleted their link to the webhook
    WEBHOOK_DELETION,

    @SerializedName("topology_changed") // A relation has changed (e.g. change: "home_owner_added")
    TOPOLOGY_CHANGED,

    @SerializedName("home_event_changed") // A home configuration has changed (e.g. event_type: "schedule")
    HOME_EVENT_CHANGED,

    @SerializedName("device_event") // A device state has changed
    DEVICE_EVENT;

    public static final EnumSet<PushType> AS_SET = EnumSet.allOf(PushType.class);

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
