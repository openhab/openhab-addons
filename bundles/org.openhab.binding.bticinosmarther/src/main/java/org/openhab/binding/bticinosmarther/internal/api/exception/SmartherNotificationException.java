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
package org.openhab.binding.bticinosmarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals that a generic C2C Webhook notification issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherNotificationException extends RuntimeException {

    private static final long serialVersionUID = -634107708647244174L;

    /**
     * Constructs a {@code SmartherNotificationException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherNotificationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code SmartherNotificationException} with the specified detail message and cause.
     *
     * @param message
     *            the error message returned from the API gateway
     * @param exception
     *            the cause (a null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public SmartherNotificationException(String message, Throwable exception) {
        super(message, exception);
    }
}
