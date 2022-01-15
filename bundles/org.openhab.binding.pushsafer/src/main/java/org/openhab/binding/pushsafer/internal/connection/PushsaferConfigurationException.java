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
package org.openhab.binding.pushsafer.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PushsaferConfigurationException} is a configuration exception for the connections to Pushsafer Messages
 * API.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class PushsaferConfigurationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with null as its detail message.
     */
    public PushsaferConfigurationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detail message
     */
    public PushsaferConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause The cause
     */
    public PushsaferConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Detail message
     * @param cause The cause
     */
    public PushsaferConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
