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
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;

/**
 * Defines the various NEEO capability types
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public enum NeeoCapabilityType {
    /** Represents the NEEO BUTTON capability */
    BUTTON("button"),
    /** Represents the NEEO SWITCH capability */
    SWITCH("switch"),
    /** Represents the NEEO SLIDER capability */
    SLIDER("slider"),
    /** Represents the NEEO SENSOR capability */
    SENSOR("sensor"),
    /** Represents the NEEO TEXT LABEL capability */
    TEXTLABEL("textlabel"),
    /** Represents the NEEO IMAGE URL capability */
    IMAGEURL("imageurl"),
    /** Represents the NEEO directory capability */
    DIRECTORY("directory"),

    /** Represents the NEEO CUSTOM SENSOR capability */
    SENSOR_CUSTOM("custom"),
    /** Represents the NEEO RANGE SENSOR capability */
    SENSOR_RANGE("range"),
    /** Represents the NEEO BINARY SENSOR capability */
    SENSOR_BINARY("binary"),
    /** Represents the NEEO POWER SENSOR capability */
    SENSOR_POWER("power"),
    /** Represents no capability (and should be excluded) */
    EXCLUDE("");

    /** The text value of the enum */
    private final String text;

    /**
     * Constructs the NeeoCapabilityType using the specified text
     *
     * @param text the text
     */
    private NeeoCapabilityType(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = text;
    }

    /**
     * Parses the text into a NeeoCapabilityType enum (ignoring case)
     *
     * @param text the text to parse
     * @return the NeeoCapabilityType type
     */
    public static NeeoCapabilityType parse(final String text) {
        if (text.isEmpty()) {
            return EXCLUDE;
        }
        for (NeeoCapabilityType enm : NeeoCapabilityType.values()) {
            if (text.equalsIgnoreCase(enm.text)) {
                return enm;
            }
        }

        return EXCLUDE;
    }

    /**
     * Guess the {@link NeeoCapabilityType} for the given {@link ChannelType}
     *
     * @param channelType the possibly null channel type
     * @return the best guess {@link NeeoCapabilityType}
     */
    public static NeeoCapabilityType guessType(@Nullable ChannelType channelType) {
        String itemType = channelType == null ? null : channelType.getItemType();
        if (channelType == null || itemType == null || itemType.isEmpty()) {
            return NeeoCapabilityType.EXCLUDE;
        }

        switch (itemType.toLowerCase()) {
            case "switch":
            case "contact":
            case "rollershutter":
                return NeeoCapabilityType.SWITCH;
            case "datetime":
            case "number":
            case "point":
            case "string":
                return NeeoCapabilityType.TEXTLABEL;
            case "dimmer":
            case "color":
                return NeeoCapabilityType.SLIDER;
            default:
                return NeeoCapabilityType.EXCLUDE;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
