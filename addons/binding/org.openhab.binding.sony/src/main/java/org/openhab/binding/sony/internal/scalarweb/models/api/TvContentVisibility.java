/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class TvContentVisibility.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class TvContentVisibility {

    /** The uri. */
    private final String uri;

    /** The epg visibility. */
    private final String epgVisibility;

    /** The channel surfing visibility. */
    private final String channelSurfingVisibility;

    /** The visibility. */
    private final String visibility;

    /**
     * Instantiates a new tv content visibility.
     *
     * @param uri the uri
     * @param epgVisibility the epg visibility
     * @param channelSurfingVisibility the channel surfing visibility
     * @param visibility the visibility
     */
    public TvContentVisibility(String uri, String epgVisibility, String channelSurfingVisibility, String visibility) {
        super();
        this.uri = uri;
        this.epgVisibility = epgVisibility;
        this.channelSurfingVisibility = channelSurfingVisibility;
        this.visibility = visibility;
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the epg visibility.
     *
     * @return the epg visibility
     */
    public String getEpgVisibility() {
        return epgVisibility;
    }

    /**
     * Gets the channel surfing visibility.
     *
     * @return the channel surfing visibility
     */
    public String getChannelSurfingVisibility() {
        return channelSurfingVisibility;
    }

    /**
     * Gets the visibility.
     *
     * @return the visibility
     */
    public String getVisibility() {
        return visibility;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TvContentVisibility [uri=" + uri + ", epgVisibility=" + epgVisibility + ", channelSurfingVisibility="
                + channelSurfingVisibility + ", visibility=" + visibility + "]";
    }

}
