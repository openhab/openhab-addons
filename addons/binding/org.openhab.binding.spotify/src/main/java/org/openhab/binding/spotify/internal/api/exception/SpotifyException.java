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
 * Generic Spotify exception class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SpotifyException extends RuntimeException {

    private static final long serialVersionUID = -8142837343923954830L;

    /**
     * Constructor.
     *
     * @param message Spotify error message
     */
    public SpotifyException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message Spotify error message
     * @param cause Original cause of this exception
     */
    public SpotifyException(String message, Throwable cause) {
        super(message, cause);
    }

}
