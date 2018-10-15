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
 * Spotify Web Api Context data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Context {
    private String type;
    private String href;
    private ExternalUrl externalUrls;
    private String uri;

    public String getType() {
        return type;
    }

    public String getHref() {
        return href;
    }

    public ExternalUrl getExternalUrls() {
        return externalUrls;
    }

    public String getUri() {
        return uri;
    }
}
