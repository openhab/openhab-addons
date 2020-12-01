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

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the URL in the onscreen browser and is used for serialization/deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TextUrl {
    /** The url */
    private @Nullable String url;

    /** The title of the page (result only) */
    private @Nullable String title;

    /** The type of page (result only) */
    private @Nullable String type;

    /** The favorite icon (result only) */
    private @Nullable String favicon;

    /**
     * Constructor used for deserialization only
     */
    public TextUrl() {
    }

    /**
     * Instantiates a new URL request
     *
     * @param url the non-null, non-empty url
     */
    public TextUrl(final String url) {
        Validate.notEmpty(url, "url cannot be empty");
        this.url = url;
    }

    /**
     * Gets the url
     *
     * @return the url
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Gets the page title
     *
     * @return the page title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the page type
     *
     * @return the page type
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * Gets the favorite icon
     *
     * @return the favorite icon
     */
    public @Nullable String getFavicon() {
        return favicon;
    }

    @Override
    public @Nullable String toString() {
        return "TextUrl [url=" + url + ", title=" + title + ", type=" + type + ", favicon=" + favicon + "]";
    }
}
