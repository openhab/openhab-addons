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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the application status and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ApplicationStatusList {

    /** The text input status */
    public static final String TEXTINPUT = "textInput";

    /** The cursor display status */
    public static final String CURSORDISPLAY = "cursorDisplay";

    /** The web browser status */
    public static final String WEBBROWSE = "webBrowse";

    /** The 'on' status */
    public static final String ON = "on";

    /** The 'off status */
    public static final String OFF = "off";

    /** The application name */
    private @Nullable String name;

    /** The application status */
    private @Nullable String status;

    /**
     * Constructor used for deserialization only
     */
    public ApplicationStatusList() {
    }

    /**
     * Gets the application name
     *
     * @return the application name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Gets the application status
     *
     * @return the applicatin status
     */
    public @Nullable String getStatus() {
        return status;
    }

    /**
     * Checks if the status is ON
     *
     * @return true, if is on - false otherwise
     */
    public boolean isOn() {
        return StringUtils.equalsIgnoreCase(ON, status);
    }

    @Override
    public String toString() {
        return "ApplicationStatusList [name=" + name + ", status=" + status + "]";
    }
}
