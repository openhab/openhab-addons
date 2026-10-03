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
package org.openhab.binding.hasslink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChannelType} enum defines the supported channel types in the Home Assistant Link binding.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public enum ChannelType {
    COLOR("color"),
    CONTACT("contact"),
    DATETIME("datetime"),
    DIMMER("dimmer"),
    IMAGE("image"),
    LOCATION("location"),
    NUMBER("number"),
    PLAYER("player"),
    ROLLERSHUTTER("rollershutter"),
    STRING("string"),
    SWITCH("switch");

    private final String id;

    ChannelType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
