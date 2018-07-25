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
 * Spotify Web Api Image data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Image {

    private Integer height;
    private String url;
    private Integer width;

    public Integer getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWidth() {
        return width;
    }
}
