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
package org.openhab.binding.ecotouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Trivial Exception Class.
 *
 * Used to solve compiler warning: "Avoid throwing raw exception types".
 *
 * @author Sebastian Held - Initial contribution
 * @since 3.1.0
 */
@NonNullByDefault
public class EcoTouchException extends Exception {
    private static final long serialVersionUID = -346364205220073265L;

    public EcoTouchException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public EcoTouchException(String errorMessage) {
        super(errorMessage);
    }

    public EcoTouchException() {
    }
}
