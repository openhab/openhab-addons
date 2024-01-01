/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal.api.exception;

/**
 * Spotify authorization problems exception class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SpotifyAuthorizationException extends RuntimeException {

    private static final long serialVersionUID = -1931713564920750911L;

    /**
     * Constructor.
     *
     * @param message Spotify error message
     */
    public SpotifyAuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message Spotify error message
     * @param exception Original cause of this exception
     */
    public SpotifyAuthorizationException(String message, Throwable exception) {
        super(message, exception);
    }
}
