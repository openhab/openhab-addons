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
 * Represents an openHAB item type and derives its corresponding channel type ID.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record ItemType(String name, String channelTypeId) {

    // Standard Static Instances
    public static final ItemType SWITCH = new ItemType("Switch", "switch");
    public static final ItemType STRING = new ItemType("String", "string");
    public static final ItemType NUMBER = new ItemType("Number", "number");
    public static final ItemType DIMENSIONLESS = number("Dimensionless");
    public static final ItemType CONTACT = new ItemType("Contact", "contact");
    public static final ItemType DIMMER = new ItemType("Dimmer", "dimmer");
    public static final ItemType COLOR = new ItemType("Color", "color");
    public static final ItemType ROLLERSHUTTER = new ItemType("Rollershutter", "rollershutter");
    public static final ItemType DATETIME = new ItemType("DateTime", "datetime");
    public static final ItemType IMAGE = new ItemType("Image", "image");
    public static final ItemType LOCATION = new ItemType("Location", "location");
    public static final ItemType PLAYER = new ItemType("Player", "player");

    /**
     * Creates a dimensioned number item type (e.g., {@code Number:Temperature}) which maps to the {@code number}
     * channel type.
     *
     * @param dimension the unit of measurement dimension (e.g., "Temperature", "Dimensionless")
     * @return a new {@link ItemType} instance configured with the UoM item type and "number" channel type
     */
    public static ItemType number(String dimension) {
        return new ItemType("Number:" + dimension, "number");
    }

    @Override
    public String toString() {
        return name;
    }
}
