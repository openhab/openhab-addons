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
package org.openhab.binding.visualcrossing.internal.api;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class VisualCrossingApiException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public VisualCrossingApiException() {
    }

    public VisualCrossingApiException(String message) {
        super(message);
    }

    public VisualCrossingApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public VisualCrossingApiException(Throwable cause) {
        super(cause);
    }

    public VisualCrossingApiException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
