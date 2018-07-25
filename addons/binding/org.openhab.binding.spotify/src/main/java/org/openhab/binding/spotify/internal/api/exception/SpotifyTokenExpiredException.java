/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api.exception;

/**
 * Spotify exception indicating the access token has expired.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SpotifyTokenExpiredException extends SpotifyAuthorizationException {

    private static final long serialVersionUID = 709275673779738436L;

    /**
     * Constructor
     *
     * @param message Spotify error message
     */
    public SpotifyTokenExpiredException(String message) {
        super(message);
    }
}
