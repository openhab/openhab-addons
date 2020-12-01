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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a browser bookmark and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class BrowserBookmark {

    /** The url being bookmarked */
    private @Nullable String url;

    /** The title of the bookmark */
    private @Nullable String title;

    /**
     * Constructor used for deserialization only
     */
    public BrowserBookmark() {
    }

    /**
     * Gets the bookmark url
     *
     * @return the bookmark url
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Gets the title
     *
     * @return the title
     */
    public @Nullable String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "BrowserBookmark [url=" + url + ", title=" + title + "]";
    }
}
