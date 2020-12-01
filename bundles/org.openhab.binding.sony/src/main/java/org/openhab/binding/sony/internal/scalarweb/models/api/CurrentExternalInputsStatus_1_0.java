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
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a current external input status
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class CurrentExternalInputsStatus_1_0 {

    /** The uri identifying the input */
    private @Nullable String uri;

    /** The title (name) of the input */
    private @Nullable String title;

    /** The connection status */
    private @Nullable Boolean connection;

    /** The label of the input */
    private @Nullable String label;

    /** The icon for the input */
    private @Nullable String icon;

    /**
     * Constructor used for deserialization only
     */
    public CurrentExternalInputsStatus_1_0() {
    }

    /**
     * Gets the uri of the input
     *
     * @return the uri of the input
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the title of the input
     *
     * @return the title of the input
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Get's the title (or label) for the input or the default value if none
     * 
     * @param defaultValue the non-null, non-empty default value
     * @return a non-null, non-empty title
     */
    public String getTitle(final String defaultValue) {
        Validate.notEmpty(defaultValue, "defaultValue cannot be empty");

        final String titleOrLabel = StringUtils.defaultIfEmpty(title, label);
        return StringUtils.defaultIfEmpty(titleOrLabel, defaultValue);
    }

    /**
     * Returns the connection status
     *
     * @return true, if it is a connection
     */
    public @Nullable Boolean isConnection() {
        return connection;
    }

    /**
     * Gets the label of the input
     *
     * @return the label of the input
     */
    public @Nullable String getLabel() {
        return label;
    }

    /**
     * Gets the icon of the input
     *
     * @return the icon of the input
     */
    public @Nullable String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "CurrentExternalInputsStatus_1_0 [uri=" + uri + ", title=" + title + ", connection=" + connection
                + ", label=" + label + ", icon=" + icon + "]";
    }
}
