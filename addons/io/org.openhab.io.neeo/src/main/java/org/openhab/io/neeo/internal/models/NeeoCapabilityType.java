/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.type.ChannelType;

/**
 * Defines the various NEEO capability types
 *
 * @author Tim Roberts - Initial contribution
 */
public enum NeeoCapabilityType {
    BUTTON("button"),
    SWITCH("switch"),
    SLIDER("slider"),
    SENSOR("sensor"),
    TEXTLABEL("textlabel"),
    IMAGEURL("imageurl"),
    DISCOVER("discover"),
    SENSOR_CUSTOM("custom"),
    SENSOR_RANGE("range"),
    SENSOR_BINARY("binary"),
    SENSOR_POWER("power"),
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
        if (StringUtils.isEmpty(text)) {
            return EXCLUDE;
        }
        for (NeeoCapabilityType enm : NeeoCapabilityType.values()) {
            if (StringUtils.equalsIgnoreCase(text, enm.text)) {
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
    public static NeeoCapabilityType guessType(ChannelType channelType) {
        if (channelType == null || StringUtils.isEmpty(channelType.getItemType())) {
            return NeeoCapabilityType.EXCLUDE;
        }

        switch (channelType.getItemType().toLowerCase()) {
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
