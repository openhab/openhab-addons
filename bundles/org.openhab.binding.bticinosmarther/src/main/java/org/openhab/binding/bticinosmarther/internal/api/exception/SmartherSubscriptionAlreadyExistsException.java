/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Signals that a "subscription for given plant already exists" C2C Webhook issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherSubscriptionAlreadyExistsException extends SmartherNotificationException {

    private static final long serialVersionUID = 5185321219105493105L;

    /**
     * Constructs a {@code SmartherSubscriptionAlreadyExistsException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
