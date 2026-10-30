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
package org.openhab.binding.philipsair.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PhilipsAirAPIException} is a communication exception for the
 * connections to Philips Air Purifier.
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Code cleanup and align with guidelines
 */
@NonNullByDefault
public class PhilipsAirAPIException extends Exception {

    private static final long serialVersionUID = 1L;

    public PhilipsAirAPIException() {
        super();
    }

    public PhilipsAirAPIException(String message) {
        super(message);
    }

    public PhilipsAirAPIException(Throwable cause) {
        super(cause);
    }

    public PhilipsAirAPIException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
