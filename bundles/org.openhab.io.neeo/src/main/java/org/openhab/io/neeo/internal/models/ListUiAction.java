/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Describes the ui actions for a list item.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public enum ListUiAction {
    /** Go back to the directory root */
    GOTOROOT("goToRoot"),

    /** Go back a single level in the directory */
    GOBACK("goBack"),

    /** Close the directory */
    CLOSE("close"),

    /** Reload the current view */
    RELOAD("reload");

    /** The text value of the enum */
    private final String text;

    /**
     * Constructs the ListUiAction using the specified text
     *
     * @param text the text
     */
    private ListUiAction(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = text;
    }

    /**
     * Parses the text into a ListUiAction enum (ignoring case)
     *
     * @param text the text to parse
     * @return the ListUiAction type
     */
    public static ListUiAction parse(final String text) {
        if (text.isEmpty()) {
            return CLOSE;
        }
        for (ListUiAction enm : ListUiAction.values()) {
            if (text.equalsIgnoreCase(enm.text)) {
                return enm;
            }
        }

        return CLOSE;
    }

    /**
     * Determines if the specified text is a valid ListUiAction
     *
     * @param text the text to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(final String text) {
        if (text.isEmpty()) {
            return true;
        }
        for (ListUiAction enm : ListUiAction.values()) {
            if (text.equalsIgnoreCase(enm.text)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return text;
    }
}
