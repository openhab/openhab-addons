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
 * This class specifies the visibility of an item
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Visibility {
    /** The epg visibility */
    private @Nullable String epgVisibility;

    /** The channel surfing visibility */
    private @Nullable String channelSurfingVisibility;

    /** The visibility of the content */
    private @Nullable String visibility;

    /**
     * Constructor used for deserialization only
     */
    public Visibility() {
    }

    /**
     * Gets the EPG visibility
     * 
     * @return the EPG visibility
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
     * Gets the general visibility
     * 
     * @return the general visibility
     */
    public @Nullable String getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "Visibility [epgVisibility=" + epgVisibility + ", channelSurfingVisibility=" + channelSurfingVisibility
                + ", visibility=" + visibility + "]";
    }
}
