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
package org.openhab.binding.tailwind.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown when a request to a TailWind controller did not succeed. This class itself means that the controller
 * answered and refused the request.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindException extends Exception {
    private static final long serialVersionUID = 1L;

    public TailwindException(String message) {
        super(message);
    }

    public TailwindException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
