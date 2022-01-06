/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
