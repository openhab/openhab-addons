/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.livetennisapi.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown for a transient Live Tennis API failure that is worth retrying: a rate-limit or quota rejection (HTTP 429)
 * or a request timeout. Handlers can catch this specifically to schedule a delayed retry rather than giving up until
 * the next scheduled cycle.
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiTransientException extends LiveTennisApiException {

    private static final long serialVersionUID = 1L;

    public LiveTennisApiTransientException(String message) {
        super(message);
    }

    public LiveTennisApiTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}
