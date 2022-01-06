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
