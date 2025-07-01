/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.pirateweather.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PirateWeatherCommunicationException} is a communication exception for the connections to Pirate Weather
 * API.
 *
 * @author Scott Hanson - Pirate Weather convertion
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PirateWeatherCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with null as its detail message.
     */
    public PirateWeatherCommunicationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detail message
     */
    public PirateWeatherCommunicationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause The cause
     */
    public PirateWeatherCommunicationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Detail message
     * @param cause The cause
     */
    public PirateWeatherCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
