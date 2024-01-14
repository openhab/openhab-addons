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
package org.openhab.binding.buienradar.internal.buienradarapi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception thrown when a result from Buienradar could not be correctly parsed.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public class BuienradarParseException extends Exception {

    private static final long serialVersionUID = 1L;

    public BuienradarParseException() {
        super();
    }

    public BuienradarParseException(@Nullable String message) {
        super(message);
    }

    public BuienradarParseException(@Nullable Throwable cause) {
        super(cause);
    }

    public BuienradarParseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public BuienradarParseException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
