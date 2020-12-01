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
 * This class represents the TV content visibility and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TvContentVisibility {
    /** The uri of the content */
    private final String uri;

    /** The epg visibility */
    private final @Nullable String epgVisibility;

    /** The channel surfing visibility */
    private final @Nullable String channelSurfingVisibility;

    /** The overall visibility */
    private final @Nullable String visibility;

    /**
     * Instantiates a new tv content visibility.
     *
     * @param uri the non-null, non-empty uri
     * @param epgVisibility the epg visibility (null if not specified)
     * @param channelSurfingVisibility the channel surfing visibility (null if not specified)
     * @param visibility the overall visibility (null if not specified)
     */
    public TvContentVisibility(final String uri, final @Nullable String epgVisibility,
            final @Nullable String channelSurfingVisibility, final @Nullable String visibility) {
        Validate.notEmpty(uri, "uri cannot be null");
        this.uri = uri;
        this.epgVisibility = epgVisibility;
        this.channelSurfingVisibility = channelSurfingVisibility;
        this.visibility = visibility;
    }

    /**
     * Gets the uri
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the epg visibility
     *
     * @return the epg visibility
     */
    public @Nullable String getEpgVisibility() {
        return epgVisibility;
    }

    /**
     * Gets the channel surfing visibility
     *
     * @return the channel surfing visibility
     */
    public @Nullable String getChannelSurfingVisibility() {
        return channelSurfingVisibility;
    }

    /**
     * Gets the overall visibility
     *
     * @return the overall visibility
     */
    public @Nullable String getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "TvContentVisibility [uri=" + uri + ", epgVisibility=" + epgVisibility + ", channelSurfingVisibility="
                + channelSurfingVisibility + ", visibility=" + visibility + "]";
    }
}
