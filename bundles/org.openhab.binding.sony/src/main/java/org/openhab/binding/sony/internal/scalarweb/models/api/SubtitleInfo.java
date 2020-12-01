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
 * This class represents the subtitle information
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SubtitleInfo {
    /** The subtitle language */
    private @Nullable String langauge;

    /** The name (title) of the subtitle */
    private @Nullable String title;

    /**
     * Constructor used for deserialization only
     */
    public SubtitleInfo() {
    }

    /**
     * Gets the subtitle language
     * 
     * @return the subtitle language
     */
    public @Nullable String getLangauge() {
        return langauge;
    }

    /**
     * Gets the subtitle title (name)
     * 
     * @return the subtitle title (name)
     */
    public @Nullable String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "SubtitleInfo [langauge=" + langauge + ", title=" + title + "]";
    }
}
