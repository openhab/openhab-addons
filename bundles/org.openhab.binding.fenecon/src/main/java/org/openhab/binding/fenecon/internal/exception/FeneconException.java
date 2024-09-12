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
package org.openhab.binding.fenecon.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FeneconException} class provides general exception for this binding.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconException extends Exception {

    private static final long serialVersionUID = 4454633961827361165L;

    public FeneconException() {
        // noop
    }

    public FeneconException(String message) {
        super(message);
    }

    public FeneconException(Exception exception) {
        super(exception);
    }

    public FeneconException(String message, Exception exception) {
        super(message, exception);
    }
}
