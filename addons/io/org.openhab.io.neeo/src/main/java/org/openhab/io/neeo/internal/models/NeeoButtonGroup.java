/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This helper class provides information about button groups that can be specified by the user (like binding a switch
 * to an up/down button pair). The button group will be used by the system to determine what buttons (and URL suffixes
 * to identify those button) will be created when a button group is chosen by the user.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoButtonGroup {
    /**
     * The Power ON/OFF button group that will create the "POWER ON" and "POWER OFF" buttons with suffixes of "on/off"
     */
    public static final NeeoButtonGroup POWERONOFF = new NeeoButtonGroup("POWERONOFF", ButtonInfo.POWERON,
            ButtonInfo.POWEROFF);

    /**
     * The VOLUMES button group that will create the "VOLUME UP" and "VOLUME DOWN" buttons with suffixes of
     * "increase/decrease"
     */
    private static final NeeoButtonGroup VOLUMES = new NeeoButtonGroup("VOLUMES",
            new ButtonInfo("VOLUME UP", "increase"), new ButtonInfo("VOLUME DOWN", "decrease"));

    /**
     * The CHANNELS button group that will create the "CHANNEL UP" and "CHANNEL DOWN" buttons with suffixes of
     * "increase/decrease"
     */
    private static final NeeoButtonGroup CHANNELS = new NeeoButtonGroup("CHANNELS",
            new ButtonInfo("CHANNEL UP", "increase"), new ButtonInfo("CHANNEL DOWN", "decrease"));

    /**
     * The CURSOR UP/DOWN button group that will create the "CURSOR UP" and "CURSOR DOWN" buttons with suffixes of
     * "increase/decrease"
     */
    private static final NeeoButtonGroup CURSORUPDOWN = new NeeoButtonGroup("CURSORUPDOWN",
            new ButtonInfo("CURSOR UP", "increase"), new ButtonInfo("CURSOR DOWN", "decrease"));

    /**
     * The CURSOR LEFT/RIGHT button group that will create the "CURSOR LEFT" and "CURSOR RIGHT" buttons with suffixes of
     * "increase/decrease"
     */
    private static final NeeoButtonGroup CURSORLEFTRIGHT = new NeeoButtonGroup("CURSORLEFTRIGHT",
            new ButtonInfo("CURSOR LEFT", "increase"), new ButtonInfo("CURSOR RIGHT", "decrease"));

    /** The text value for the button group (specified by the user) */
    private final String text;

    /** The button information related to this button group */
    private final ButtonInfo[] buttons;

    /**
     * Create the button group from the given text and button information
     *
     * @param text the button group text (not null, not empty)
     * @param buttons the button information in this group (not null, not empty)
     */
    private NeeoButtonGroup(final String text, final ButtonInfo... buttons) {
        Objects.requireNonNull(text, "text is required");
        Objects.requireNonNull(buttons, "buttons is required");
        if (buttons.length == 0) {
            throw new IllegalArgumentException("Atleast one ButtonInfo must be specified");
        }
        this.text = text;
        this.buttons = buttons;
    }

    /**
     * Gets the text for this button group.
     *
     * @return the non-null, non-empty text for this button group
     */
    public String getText() {
        return text;
    }

    /**
     * Get's the button information for this button group
     *
     * @return a non-null, non-empty array of {@link ButtonInfo}
     */
    public ButtonInfo[] getButtonInfos() {
        return buttons;
    }

    /**
     * Parses the given text info a {@link NeeoButtonGroup} if the text matches (ignoring case) any of the button groups
     * defined. Returns null if the text doesn't match any button group.
     *
     * @param text the text to parse
     * @return the possibly null {@link NeeoButtonGroup}
     */
    @Nullable
    public static NeeoButtonGroup parse(final String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        // note: if we add more - might want to switch this into a loop
        if (StringUtils.equalsIgnoreCase(text, POWERONOFF.text)) {
            return POWERONOFF;
        }

        if (StringUtils.equalsIgnoreCase(text, VOLUMES.text)) {
            return VOLUMES;
        }

        if (StringUtils.equalsIgnoreCase(text, CHANNELS.text)) {
            return CHANNELS;
        }

        if (StringUtils.equalsIgnoreCase(text, CURSORUPDOWN.text)) {
            return CURSORUPDOWN;
        }

        if (StringUtils.equalsIgnoreCase(text, CURSORLEFTRIGHT.text)) {
            return CURSORLEFTRIGHT;
        }

        return null;
    }

    /**
     * Performs a case insensitive match to the given text
     *
     * @param text a possibly null, possibly empty text to match against
     * @return true if matches, false otherwise
     */
    public boolean equals(String text) {
        return StringUtils.equalsIgnoreCase(this.text, text);
    }

    @Override
    public String toString() {
        return text;
    }
}
