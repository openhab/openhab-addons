/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api.model;

/**
 * Spotify Web Api ModelObjectType data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public enum ModelObjectType {
    ALBUM("album"),
    ARTIST("artist"),
    AUDIO_FEATURES("audio_features"),
    GENRE("genre"),
    PLAYLIST("playlist"),
    TRACK("track"),
    USER("user");

    public final String type;

    ModelObjectType(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
